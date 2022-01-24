/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ１２０Ｋ＞  成績チエックリスト
 *
 *	2004/09/14 yamashiro・新規作成
 *  2004/11/29 yamashiro・類型別学年評定を追加
 *                      ・出欠の出力を追加
 *  2004/12/13 yamashiro・同一講座コードを複数科目で使用した際に起こる不具合を修正 => 生徒名が重複して出力される
 *  2005/03/02 nakamoto ・文字評定を追加。合計欄などは空白。（総合学習='900100'のみ表示）---NO001
 *  2006/02/07 nakamoto ・NO002 複数クラスが混在する講座の場合、２枚目以降に各クラス毎のチェックリストを出力するよう追加修正
 */

package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJD120K {

    private static final Log log = LogFactory.getLog(KNJD120K.class);
	private DecimalFormat dmf1 = new DecimalFormat("00");
	private List grclList = new ArrayList();//NO002 年組を保持
	private boolean isABCHyoutei;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
        String param[] = new String[7];

	//	パラメータの取得
		//String printname = request.getParameter("PRINTNAME");   			//プリンタ名
		String printname = null;											//PDFで出力用！！
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
			param[1] = request.getParameter("SEMESTER");   					//1-3:学期
			param[2] = request.getParameter("CHAIRCD");  					//講座コード
			param[3] = request.getParameter("SUBCLASSCD");					//科目コード  04/12/13Add
			param[4] = request.getParameter("useCurriculumcd");
            //教育課程対応
            if ("1".equals(param[4])) {
			    param[5] = StringUtils.split(param[3], "-")[3];
			} else {
			    param[5] = param[3];
			}
            param[6] = request.getParameter("useProvFlg"); // プロパティ：仮評定の追加対応
		} catch( Exception ex ) {
			log.error("[KNJD120K]parameter error!", ex);
		}
        log.fatal("$Revision: 56595 $");
		KNJServletUtils.debugParam(request, log);

	//	print設定-->printnameが存在する-->プリンターへ直接出力の場合
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		if( printname!=null )	response.setContentType("text/html");
		else					response.setContentType("application/pdf");

	//	svf設定-->printnameが存在する-->プリンターへ直接出力の場合
		int ret = svf.VrInit();						   		//クラスの初期化
		if( printname!=null ){
        	ret = svf.VrSetPrinter("", printname);			//プリンタ名の設定
			if( ret < 0 ) log.info("printname ret = " + ret);
		} else
			ret = svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("[KNJD120K]DB2 open error!", ex);
		}

		//文字評定
		isABCHyoutei = false;
		if (param[5].equals("900100")) {
            isABCHyoutei = true;
        }
        ResultSet rs = null;
        try {
            db2.query( getABCHyoutei(param) );
            rs = db2.getResultSet();
            while (rs.next()) {
                final String subcd = rs.getString("NAME1");
                if (param[3].equals(subcd)) {
                    isABCHyoutei = true;
                }
            }
            rs.close();
        } catch( Exception ex ){
            log.error("[KNJD120K]getABCHyoutei error!", ex );
        }

	//	ＳＶＦ作成処理
		boolean nonedata = false;
		setHead(db2,svf,param);			//見出し項目
//NO002
//		if( printSvfMain(db2,svf,param) )nonedata = true;		//SVF-FORM出力処理
		ret = svf.VrsOut("TITLENO"  , "1" );
		if( printSvfMain(db2,svf,param,"0") )nonedata = true;		//SVF-FORM出力処理
        //複数クラスが混在する講座の場合のみ処理する
        if( nonedata && 1 < grclList.size() ){
            for(int ib=0 ; ib<grclList.size() ; ib++) {
        		ret = svf.VrsOut("TITLENO"  , String.valueOf(ib+2) );
                printSvfMain(db2,svf,param,(String)grclList.get(ib));
    		}
		}
