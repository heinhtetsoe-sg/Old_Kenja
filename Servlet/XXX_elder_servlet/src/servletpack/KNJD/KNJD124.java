/**
 *
 *	学校教育システム 賢者 [成績管理] 成績チエックリスト（大宮）
 *
 *	2005/05/30 nakamoto 新規作成 KNJD123を土台に作成
 *                      出力項目：前期・後期(中間・期末・平均・評価)、学年(平均・評価)
 *                      各平均は、計算して表示
 *  2005/10/06 nakamoto ・定期考査の出欠の表２のSQL不具合修正
 *  2007/09/12 nakamoto 成績の表SQLを修正した。RECORD_DAT.CHAIRCDはリンクしないようにした
 */

package servletpack.KNJD;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.text.DecimalFormat;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJDefineCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJD124 {

    private static final Log log = LogFactory.getLog(KNJD124.class);
	private DecimalFormat dmf1 = new DecimalFormat("00");
	private DecimalFormat dmf2 = new DecimalFormat("#.#");
	private KNJDefineCode definecode;		//各学校における定数等設定

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
	    
		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[7];

	//	パラメータの取得
		//String printname = request.getParameter("PRINTNAME");   			//プリンタ名
		String printname = null;											//PDFで出力用！！
		try {
log.debug("YEAR="+request.getParameter("YEAR"));
log.debug("SEMESTER="+request.getParameter("SEMESTER"));
log.debug("CHAIRCD="+request.getParameter("CHAIRCD"));
log.debug("SUBCLASSCD="+request.getParameter("SUBCLASSCD"));
	        param[0] = request.getParameter("YEAR");         				//年度
			param[1] = request.getParameter("SEMESTER");   					//1-3:学期
			param[2] = request.getParameter("CHAIRCD");  					//講座コード
			param[3] = request.getParameter("SUBCLASSCD");					//科目コード  04/12/13Add
			param[4] = request.getParameter("STAFF");						//ログイン者コード
			param[5] = request.getParameter("useCurriculumcd");             //教育課程コード
            param[6] = request.getParameter("useProvFlg"); // プロパティ：仮評定の追加対応
		} catch( Exception ex ) {
			log.error("[KNJD124]parameter error!", ex);
		}

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
			log.error("[KNJD124]DB2 open error!", ex);
		}

	//	ＳＶＦ作成処理
		boolean nonedata = false;
		setHead(db2,svf,param);			//見出し項目
		if( printSvfMain(db2,svf,param) )nonedata = true;		//SVF-FORM出力処理

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

        final String form = "1".equals(param[6]) ? "KNJD124_P.frm" : "KNJD124.frm";
        svf.VrSetForm(form, 4);

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		svf.VrsOut("NENDO",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");

	//	作成日(現在処理日)
		try {
			returnval = getinfo.Control(db2);
			svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));
		} catch( Exception ex ){
			log.error("[KNJD124]ReturnVal setHead() get TODAY error!", ex );
		}

	//	科目名
		String ChairCredit = "" ;
		ResultSet rs = null;
		try {
			db2.query( prestatementHeadSubclass(param) );
			rs = db2.getResultSet();
			if( rs.next() ){
				ChairCredit = rs.getString("SUBCLASSNAME");
log.debug("kamokuset"+String.valueOf(ChairCredit));
			}
			rs.close();
		} catch( Exception ex ){
			log.error("[KNJD124]setHead_ hrclass_staff error!", ex );
		}

	//	単位数 
		try {
			db2.query( prestatementHeadCredit(param) );
			rs = db2.getResultSet();
			if( rs.next() ){

				if (rs.getString("MAXCREDITS").equalsIgnoreCase(rs.getString("MINCREDITS"))){
					ChairCredit = ChairCredit + "(" + rs.getString("MAXCREDITS") + "単位)";
log.debug("1kaime"+String.valueOf(ChairCredit));
				}else {
					ChairCredit = ChairCredit + "(" + rs.getString("MINCREDITS") + " \uFF5E ";
					ChairCredit = ChairCredit + rs.getString("MAXCREDITS") + "単位)";
log.debug("2kaime"+String.valueOf(ChairCredit));
				}
				svf.VrsOut("SUBCLASS",String.valueOf(ChairCredit));

			}else {
				svf.VrsOut("SUBCLASS",String.valueOf(ChairCredit) + "(　単位)");
			}
			rs.close();
		} catch( Exception ex ){
			log.error("[KNJD124]setHead_ hrclass_staff error!", ex );
		}

	//	講座名 
		try {
			db2.query( prestatementHeadChair(param) );
			rs = db2.getResultSet();
			if( rs.next() ){
				svf.VrsOut("HR_CLASS",rs.getString("CHAIRNAME"));
			}
			rs.close();
		} catch( Exception ex ){
			log.error("[KNJD124]setHead_ hrclass_staff error!", ex );
		}

	//	担当者名
		String Staffcd = "";
		try {
			db2.query( prestatementHeadStaff(param) );
			rs = db2.getResultSet();
			if( rs.next() ){
				Staffcd = String.valueOf(param[4]);
				svf.VrsOut("STAFFNAME1",rs.getString("STAFFNAME"));
			}
			rs.close();
		} catch( Exception ex ){
			log.error("[KNJD124]setHead_ hrclass_staff error!", ex );
		}

	//	担当者名2
		try {
			int staffcnt = 0 ;
			String Staffname = "" ;
			db2.query( prestatementHeadStaff2(param) );
			rs = db2.getResultSet();
			while( rs.next() ){
log.debug(rs.getString("STAFFCD")+rs.getString("STAFFNAME"));
				if (staffcnt == 2) break;
				if (!Staffcd.equalsIgnoreCase(rs.getString("STAFFCD"))){
					if (staffcnt == 1) {
						Staffname = Staffname + "," + rs.getString("STAFFNAME");
					}else {
						Staffname = rs.getString("STAFFNAME");
					}
					staffcnt++;
				}
			}
			svf.VrsOut("STAFFNAME2",String.valueOf(Staffname));
			rs.close();
		} catch( Exception ex ){
			log.error("[KNJD124]setHead_ hrclass_staff error!", ex );
		}

		getinfo = null;
		returnval = null;

	}//setHead()の括り



	/** SVF-FORM メイン出力処理 **/
	private boolean printSvfMain(DB2UDB db2,Vrw32alp svf,String param[])
	{
for(int i = 0 ; i < param.length ; i++ )log.debug("param[" + i + "] = " + param[i] );
		//定義
		boolean nonedata = false;
		ResultSet rs = null;
		float total[][] = {{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0}};   //合計 M001
		Map attendmap = getAttendMarkMap( db2, param );		//出欠記号の取得

		//RecordSet作成
		try {
			db2.query( prestatementRecord(param,0) );			//生徒別成績データ
			rs = db2.getResultSet();
			int linex = 0;								        //１ページ当り出力行数
			while( rs.next() ){
				if( printSvfOutMeisai( svf, rs, param, total, attendmap ) ){	//SVF-FORMへ出力---NO001
					nonedata = true;
					linex++;
				}
			}
			if( linex < 50 && nonedata ){                       //明細行は５０行まで->足りない場合は空行を出力！
				for( ; linex < 50 ; linex++ ){
					svf.VrAttribute( "NAME", "Meido=100" );
					svf.VrsOut( "NAME", " . " );
					svf.VrEndRecord();
				}
			}
			if( nonedata ) printSvfOutTotal( svf, total );						//SVF-FORMへ出力 M001
		} catch( Exception ex ) { log.error("[KNJD124]printSvfMain read error! ", ex);	}

		return nonedata;

	}//boolean printSvfMain()の括り



	/** 
     *   ＨＲ成績生徒別明細を出力 => VrEndRecord()
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
	private boolean printSvfOutMeisai( Vrw32alp svf, ResultSet rs, String param[], float total[][], Map attendmap ){

		boolean nonedata = false;
		try {
			int ret = 0;
			ret = svf.VrsOut( "ATTENDNO",  rs.getString("HR_NAMEABBV") + "-" + dmf1.format(rs.getInt("ATTENDNO")) );  //クラス−出席番号
			ret = svf.VrsOut( "NAME",      rs.getString("NAME"));                                                     //名前

			printSvfOutDetal( svf, "POINT1_1",   rs.getString("SEM1_INTR_SCORE"), getTestAttend( rs.getString("SEM1_INTR_ATTEND"), attendmap ) ); //前期中間素点
			printSvfOutDetal( svf, "POINT1_2",   rs.getString("SEM1_TERM_SCORE"), getTestAttend( rs.getString("SEM1_TERM_ATTEND"), attendmap ) ); //前期期末素点
			printSvfOutDetal( svf, "AVERAGE1",   getScoreAverage( rs, 1 ),        null );   //前期平均
			printSvfOutDetal( svf, "GRADING1",   rs.getString("SEM1_VALUE"),      null );   //前期評価

			printSvfOutDetal( svf, "POINT2_1",   rs.getString("SEM2_INTR_SCORE"), getTestAttend( rs.getString("SEM2_INTR_ATTEND"), attendmap ) ); //後期中間素点
			printSvfOutDetal( svf, "POINT2_2",   rs.getString("SEM2_TERM_SCORE"), getTestAttend( rs.getString("SEM2_TERM_ATTEND"), attendmap ) ); //後期期末素点
			printSvfOutDetal( svf, "AVERAGE2",   getScoreAverage( rs, 2 ),        null );   //後期平均
			printSvfOutDetal( svf, "GRADING2",   rs.getString("SEM2_VALUE"),      null );   //後期評価

            if ("1".equals(param[6])) {
                printSvfOutDetal( svf, "PROV_FLG",   rs.getString("PROV_FLG"), null);
            }
			printSvfOutDetal( svf, "AVERAGE4",   getScoreAverage( rs, 4 ),        null );   //学年平均
			printSvfOutDetal( svf, "GRADING4",   rs.getString("GRAD_VALUE"),      null );   //学年評定

			//平均・最高点・最高点の累積および保存処理
			int i = 0;
			accumMeisai( total, i++, rs.getString("SEM1_INTR_SCORE") );
			accumMeisai( total, i++, rs.getString("SEM1_TERM_SCORE") );
			accumMeisai( total, i++, getScoreAverage( rs, 1 ) );
			accumMeisai( total, i++, rs.getString("SEM1_VALUE") );
			accumMeisai( total, i++, rs.getString("SEM2_INTR_SCORE") );
			accumMeisai( total, i++, rs.getString("SEM2_TERM_SCORE") );
			accumMeisai( total, i++, getScoreAverage( rs, 2 ) );
			accumMeisai( total, i++, rs.getString("SEM2_VALUE") );
			accumMeisai( total, i++, getScoreAverage( rs, 4 ) );
			accumMeisai( total, i++, rs.getString("GRAD_VALUE") );

			ret = svf.VrEndRecord();
			if( ret == 0 )nonedata = true;
		} catch( SQLException ex ){
			log.error("[KNJD124]printSvfOutMeisai error!", ex );
		}

		return nonedata;

	}//printSvfOutMeisai()の括り


	/** 
     *   試験日の出欠の編集
     */
	private String getTestAttend( String attend, Map attendmap )
	{
		String retattend = null;
		try {
			if( attend != null ){
				retattend = (String)attendmap.get( attend );
			}
		} catch( Exception ex ){
			log.error("error! ", ex );
		}
		return retattend;
	}


	/** 
     *   生徒毎の各平均の計算処理
     *
     *   avg_type:前期平均(1)、後期平均(2)、学年平均(4)
     */
	private String getScoreAverage( ResultSet rs, int avg_type )
	{
		String ret_avg = null;
        int avg_cnt = 0;
        int kei = 0;
		try {
    		String str1 = rs.getString("SEM1_INTR_SCORE");  //前期中間素点
    		String str2 = rs.getString("SEM1_TERM_SCORE");  //前期期末素点
    		String str3 = rs.getString("SEM2_INTR_SCORE");  //後期中間素点
    		String str4 = rs.getString("SEM2_TERM_SCORE");  //後期期末素点

			if( (str2 != null && avg_type == 1) || (str4 != null && avg_type != 1) ){
    			if( str1 != null && avg_type != 2 ){
                    kei += Integer.parseInt( str1 );
                    avg_cnt++;
    			}
    			if( str2 != null && avg_type != 2 ){
                    kei += Integer.parseInt( str2 );
                    avg_cnt++;
    			}
    			if( str3 != null && avg_type != 1 ){
                    kei += Integer.parseInt( str3 );
                    avg_cnt++;
    			}
    			if( str4 != null && avg_type != 1 ){
                    kei += Integer.parseInt( str4 );
                    avg_cnt++;
    			}
                ret_avg = String.valueOf( dmf2.format( (float)Math.round( (float)kei / (float)avg_cnt * 10 ) / 10 ) );
			}
		} catch( Exception ex ){
			log.error("getScoreAverage error! ", ex );
		}
		return ret_avg;
	}


	/** 
     *   平均・最高点・最高点の累積および保存処理
     */
	private void accumMeisai( float total[][], int i, String str )
	{
		try {
			if( str != null ){
				total[0][i] += Float.parseFloat( str );
				total[1][i] += 1;
				if( total[2][i] == 0  ||  Float.parseFloat( str ) < total[2][i] ) total[2][i] = Float.parseFloat( str );
				if( total[3][i] < Float.parseFloat( str ) ) total[3][i] = Float.parseFloat( str );
			}
		} catch( Exception ex ){
			log.error("error! ", ex );
		}

	}


	/** 
     *   ＨＲ成績合計・平均・最高点・最低点を出力 => VrEndRecord()
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
	private void printSvfOutTotal( Vrw32alp svf, float total[][] )
	{
		try {
			String fname[] = { "TOTAL1_1", "TOTAL1_2", "TOTAL_AVERAGE1", "TOTAL_GRADING1",
	                           "TOTAL2_1", "TOTAL2_2", "TOTAL_AVERAGE2", "TOTAL_GRADING2",
	                           "TOTAL_AVERAGE4", "TOTAL_GRADING4" };

			printSvfOutTotalDetail( svf, total, fname, 0, "合計" );
			printSvfOutTotalDetail( svf, total, fname, 1, "平均" );
			printSvfOutTotalDetail( svf, total, fname, 3, "最高点" );
			printSvfOutTotalDetail( svf, total, fname, 2, "最低点" );
		} catch( Exception ex ){
			log.error("error! ", ex );
		}
	}


	/** 
     *   合計・平均・最高点・最低点を出力
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
	private void printSvfOutTotalDetail( Vrw32alp svf, float total[][], String fname[], int ti, String title )
	{
		svf.VrsOut( "ITEM" , title );
		for( int i = 0 ; i < fname.length ; i++ ){
			if( 0 < total[ti][i] && ti != 1 )
				printSvfOutDetal( svf, fname[i], String.valueOf( dmf2.format( total[ti][i] ) ), null);
			if( 0 < total[ti][i] && ti == 1 )
				printSvfOutDetal( svf, fname[i], String.valueOf( dmf2.format( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 ) ), null);
		}
		svf.VrEndRecord();

	}


	/** 
     *   ＳＶＦＲｅｃｏｒｄ出力
     *     Strnig svffieldname => 出力SVFフィールド
     *     String data1 => data2がnullなら成績データが入っている
     *     String data2 => 成績データまたは出欠のデータが入っている
     *                     成績データと出欠のデータが入っている場合は、繋いで出力
     */
	private void printSvfOutDetal(Vrw32alp svf, String svffieldname, String data1, String data2){

		try {
//			if( data1 != null  &&  data2 == null )
			if( data1 != null )
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
			log.error("[KNJD124]printSvfOutDetal error!", ex );
		}

	}//printSvfOutDetal()の括り



	/** 
     *      
     *   SQLStatement作成 成績データ
     *     int pdiv : 0=>生徒別  1=>合計 
     *      
     */
	String prestatementRecord(String param[],int pdiv) {

		StringBuffer stb = new StringBuffer();
		try {
			//講座の表
			stb.append("WITH CHAIR_A AS(");
			stb.append(	  "SELECT  SCHREGNO,S1.SEMESTER ");
			stb.append(    "FROM    CHAIR_STD_DAT S1, ");
			stb.append(            "CHAIR_DAT S2 ");
			stb.append(    "WHERE   S1.YEAR = '" + param[0] + "' AND ");
			stb.append(		       "S1.SEMESTER <= '" + param[1] + "' AND ");
			stb.append(            "S1.CHAIRCD = '" + param[2] + "' AND ");
			if ("1".equals(param[5])) {
                stb.append(            "S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || S2.SUBCLASSCD = '" + param[3] + "' AND ");
			} else {
			    stb.append(            "S2.SUBCLASSCD = '" + param[3] + "' AND ");
			}
			stb.append(            "S2.YEAR = S1.YEAR AND ");
			stb.append(            "S2.SEMESTER = S1.SEMESTER AND ");
			stb.append(            "S2.CHAIRCD = S1.CHAIRCD ");
			stb.append(            "GROUP BY SCHREGNO,S1.SEMESTER ");
			stb.append(    ") ");
			
			//在籍の表
			stb.append(",SCHNO AS(");
			stb.append(    "SELECT  T2.SCHREGNO, T3.NAME, T4.HR_NAMEABBV, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
			stb.append(    "FROM    SCHREG_REGD_DAT T2, ");
			stb.append(            "SCHREG_BASE_MST T3, ");
			stb.append(            "SCHREG_REGD_HDAT T4 ");
			stb.append(    "WHERE   T2.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T2.SEMESTER = (SELECT  MAX(SEMESTER) ");
			stb.append(                           "FROM    SCHREG_REGD_DAT W2 ");
			stb.append(            		  		  "WHERE   W2.YEAR = '" + param[0] + "' AND ");
			stb.append(            						  "W2.SEMESTER <= '" + param[1] + "' AND ");
			stb.append(            						  "W2.SCHREGNO = T2.SCHREGNO) AND ");
			stb.append(            "T2.SCHREGNO = T3.SCHREGNO AND T4.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T4.SEMESTER = T2.SEMESTER AND T4.GRADE = T2.GRADE AND ");
			stb.append(            "T4.HR_CLASS = T2.HR_CLASS AND ");
			stb.append(			   "EXISTS( SELECT'X' FROM CHAIR_A T5 WHERE T5.SCHREGNO = T2.SCHREGNO ) ");
			stb.append(	   ") ");

			//定期考査の出欠の表１
			stb.append(",TEST_ATTEND_A AS (");
			stb.append(    "SELECT  '1' AS SEMES, '01' AS TESTKINDCD, T2.SCHREGNO, ");
            stb.append(            "T2.DI_CD ");
            stb.append(    "FROM    SCH_CHR_TEST T1,ATTEND_DAT T2, ATTEND_DI_CD_DAT T3 ");
			stb.append(    "WHERE   T1.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T1.SEMESTER = '1' AND T1.TESTKINDCD = '01' AND T1.TESTITEMCD = '01' AND ");
			stb.append(            "T1.CHAIRCD = '" + param[2] + "' AND ");
            stb.append(            "T3.YEAR = '" + param[0] + "' AND ");
            stb.append(            "T3.DI_CD = T2.DI_CD AND ");
			stb.append(            "T2.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T2.CHAIRCD = '" + param[2] + "' AND ");
			stb.append(            "T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND ");
			stb.append(            "T3.REP_DI_CD IN('1','2','3','4','5','6','8','9','10','11','12','13','14','29','30','31') ");
			stb.append(    "UNION ");
			stb.append(    "SELECT  '1' AS SEMES, '02' AS TESTKINDCD, T2.SCHREGNO, ");
            stb.append(            "T2.DI_CD ");
            stb.append(    "FROM    SCH_CHR_TEST T1,ATTEND_DAT T2, ATTEND_DI_CD_DAT T3 ");
			stb.append(    "WHERE   T1.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T1.SEMESTER = '1' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '01' AND ");
			stb.append(            "T1.CHAIRCD = '" + param[2] + "' AND ");
            stb.append(            "T3.YEAR = '" + param[0] + "' AND ");
            stb.append(            "T3.DI_CD = T2.DI_CD AND ");
			stb.append(            "T2.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T2.CHAIRCD = '" + param[2] + "' AND ");
			stb.append(            "T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND ");
			stb.append(            "T3.REP_DI_CD IN('1','2','3','4','5','6','8','9','10','11','12','13','14','29','30','31') ");
			stb.append(    "UNION ");
			stb.append(    "SELECT  '2' AS SEMES, '01' AS TESTKINDCD, T2.SCHREGNO, ");
            stb.append(            "T2.DI_CD ");
            stb.append(    "FROM    SCH_CHR_TEST T1,ATTEND_DAT T2, ATTEND_DI_CD_DAT T3 ");
			stb.append(    "WHERE   T1.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T1.SEMESTER = '2' AND T1.TESTKINDCD = '01' AND T1.TESTITEMCD = '01' AND ");
			stb.append(            "T1.CHAIRCD = '" + param[2] + "' AND ");
            stb.append(            "T3.YEAR = '" + param[0] + "' AND ");
            stb.append(            "T3.DI_CD = T2.DI_CD AND ");
			stb.append(            "T2.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T2.CHAIRCD = '" + param[2] + "' AND ");
			stb.append(            "T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND ");
			stb.append(            "T3.REP_DI_CD IN('1','2','3','4','5','6','8','9','10','11','12','13','14','29','30','31') ");
			stb.append(    "UNION ");
			stb.append(    "SELECT  '2' AS SEMES, '02' AS TESTKINDCD, T2.SCHREGNO, ");
            stb.append(            "T2.DI_CD ");
            stb.append(    "FROM    SCH_CHR_TEST T1,ATTEND_DAT T2, ATTEND_DI_CD_DAT T3 ");
			stb.append(    "WHERE   T1.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T1.SEMESTER = '2' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '01' AND ");
			stb.append(            "T1.CHAIRCD = '" + param[2] + "' AND ");
            stb.append(            "T3.YEAR = '" + param[0] + "' AND ");
            stb.append(            "T3.DI_CD = T2.DI_CD AND ");
			stb.append(            "T2.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T2.CHAIRCD = '" + param[2] + "' AND ");
			stb.append(            "T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND ");
			stb.append(            "T3.REP_DI_CD IN('1','2','3','4','5','6','8','9','10','11','12','13','14','29','30','31') ");
			stb.append(    "UNION ");
			stb.append(    "SELECT  '3' AS SEMES, '01' AS TESTKINDCD, T2.SCHREGNO, ");
            stb.append(            "T2.DI_CD ");
            stb.append(    "FROM    SCH_CHR_TEST T1,ATTEND_DAT T2, ATTEND_DI_CD_DAT T3 ");
			stb.append(    "WHERE   T1.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T1.SEMESTER = '3' AND T1.TESTKINDCD = '01' AND T1.TESTITEMCD = '01' AND ");
			stb.append(            "T1.CHAIRCD = '" + param[2] + "' AND ");
            stb.append(            "T3.YEAR = '" + param[0] + "' AND ");
            stb.append(            "T3.DI_CD = T2.DI_CD AND ");
			stb.append(            "T2.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T2.CHAIRCD = '" + param[2] + "' AND ");
			stb.append(            "T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND ");
			stb.append(            "T3.REP_DI_CD IN('1','2','3','4','5','6','8','9','10','11','12','13','14','29','30','31') ");
			stb.append(    "UNION ");
			stb.append(    "SELECT  '3' AS SEMES, '02' AS TESTKINDCD, T2.SCHREGNO, ");
            stb.append(            "T2.DI_CD ");
            stb.append(    "FROM    SCH_CHR_TEST T1,ATTEND_DAT T2, ATTEND_DI_CD_DAT T3 ");
			stb.append(    "WHERE   T1.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T1.SEMESTER = '3' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '01' AND ");
			stb.append(            "T1.CHAIRCD = '" + param[2] + "' AND ");
            stb.append(            "T3.YEAR = '" + param[0] + "' AND ");
            stb.append(            "T3.DI_CD = T2.DI_CD AND ");
			stb.append(            "T2.YEAR = '" + param[0] + "' AND ");
			stb.append(            "T2.CHAIRCD = '" + param[2] + "' AND ");
			stb.append(            "T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND ");
			stb.append(            "T3.REP_DI_CD IN('1','2','3','4','5','6','8','9','10','11','12','13','14','29','30','31') ");
			stb.append(	   ") ");

			//定期考査の出欠の表２ 05/10/06
			stb.append(",TEST_ATTEND_B AS (");
			stb.append(    "SELECT  SCHREGNO, ");
			stb.append(            "MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM1_INTR_ATTEND, ");
			stb.append(            "MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM1_TERM_ATTEND, ");
			stb.append(            "MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM2_INTR_ATTEND, ");
			stb.append(            "MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM2_TERM_ATTEND, ");
			stb.append(            "MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM3_INTR_ATTEND, ");
			stb.append(            "MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM3_TERM_ATTEND ");
//			stb.append(            "MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '01' THEN INT(DI_CD) ELSE 0 END) AS SEM1_INTR_ATTEND, ");
//			stb.append(            "MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '02' THEN INT(DI_CD) ELSE 0 END) AS SEM1_TERM_ATTEND, ");
//			stb.append(            "MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '01' THEN INT(DI_CD) ELSE 0 END) AS SEM2_INTR_ATTEND, ");
//			stb.append(            "MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '02' THEN INT(DI_CD) ELSE 0 END) AS SEM2_TERM_ATTEND, ");
//			stb.append(            "MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '01' THEN INT(DI_CD) ELSE 0 END) AS SEM3_INTR_ATTEND, ");
//			stb.append(            "MIN(CASE WHEN SEMES = '3' AND TESTKINDCD = '02' THEN INT(DI_CD) ELSE 0 END) AS SEM3_TERM_ATTEND ");
			stb.append(    "FROM    TEST_ATTEND_A ");
			stb.append(    "GROUP BY SCHREGNO ");
			stb.append(	   ") ");

			//成績の表
			stb.append(",RECORD AS(");
			stb.append(	   "SELECT  T1.SCHREGNO, ");
			stb.append(		       "SEM1_INTR_SCORE, ");
			stb.append(            "SEM1_INTR_VALUE, ");
			stb.append(			   "SEM1_TERM_SCORE, ");
			stb.append(            "SEM1_TERM_VALUE, ");
			stb.append(			   "SEM1_VALUE, ");
			stb.append(			   "SEM2_INTR_SCORE, ");
			stb.append(            "SEM2_INTR_VALUE, ");
			stb.append(			   "SEM2_TERM_SCORE, ");
			stb.append(            "SEM2_TERM_VALUE, ");
			stb.append(			   "SEM2_VALUE, ");
			stb.append(			   "SEM3_INTR_SCORE, ");
			stb.append(            "SEM3_INTR_VALUE, ");
			stb.append(			   "SEM3_TERM_SCORE, ");
			stb.append(            "SEM3_TERM_VALUE, ");
			stb.append(			   "SEM3_VALUE, ");
			stb.append(            "T2.SEM1_INTR_ATTEND, T2.SEM1_TERM_ATTEND, ");
			stb.append(            "T2.SEM2_INTR_ATTEND, T2.SEM2_TERM_ATTEND, ");
			stb.append(            "T2.SEM3_INTR_ATTEND, T2.SEM3_TERM_ATTEND, ");
			stb.append(            "GRAD_VALUE ");
			stb.append(	   "FROM    RECORD_DAT T1 ");
			stb.append(            "LEFT JOIN TEST_ATTEND_B T2 ON T1.SCHREGNO = T2.SCHREGNO ");
			stb.append(	   "WHERE   YEAR = '" + param[0] + "' AND ");
	         if ("1".equals(param[5])) {
	             stb.append(            "CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' AND ");
	         } else {
	             stb.append(			   "SUBCLASSCD = '" + param[3] + "' AND ");
	         }
//			stb.append(			   "CHAIRCD = '" + param[2] + "' ");
			stb.append(			   "T1.SCHREGNO IN(SELECT SCHREGNO  ");
			stb.append(			   "                 FROM CHAIR_STD_DAT  ");
			stb.append(			   "                WHERE YEAR = '" + param[0] + "' ");
			stb.append(			   "                  AND CHAIRCD = '" + param[2] + "' ");
			stb.append(			   "                GROUP BY SCHREGNO) ");
			stb.append(    ") ");
			
            if ("1".equals(param[6])) {
                stb.append(" ,RECORD_PROV_FLG AS ( ");
                stb.append("     SELECT  SCHREGNO, PROV_FLG ");
                stb.append("     FROM   RECORD_PROV_FLG_DAT ");
                stb.append("     WHERE  YEAR = '" + param[0] + "' ");
                //教育課程対応
                if ("1".equals(param[5])) {
                    stb.append(            "AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' ");
                } else {
                    stb.append(            "AND SUBCLASSCD = '" + param[3] + "' ");
                }
                stb.append("     ) ");
            }

            //メイン表
			stb.append("SELECT  T2.HR_NAMEABBV, ");
			stb.append(        "T2.ATTENDNO, ");
			stb.append(        "T2.NAME, ");
			stb.append(		   "SEM1_INTR_SCORE, SEM1_INTR_VALUE, ");
			stb.append(		   "SEM1_TERM_SCORE, SEM1_TERM_VALUE, ");
			stb.append(		   "SEM1_VALUE, ");
			stb.append(		   "SEM2_INTR_SCORE, SEM2_INTR_VALUE, ");
			stb.append(		   "SEM2_TERM_SCORE, SEM2_TERM_VALUE, ");
			stb.append(		   "SEM2_VALUE, ");
			stb.append(		   "SEM3_INTR_SCORE, SEM3_INTR_VALUE, ");
			stb.append(		   "SEM3_TERM_SCORE, SEM3_TERM_VALUE, ");
			stb.append(		   "SEM3_VALUE, ");
			stb.append(	       "SEM1_INTR_ATTEND, ");
			stb.append(	       "SEM1_TERM_ATTEND, ");
			stb.append(	       "SEM2_INTR_ATTEND, ");
			stb.append(	       "SEM2_TERM_ATTEND, ");
			stb.append(	       "SEM3_INTR_ATTEND, ");
			stb.append(	       "SEM3_TERM_ATTEND, ");
            if ("1".equals(param[6])) {
                stb.append(        "CASE WHEN P0.PROV_FLG = '1' THEN 'レ' END AS PROV_FLG, ");
            }
			stb.append(        "GRAD_VALUE ");
			
			stb.append("FROM    SCHNO T2 ");
			stb.append(        "LEFT JOIN RECORD T1 ON T1.SCHREGNO = T2.SCHREGNO ");
            /* 仮評定情報 */
            //仮評定フラグ対応
            if ("1".equals(param[6])) {
                stb.append(        " LEFT JOIN RECORD_PROV_FLG P0 ON P0.SCHREGNO = T2.SCHREGNO ");
            }
			stb.append("ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO");

		} catch( Exception ex ){
			log.error("[KNJD124]prestatementRecord error!", ex );
		}
//log.debug("[KNJD124]prestatementRecord = "+stb.toString());

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
	         if ("1".equals(param[5])) {
	             stb.append("WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' ");
	         } else {
	             stb.append("WHERE  SUBCLASSCD ='"+param[3]+"' ");
	         }
		} catch( Exception ex ){
			log.error("[KNJD124]prestatementHead error!", ex );
		}
//log.debug(stb);
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
			if ("1".equals(param[5])) {
                stb.append(       "CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' ");
			} else {
			    stb.append(		  "SUBCLASSCD = '" + param[3]+"' ");
			}
		} catch( Exception ex ){
			log.error("[KNJD124]prestatementHead error!", ex );
		}
		return stb.toString();

	}//prestatementHead()の括り


	/** 
     *  SQLStatement作成 担当者名
     **/
	String prestatementHeadStaff(String param[]) {

		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT STAFFNAME ");
			stb.append("FROM   STAFF_MST ");
			stb.append("WHERE  STAFFCD = '" + param[4] + "' ");
		} catch( Exception ex ){
			log.error("[KNJD124]prestatementHeadStaff error!", ex );
		}
		return stb.toString();

	}//prestatementHead()の括り

	/** 
     *  SQLStatement作成 担当者名
     **/
	String prestatementHeadStaff2(String param[]) {

		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT STAFFNAME,t1.STAFFCD ");
			stb.append("FROM   CHAIR_STF_DAT t1 left join STAFF_MST t2 on t1.STAFFCD = t2.STAFFCD  ");
			stb.append("WHERE  t1.YEAR = '" + param[0] + "' AND ");
			stb.append("       t1.SEMESTER = '" + param[1] + "' AND ");
			stb.append("       t1.CHAIRCD = '" + param[2] + "' ");
			stb.append("ORDER BY t1.STAFFCD ");
		} catch( Exception ex ){
			log.error("[KNJD124]prestatementHeadStaff error!", ex );
		}
		return stb.toString();

	}//prestatementHead()の括り

	/** 
     *  単位の取得
     */
	String prestatementHeadCredit(String param[])
    {
		StringBuffer stb = new StringBuffer();
		try {
			stb.append(" WITH CHAIR_A AS(SELECT ");
			stb.append("     SCHREGNO, ");
			stb.append("     S1.SEMESTER ");
			stb.append(" FROM ");
			stb.append("     CHAIR_STD_DAT S1, ");
			stb.append("     CHAIR_DAT S2 ");
			stb.append(" WHERE ");
			stb.append("     S1.YEAR = '" + param[0] + "' AND ");
			stb.append("     S1.SEMESTER <= '" + param[1] + "' AND ");
			stb.append("     S1.CHAIRCD = '" + param[2] + "' AND ");
	         if ("1".equals(param[5])) {
	             stb.append("     S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || S2.SUBCLASSCD = '" + param[3] + "' AND ");
	         } else {
	             stb.append("     S2.SUBCLASSCD = '" + param[3]+"' AND ");
	         }
			stb.append("     S2.YEAR = S1.YEAR AND ");
			stb.append("     S2.SEMESTER = S1.SEMESTER AND ");
			stb.append("     S2.CHAIRCD = S1.CHAIRCD ");
			stb.append(" GROUP BY ");
			stb.append("     SCHREGNO, ");
			stb.append("     S1.SEMESTER ");
			stb.append(" ), ");
			stb.append(" btable (GRADE,COURSECD,YEAR,MAJORCD,COURSECODE) as ( ");
			stb.append(" SELECT   ");
			stb.append("     T2.GRADE,T2.COURSECD,T2.YEAR,T2.MAJORCD,T2.COURSECODE ");
			stb.append(" FROM     ");
			stb.append("     SCHREG_REGD_DAT T2,  ");
			stb.append("     SCHREG_BASE_MST T3,  ");
			stb.append("     SCHREG_REGD_HDAT T4  ");
			stb.append(" WHERE    ");
			stb.append("     T2.YEAR = '" + param[0] + "' AND  ");
			stb.append("     T2.SEMESTER = (SELECT ");
			stb.append("                        MAX(SEMESTER) ");
			stb.append("                    FROM ");
			stb.append("                        SCHREG_REGD_DAT W2 ");
			stb.append("                    WHERE ");
			stb.append("                        W2.YEAR = '" + param[0] + "' AND ");
			stb.append("                        W2.SEMESTER <= '" + param[1] + "' AND ");
			stb.append("                        W2.SCHREGNO = T2.SCHREGNO ) AND  ");
			stb.append("     T2.SCHREGNO = T3.SCHREGNO AND T4.YEAR = '" + param[0] + "' AND  ");
			stb.append("     T4.SEMESTER = T2.SEMESTER AND T4.GRADE = T2.GRADE AND  ");
			stb.append("     T4.HR_CLASS = T2.HR_CLASS AND ");
			stb.append("     EXISTS(SELECT ");
 			stb.append("                'X' ");
 			stb.append("            FROM ");
			stb.append("                 CHAIR_A T5 ");
			stb.append("             WHERE ");
			stb.append("                 T5.SCHREGNO = T2.SCHREGNO ) ) ");
			stb.append(" SELECT  ");
			stb.append("     T1.YEAR,MAX(CREDITS) as MAXCREDITS,MIN(CREDITS) as MINCREDITS ");
			stb.append(" FROM  ");
			stb.append("     btable T5,CREDIT_MST T1 ");
			stb.append(" WHERE  ");
			stb.append("     T1.YEAR = '" + param[0] + "' AND T1.GRADE = T5.GRADE AND  ");
			stb.append("     T1.COURSECD = T5.COURSECD AND T1.MAJORCD = T5.MAJORCD AND ");
			stb.append("     T1.GRADE = T5.GRADE AND T1.COURSECODE = T5.COURSECODE AND ");
	         if ("1".equals(param[5])) {
	             stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + param[3] + "' ");
	         } else {
	             stb.append("     T1.SUBCLASSCD = '" + param[3]+"' ");
	         }
			stb.append(" GROUP BY ");
			stb.append("     T1.YEAR ");

//log.debug(stb);
//log.debug(param[3].substring(0,2));

		} catch( Exception ex ){
			log.error("[KNJD124]prestatementHeadCredit error!", ex );
		}
		return stb.toString();

    }



	/** 
     *  出欠記号の取得
     */
    private Map getAttendMarkMap( DB2UDB db2, String param[] )
    {
		StringBuffer stb = new StringBuffer();
		Map attendmap = new HashMap(16);
		ResultSet rs = null;

		try {
            stb.append("SELECT  DI_CD, DI_MARK ");
            stb.append("FROM    ATTEND_DI_CD_DAT ");
            stb.append("WHERE   YEAR = '" + param[0] + "' AND '0' <  DI_CD ");
            stb.append("ORDER BY DI_CD ");
            db2.query( stb.toString() );
            rs = db2.getResultSet();
            while( rs.next() ) attendmap.put( rs.getString("DI_CD"),  rs.getString("DI_MARK") );
		} catch( Exception ex ){
			log.warn("ResultSet error!",ex);
		} finally{
			db2.commit();
			if( rs != null )try {rs.close();} catch( Exception ex ) {log.warn("ResultSet error!",ex);}
		}
		return attendmap;

    }

}//クラスの括り