//NO002

	//	該当データ無し-->printnameが存在する-->プリンターへ直接出力の場合
		if( printname!=null ){
			if( !nonedata )	outstrm.println("<H1>対象データはありません。</h1>");
			else			outstrm.println("<H1>印刷しました。</h1>");
		} else if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		if( ret == 0 )log.info("===> VrQuit():" + ret);
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** SVF-FORMセット＆見出し項目 **/
	private void setHead(DB2UDB db2,Vrw32alp svf,String param[]){

        final String form = "1".equals(param[6]) ? "KNJD120_P.frm" : "KNJD120.frm";
        svf.VrSetForm(form, 4);

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		svf.VrsOut("NENDO",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");

	//	作成日(現在処理日)
		try {
			returnval = getinfo.Control(db2);
			svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));
		} catch( Exception ex ){
			log.error("[KNJD120K]ReturnVal setHead() get TODAY error!", ex );
		}

	//	科目名 04/12/13Modify
		ResultSet rs = null;
		try {
			db2.query( prestatementHeadSubclass(param) );
			rs = db2.getResultSet();
			if( rs.next() ){
				svf.VrsOut("SUBCLASS",rs.getString("SUBCLASSNAME"));
			}
			rs.close();
		} catch( Exception ex ){
			log.error("[KNJD120K]setHead_ hrclass_staff error!", ex );
		}

	//	講座名 04/12/13Modify
		try {
			db2.query( prestatementHeadChair(param) );
			rs = db2.getResultSet();
			if( rs.next() ){
				svf.VrsOut("HR_CLASS",rs.getString("CHAIRNAME"));
			}
			rs.close();
		} catch( Exception ex ){
			log.error("[KNJD120K]setHead_ hrclass_staff error!", ex );
		}

		getinfo = null;
		returnval = null;

	}//setHead()の括り



	/** SVF-FORM メイン出力処理 **/
//NO002
//	private boolean printSvfMain(DB2UDB db2,Vrw32alp svf,String param[])
	private boolean printSvfMain(DB2UDB db2,Vrw32alp svf,String param[],String grclcd)
	{
for(int ib=0 ; ib<param.length ; ib++)log.debug("[KNJD120K]boolean Set_Detail_2() param["+ib+"]="+param[ib]);
		//定義
		boolean nonedata = false;
		ResultSet rs = null;
		//RecordSet作成
		try {
log.debug("grclcd = "+grclcd);
//NO002
//			db2.query( prestatementRecord(param,0) );			//生徒別成績データ
			db2.query( prestatementRecord(param,0,grclcd) );			//生徒別成績データ
			rs = db2.getResultSet();
			int linex = 0;								//１ページ当り出力行数
            String grcl = "0";//NO002
			while( rs.next() ){
                if ( grclcd.equals("0") && !grcl.equals(rs.getString("GR_CL")) ) grclList.add(rs.getString("GR_CL"));//NO002
				if( printSvfOutMeisai(svf,rs,param) ){			//SVF-FORMへ出力---NO001
					nonedata = true;
					linex++;
				}
                grcl = rs.getString("GR_CL");
			}//while()
			if( linex<50 ){		//明細行は５０行まで->足りない場合は空行を出力！
		//		svf.VrAttribute("RECORD1","Print=1");
				for( ; linex<50 ; linex++ ){
					svf.VrAttribute("NAME","Meido=100");
					svf.VrsOut("NAME" 	," . ");
					svf.VrEndRecord();
				}
			}
//NO002
//			db2.query( prestatementRecord(param,1) );			//成績合計データ
			db2.query( prestatementRecord(param,1,grclcd) );			//成績合計データ
			rs = db2.getResultSet();
			if( rs.next() )printSvfOutTotal(svf,rs,param);		//SVF-FORMへ出力---NO001
			rs.close();
		} catch( Exception ex ) { log.error("[KNJD120K]printSvfMain read error! ", ex);	}

		return nonedata;

	}//boolean printSvfMain()の括り



	/** 
     *   ＨＲ成績生徒別明細を出力 => VrEndRecord()
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
	private boolean printSvfOutMeisai(Vrw32alp svf,ResultSet rs,String param[]){

		boolean nonedata = false;
		try {
			int ret = 0;
			ret = svf.VrsOut("ATTENDNO"   , rs.getString("HR_NAMEABBV") + "-" + dmf1.format(rs.getInt("ATTENDNO")) );
			ret = svf.VrsOut("NAME" 	  , rs.getString("NAME"));

			printSvfOutDetal(svf, "POINT1_1", rs.getString("SEM1_INTER_REC"), rs.getString("SEM1_INTER_ATTEND") );
			printSvfOutDetal(svf, "POINT1_2", rs.getString("SEM1_TERM_REC") , rs.getString("SEM1_TERM_ATTEND") );
			printSvfOutDetal(svf, "AVERAGE1", rs.getString("SEM1_REC")      , null );

			printSvfOutDetal(svf, "POINT2_1", rs.getString("SEM2_INTER_REC"), rs.getString("SEM2_INTER_ATTEND") );
			printSvfOutDetal(svf, "POINT2_2", rs.getString("SEM2_TERM_REC") , rs.getString("SEM2_TERM_ATTEND") );
			printSvfOutDetal(svf, "AVERAGE2", rs.getString("SEM2_REC")      , null);

			printSvfOutDetal(svf, "POINT3_2", rs.getString("SEM3_TERM_REC") , rs.getString("SEM3_TERM_ATTEND"));
			printSvfOutDetal(svf, "AVERAGE4", rs.getString("GRADE_RECORD")  , null);

			if( isABCHyoutei )
				printSvfOutDetal(svf, "WORD"    , null, rs.getString("ASSESSMARK") );//---NO001

			if ("1".equals(param[6])) {
                printSvfOutDetal( svf, "PROV_FLG",   rs.getString("PROV_FLG"), null);
            }
			printSvfOutDetal(svf, "TYPE_A"  , null, rs.getString("ASSES_LEVEL_A") );
			printSvfOutDetal(svf, "TYPE_B"  , null, rs.getString("ASSES_LEVEL_B") );
			printSvfOutDetal(svf, "TYPE_C"  , null, rs.getString("ASSES_LEVEL_C") );

			ret = svf.VrEndRecord();
			if( ret == 0 )nonedata = true;
		} catch( SQLException ex ){
			log.error("[KNJD120K]printSvfOutMeisai error!", ex );
		}

		return nonedata;

	}//printSvfOutMeisai()の括り



	/** 
     *   ＨＲ成績合計・平均・最高点・最低点を出力 => VrEndRecord()
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
	private void printSvfOutTotal(Vrw32alp svf, ResultSet rs, String param[]){


		try {
			svf.VrsOut("ITEM" , "合計");
			printSvfOutDetal(svf, "TOTAL1_1"		 , rs.getString("SUM_INTER_1")	, null);
			printSvfOutDetal(svf, "TOTAL1_2"		 , rs.getString("SUM_TERM_1")	, null);
			printSvfOutDetal(svf, "TOTAL_AVERAGE1"   , rs.getString("SUM_1")		, null);
			printSvfOutDetal(svf, "TOTAL2_1"		 , rs.getString("SUM_INTER_2")	, null);
			printSvfOutDetal(svf, "TOTAL2_2"		 , rs.getString("SUM_TERM_2")	, null);
			printSvfOutDetal(svf, "TOTAL_AVERAGE2"   , rs.getString("SUM_2")		, null);
			printSvfOutDetal(svf, "TOTAL3_2"		 , rs.getString("SUM_TERM_3")	, null);
			if( !isABCHyoutei ) //---NO001
				printSvfOutDetal(svf, "TOTAL_AVERAGE4"   , rs.getString("SUM_GRADE")	, null);
			printSvfOutDetal(svf, "TOTAL_TYPE_A"     , null  , rs.getString("SUM_ASSES_LEVEL_A") );
			printSvfOutDetal(svf, "TOTAL_TYPE_B"     , null  , rs.getString("SUM_ASSES_LEVEL_B") );
			printSvfOutDetal(svf, "TOTAL_TYPE_C"     , null  , rs.getString("SUM_ASSES_LEVEL_C") );
			svf.VrEndRecord();

			svf.VrsOut("ITEM" , "平均");
            dmf1.applyPattern("#");
			printSvfOutfloat(svf, "TOTAL1_1"		 , rs.getString("AVG_INTER_1")	, null);
			printSvfOutfloat(svf, "TOTAL1_2"		 , rs.getString("AVG_TERM_1")	, null);
			printSvfOutfloat(svf, "TOTAL_AVERAGE1"   , rs.getString("AVG_1")		, null);
			printSvfOutfloat(svf, "TOTAL2_1"		 , rs.getString("AVG_INTER_2")	, null);
			printSvfOutfloat(svf, "TOTAL2_2"		 , rs.getString("AVG_TERM_2")	, null);
			printSvfOutfloat(svf, "TOTAL_AVERAGE2"   , rs.getString("AVG_2")		, null);
			printSvfOutfloat(svf, "TOTAL3_2"		 , rs.getString("AVG_TERM_3")	, null);
			if( !isABCHyoutei ) //---NO001
				printSvfOutfloat(svf, "TOTAL_AVERAGE4"   , rs.getString("AVG_GRADE")	, null);
            dmf1.applyPattern("#.##");
			printSvfOutfloat(svf, "TOTAL_TYPE_A"     , null  , rs.getString("AVG_ASSES_LEVEL_A") );
			printSvfOutfloat(svf, "TOTAL_TYPE_B"     , null  , rs.getString("AVG_ASSES_LEVEL_B") );
			printSvfOutfloat(svf, "TOTAL_TYPE_C"     , null  , rs.getString("AVG_ASSES_LEVEL_C") );
			svf.VrEndRecord();

			svf.VrsOut("ITEM" , "最高点");
			printSvfOutDetal(svf, "TOTAL1_1"		 , rs.getString("MAX_INTER_1")	, null);
			printSvfOutDetal(svf, "TOTAL1_2"		 , rs.getString("MAX_TERM_1")	, null);
			printSvfOutDetal(svf, "TOTAL_AVERAGE1"   , rs.getString("MAX_1")		, null);
			printSvfOutDetal(svf, "TOTAL2_1"		 , rs.getString("MAX_INTER_2")	, null);
			printSvfOutDetal(svf, "TOTAL2_2"		 , rs.getString("MAX_TERM_2")	, null);
			printSvfOutDetal(svf, "TOTAL_AVERAGE2"   , rs.getString("MAX_2")		, null);
			printSvfOutDetal(svf, "TOTAL3_2"		 , rs.getString("MAX_TERM_3")	, null);
			if( !isABCHyoutei ) //---NO001
				printSvfOutDetal(svf, "TOTAL_AVERAGE4"   , rs.getString("MAX_GRADE")	, null);
			printSvfOutDetal(svf, "TOTAL_TYPE_A"     , null  , rs.getString("MAX_ASSES_LEVEL_A") );
			printSvfOutDetal(svf, "TOTAL_TYPE_B"     , null  , rs.getString("MAX_ASSES_LEVEL_B") );
			printSvfOutDetal(svf, "TOTAL_TYPE_C"     , null  , rs.getString("MAX_ASSES_LEVEL_C") );
			svf.VrEndRecord();

			svf.VrsOut("ITEM" , "最低点");
			printSvfOutDetal(svf, "TOTAL1_1"		 , rs.getString("MIN_INTER_1")	, null);
			printSvfOutDetal(svf, "TOTAL1_2"		 , rs.getString("MIN_TERM_1")	, null);
			printSvfOutDetal(svf, "TOTAL_AVERAGE1"   , rs.getString("MIN_1")		, null);
			printSvfOutDetal(svf, "TOTAL2_1"		 , rs.getString("MIN_INTER_2")	, null);
			printSvfOutDetal(svf, "TOTAL2_2"		 , rs.getString("MIN_TERM_2")	, null);
			printSvfOutDetal(svf, "TOTAL_AVERAGE2"   , rs.getString("MIN_2")		, null);
			printSvfOutDetal(svf, "TOTAL3_2"		 , rs.getString("MIN_TERM_3")	, null);
			if( !isABCHyoutei ) //---NO001
				printSvfOutDetal(svf, "TOTAL_AVERAGE4"   , rs.getString("MIN_GRADE")	, null);
			printSvfOutDetal(svf, "TOTAL_TYPE_A"     , null  , rs.getString("MIN_ASSES_LEVEL_A") );
			printSvfOutDetal(svf, "TOTAL_TYPE_B"     , null  , rs.getString("MIN_ASSES_LEVEL_B") );
			printSvfOutDetal(svf, "TOTAL_TYPE_C"     , null  , rs.getString("MIN_ASSES_LEVEL_C") );
			svf.VrEndRecord();
		} catch( SQLException ex ){
			log.error("[KNJD120K]printSvfOutTotal error!", ex );
		}

	}//printSvfOutTotal()の括り



	/** 
     *   ＳＶＦＲｅｃｏｒｄ出力
     *     Strnig svffieldname => 出力SVFフィールド
     *     String data1 => data2がnullなら成績データが入っている
     *     String data2 => 成績データまたは出欠のデータが入っている
     *                     成績データと出欠のデータが入っている場合は、繋いで出力
     */
	private void printSvfOutDetal(Vrw32alp svf, String svffieldname, String data1, String data2){

		try {
			if( data1 != null  &&  data2 == null )
                svf.VrAttribute(svffieldname , "Hensyu=1");		//右詰め
            else
				svf.VrAttribute(svffieldname , "Hensyu=3");		//中央割付

            if( data1 != null  &&  data2 != null )
				svf.VrsOut(svffieldname , data2 + ("   " + data1).substring( data1.length(), data1.length() + 3 ) );
			else if( data2 != null )
				svf.VrsOut(svffieldname , data2);
			else if( data1 != null )
				svf.VrsOut(svffieldname , data1);

		} catch( Exception ex ){
			log.error("[KNJD120K]printSvfOutDetal error!", ex );
		}

	}//printSvfOutDetal()の括り



	/** 
     *   ＳＶＦＲｅｃｏｒｄ出力  formatして小数点を出力
     *     Strnig svffieldname => 出力SVFフィールド
     *     String data1 => nullでないなら成績データが入っている
     *     String data2 => nullでないなら成績データが入っている
     */
	private void printSvfOutfloat(Vrw32alp svf, String svffieldname, String data1, String data2){

        if( data1 == null  &&  data2 == null ) return;

		try {
			if( data1 != null  &&  data2 == null )
                svf.VrAttribute(svffieldname , "Hensyu=1");		//右詰め
            else
				svf.VrAttribute(svffieldname , "Hensyu=3");		//中央割付

            if( data2 != null )
				svf.VrsOut( svffieldname , dmf1.format(Float.parseFloat(data2)) );
			else if( data1 != null )
				svf.VrsOut(svffieldname , dmf1.format(Float.parseFloat(data1)) );

		} catch( Exception ex ){
			log.error("[KNJD120K]printSvfOutfloat error!", ex );
		}

	}//printSvfOutfloat()の括り



	/** 
     *      
     *   SQLStatement作成 成績データ
     *     int pdiv : 0=>生徒別  1=>合計 
     *      
     */
//NO002
//	String prestatementRecord(String param[],int pdiv) {
	String prestatementRecord(String param[],int pdiv,String grclcd) {

		StringBuffer stb = new StringBuffer();
		try {
			if( pdiv==0 ){
	            if ("1".equals(param[6])) {
	                stb.append(" WITH RECORD_PROV_FLG AS ( ");
	                stb.append("     SELECT  SCHREGNO, PROV_FLG ");
	                stb.append("     FROM   RECORD_PROV_FLG_DAT ");
	                stb.append("     WHERE  YEAR = '" + param[0] + "' ");
	                //教育課程対応
	                if ("1".equals(param[4])) {
	                    stb.append(            "AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' ");
	                } else {
	                    stb.append(            "AND SUBCLASSCD = '" + param[3] + "' ");
	                }
	                stb.append("     ) ");
	            }
				stb.append("SELECT S1.HR_NAMEABBV,S1.ATTENDNO,S1.NAME,");
				stb.append(       "S1.GRADE||S1.HR_CLASS AS GR_CL,");//NO002
				stb.append(       "SEM1_INTER_REC,SEM1_TERM_REC,SEM1_REC,");
				stb.append(       "SEM2_INTER_REC,SEM2_TERM_REC,SEM2_REC,");
				stb.append(       "SEM3_TERM_REC,GRADE_RECORD,");
		        stb.append(       "CASE VALUE(SEM1_INTER_REC_DI,'0') WHEN 'KK' THEN '公' WHEN 'KS' THEN '欠' ELSE NULL END AS SEM1_INTER_ATTEND,");
		        stb.append(       "CASE VALUE(SEM1_TERM_REC_DI, '0') WHEN 'KK' THEN '公' WHEN 'KS' THEN '欠' ELSE NULL END AS SEM1_TERM_ATTEND,");
		        stb.append(       "CASE VALUE(SEM2_INTER_REC_DI,'0') WHEN 'KK' THEN '公' WHEN 'KS' THEN '欠' ELSE NULL END AS SEM2_INTER_ATTEND,");
		        stb.append(       "CASE VALUE(SEM2_TERM_REC_DI, '0') WHEN 'KK' THEN '公' WHEN 'KS' THEN '欠' ELSE NULL END AS SEM2_TERM_ATTEND,");
		        stb.append(       "CASE VALUE(SEM3_TERM_REC_DI, '0') WHEN 'KK' THEN '公' WHEN 'KS' THEN '欠' ELSE NULL END AS SEM3_TERM_ATTEND,");
				if( isABCHyoutei )
					stb.append(   "ASSESSMARK,");//---NO001
	            if ("1".equals(param[6])) {
	                stb.append(        "CASE WHEN P0.PROV_FLG = '1' THEN 'レ' END AS PROV_FLG, ");
	            }
                stb.append(       "A_PATTERN_ASSESS AS ASSES_LEVEL_A,");
                stb.append(       "B_PATTERN_ASSESS AS ASSES_LEVEL_B,");
                stb.append(       "C_PATTERN_ASSESS AS ASSES_LEVEL_C ");
			} else{
				stb.append("SELECT SUM(SEM1_INTER_REC)AS SUM_INTER_1,");
				stb.append(		  "SUM(SEM1_TERM_REC) AS SUM_TERM_1,");
				stb.append(		  "SUM(SEM1_REC)      AS SUM_1,");
				stb.append(       "SUM(SEM2_INTER_REC)AS SUM_INTER_2,");
				stb.append(		  "SUM(SEM2_TERM_REC) AS SUM_TERM_2,");
				stb.append(		  "SUM(SEM2_REC)      AS SUM_2,");
				stb.append(       "SUM(SEM3_TERM_REC) AS SUM_TERM_3,");
				stb.append(		  "SUM(GRADE_RECORD)  AS SUM_GRADE,");
                stb.append(       "SUM(INT(A_PATTERN_ASSESS)) AS SUM_ASSES_LEVEL_A,");
                stb.append(       "SUM(INT(B_PATTERN_ASSESS)) AS SUM_ASSES_LEVEL_B,");
                stb.append(       "SUM(INT(C_PATTERN_ASSESS)) AS SUM_ASSES_LEVEL_C, ");

				stb.append(		  "ROUND(AVG(FLOAT(SEM1_INTER_REC)),0)AS AVG_INTER_1,");
				stb.append(		  "ROUND(AVG(FLOAT(SEM1_TERM_REC)),0) AS AVG_TERM_1,");
				stb.append(		  "ROUND(AVG(FLOAT(SEM1_REC)),0)      AS AVG_1,");
				stb.append(		  "ROUND(AVG(FLOAT(SEM2_INTER_REC)),0)AS AVG_INTER_2,");
				stb.append(		  "ROUND(AVG(FLOAT(SEM2_TERM_REC)),0) AS AVG_TERM_2,");
				stb.append(		  "ROUND(AVG(FLOAT(SEM2_REC)),0)      AS AVG_2,");
				stb.append(		  "ROUND(AVG(FLOAT(SEM3_TERM_REC)),0) AS AVG_TERM_3,");
				stb.append(		  "ROUND(AVG(FLOAT(GRADE_RECORD)),0)  AS AVG_GRADE,");
                stb.append(       "ROUND(AVG(FLOAT(INT(A_PATTERN_ASSESS)))*100,0)/100 AS AVG_ASSES_LEVEL_A,");
                stb.append(       "ROUND(AVG(FLOAT(INT(B_PATTERN_ASSESS)))*100,0)/100 AS AVG_ASSES_LEVEL_B,");
                stb.append(       "ROUND(AVG(FLOAT(INT(C_PATTERN_ASSESS)))*100,0)/100 AS AVG_ASSES_LEVEL_C,");

				stb.append(		  "MAX(SEM1_INTER_REC)AS MAX_INTER_1,");
				stb.append(		  "MAX(SEM1_TERM_REC) AS MAX_TERM_1,");
				stb.append(		  "MAX(SEM1_REC)      AS MAX_1,");
				stb.append(		  "MAX(SEM2_INTER_REC)AS MAX_INTER_2,");
				stb.append(		  "MAX(SEM2_TERM_REC) AS MAX_TERM_2,");
				stb.append(		  "MAX(SEM2_REC)      AS MAX_2,");
				stb.append(		  "MAX(SEM3_TERM_REC) AS MAX_TERM_3,");
				stb.append(		  "MAX(GRADE_RECORD)  AS MAX_GRADE,");
                stb.append(       "MAX(INT(A_PATTERN_ASSESS)) AS MAX_ASSES_LEVEL_A,");
                stb.append(       "MAX(INT(B_PATTERN_ASSESS)) AS MAX_ASSES_LEVEL_B,");
                stb.append(       "MAX(INT(C_PATTERN_ASSESS)) AS MAX_ASSES_LEVEL_C,");

				stb.append(		  "MIN(SEM1_INTER_REC)AS MIN_INTER_1,");
				stb.append(		  "MIN(SEM1_TERM_REC) AS MIN_TERM_1,");
				stb.append(		  "MIN(SEM1_REC)      AS MIN_1,");
				stb.append(		  "MIN(SEM2_INTER_REC)AS MIN_INTER_2,");
				stb.append(		  "MIN(SEM2_TERM_REC) AS MIN_TERM_2,");
				stb.append(		  "MIN(SEM2_REC)      AS MIN_2,");
				stb.append(		  "MIN(SEM3_TERM_REC) AS MIN_TERM_3,");
				stb.append(		  "MIN(GRADE_RECORD)  AS MIN_GRADE,");
                stb.append(       "MIN(INT(A_PATTERN_ASSESS)) AS MIN_ASSES_LEVEL_A,");
                stb.append(       "MIN(INT(B_PATTERN_ASSESS)) AS MIN_ASSES_LEVEL_B,");
                stb.append(       "MIN(INT(C_PATTERN_ASSESS)) AS MIN_ASSES_LEVEL_C ");
			}
            stb.append(    "FROM  (SELECT T1.SCHREGNO,T3.NAME,T4.HR_NAMEABBV,T2.GRADE,T2.HR_CLASS,T2.ATTENDNO ");
            stb.append(		      "FROM  (SELECT SCHREGNO ");
            stb.append(                  "FROM   CHAIR_STD_DAT S1, ");
            stb.append(                         "CHAIR_DAT S2 ");                             // 04/12/13Add
            stb.append(  	   		     "WHERE  S1.YEAR = '" + param[0] + "' AND ");
            //stb.append(                         "S1.SEMESTER <= '" + param[1] + "' AND ");
            stb.append(						    "S1.CHAIRCD = '" + param[2] + "' AND ");
            if ("1".equals(param[4])) {
                stb.append(                     "S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || ");
            }
            stb.append(                         "S2.SUBCLASSCD = '" + param[3] + "' AND ");   // 04/12/13Add
            stb.append(                         "S2.YEAR = S1.YEAR AND ");                    // 04/12/13Add
            stb.append(                         "S2.SEMESTER = S1.SEMESTER AND ");            // 04/12/13Add
            stb.append(                         "S2.CHAIRCD = S1.CHAIRCD ");                  // 04/12/13Add
            stb.append(	   			     "GROUP BY SCHREGNO)T1,");
            stb.append(      		     "SCHREG_REGD_DAT T2,");
            stb.append(	  			     "SCHREG_BASE_MST T3,");
            stb.append(      		     "SCHREG_REGD_HDAT T4 ");
            stb.append(		      "WHERE  T2.YEAR = '" + param[0] + "' AND ");
            stb.append(                  "T2.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(	   			     "T2.SEMESTER = (SELECT MAX(SEMESTER) ");
            stb.append(                                 "FROM   SCHREG_REGD_DAT W2 ");
            stb.append(				    		        "WHERE  W2.YEAR='"+param[0]+"' AND ");
            stb.append(								           "W2.SEMESTER <= '" + param[1] + "' AND ");
            stb.append(									       "W2.SCHREGNO = T2.SCHREGNO) AND ");
            //NO002
			if( !grclcd.equals("0") ){
                stb.append(	   			 "T2.GRADE||T2.HR_CLASS = '" + grclcd + "' AND ");
			}
            stb.append(	   			     "T2.SCHREGNO = T3.SCHREGNO AND T4.YEAR = '" + param[0] + "' AND ");
            stb.append(	   			     "T4.SEMESTER = T2.SEMESTER AND T4.GRADE = T2.GRADE AND ");
            stb.append(				     "T4.HR_CLASS = T2.HR_CLASS)S1 ");
            stb.append(		      "LEFT JOIN KIN_RECORD_DAT S2 ON S2.YEAR = '" + param[0] + "' AND ");
            // 04/12/13delete stb.append(                                          "S2.CHAIRCD = '" + param[2] + "' AND ");
            if ("1".equals(param[4])) {
                stb.append(                     "S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || ");
            }
            stb.append(                                          "S2.SUBCLASSCD = '" + param[3] + "' AND "); // 04/12/13Modify
            stb.append(                                          "S2.SCHREGNO = S1.SCHREGNO ");
            //---NO001
			if( pdiv==0 && isABCHyoutei ){
                stb.append(		  "LEFT JOIN RELATIVEASSESS_MST S3 ON S3.GRADE = S1.GRADE AND ");
                if ("1".equals(param[4])) {
                    stb.append(                     "S3.CLASSCD || '-' || S3.SCHOOL_KIND || '-' || S3.CURRICULUM_CD || '-' || ");
                }
                stb.append(                                          "S3.SUBCLASSCD = '" + param[3] + "' AND ");
                stb.append(                                          "S3.ASSESSCD = '3' AND ");
                stb.append(                                          "S3.ASSESSLOW <= S2.GRADE_RECORD AND ");
                stb.append(                                          "S2.GRADE_RECORD <= S3.ASSESSHIGH ");
			}
			if( pdiv==0 ){
	            /* 仮評定情報 */
	            //仮評定フラグ対応
	            if ("1".equals(param[6])) {
	                stb.append(        " LEFT JOIN RECORD_PROV_FLG P0 ON P0.SCHREGNO = S1.SCHREGNO ");
	            }
			}
			if( pdiv==0 )
				stb.append("ORDER BY S1.GRADE,S1.HR_CLASS,S1.ATTENDNO");
		} catch( Exception ex ){
			log.error("[KNJD120K]prestatementRecord error!", ex );
		}
//log.debug("[KNJD120K]prestatementRecord = "+stb.toString());

		return stb.toString();

	}//prestatementRecord()の括り


	/** 
     *  SQLStatement作成 科目名 
     *     2004/12/13 講座名称と科目名称の取得を分割
     **/
	String prestatementHeadSubclass(String param[]) {

		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT SUBCLASSNAME ");
			stb.append("FROM   SUBCLASS_MST ");
            stb.append("WHERE  ");
			if ("1".equals(param[4])) {
                stb.append(" CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ='"+param[3]+"' ");
			} else {
			    stb.append(" SUBCLASSCD ='"+param[3]+"' ");
			}
		} catch( Exception ex ){
			log.error("[KNJD120K]prestatementHead error!", ex );
		}
		return stb.toString();

	}//prestatementHead()の括り


	/** 
     *  SQLStatement作成 講座名 
     *     2004/12/13 講座名称と科目名称の取得を分割
     **/
	String prestatementHeadChair(String param[]) {

		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT DISTINCT CHAIRNAME ");
			stb.append("FROM   CHAIR_DAT ");
			stb.append("WHERE  YEAR = '" + param[0] + "' AND ");
			stb.append(		  "CHAIRCD = '" + param[2] + "' AND ");
            if ("1".equals(param[4])) {
                stb.append("  CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' ");
            } else {
                stb.append("  SUBCLASSCD = '" + param[3] + "' ");
            }
		} catch( Exception ex ){
			log.error("[KNJD120K]prestatementHead error!", ex );
		}
		return stb.toString();

	}//prestatementHead()の括り


    String getABCHyoutei(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" SELECT ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param[0] + "' ");
            stb.append("     AND NAMECD1 = 'D065' ");
        } catch( Exception ex ){
            log.error("[KNJD120K]getABCHyoutei error!", ex );
        }
        return stb.toString();
    }



}//クラスの括り
