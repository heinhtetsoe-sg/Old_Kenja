/**
 *
 *	学校教育システム 賢者 [入試処理]  入試試験成績資料
 *
 *	2004/12/26
 *  2005/01/05 科目コードを社会:3 理科:4に変更
 *  2005/01/12 db2.commit()を随所に入れる
 *	2005/02/01 nakamoto 志願者基礎データのテーブル変更。SPECIAL_MEASURES（特別設置区分）1:繰上合格,2:特別合格 NO001
 *
 */

package servletpack.KNJL;

import java.io.IOException;
import java.text.DecimalFormat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJL329 {

    private static final Log log = LogFactory.getLog(KNJL329.class);

	String param[];
	boolean nonedata;
    int pageno;
    int totalpageno;
    int totalnum[];
    int outcount;
    private StringBuffer stb = new StringBuffer();
    DecimalFormat df = new DecimalFormat("#.#");

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;				// Databaseクラスを継承したクラス
		param = new String[11];
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

	}	//doGetの括り


	/**
     *  svf print 印刷処理 
     */
    void printSvf(DB2UDB db2, Vrw32alp svf)	{

		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		PreparedStatement ps4 = null;
        PreparedStatement psTestDiv = null;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        try {
            ps2 = db2.prepareStatement( statementMeiboMeisai() );       //受験者名簿
            ps3 = db2.prepareStatement( statementMeiboTotalPage() );    //総頁数
            ps4 = db2.prepareStatement( statementMeishou() );           //名称マスター
            psTestDiv = db2.prepareStatement( statementTestDivMst() );
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
//log.debug("ps2="+ps2.toString());
        printSvfMain(db2, svf, ps2, ps3, ps4, psTestDiv);	            //統計表・名簿の印刷

        try {
            if( ps2 != null ) ps2.close();
            if( ps3 != null ) ps3.close();
            if( ps4 != null ) ps4.close();
            if( psTestDiv != null ) psTestDiv.close();
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
    }


	/**
     *  svf print 印刷処理
     *            入試区分が指定されていれば( => param[2] !== "0" )１回の処理
     *            入試区分が複数の場合は全ての入試区分を舐める
     */
    void printSvfMain(DB2UDB db2, Vrw32alp svf, PreparedStatement ps2,
                                                PreparedStatement ps3, PreparedStatement ps4, PreparedStatement psTestDiv) {

        getHead(db2, ps4);      //見出し項目

        if( ! param[2].equals("9") ){
            getTestdiv(db2, psTestDiv, param[2]);                              //入試区分名称
            printSvfMeibo(db2, svf, param[2], ps2, ps3, ps4);       //名簿の印刷
            return;
        }

        ResultSet rs = null;

		try{
            db2.query("SELECT W1.TESTDIV FROM ENTEXAM_RECEPT_DAT W1 WHERE  W1.ENTEXAMYEAR = '" + param[0] + "' GROUP BY W1.TESTDIV  ORDER BY W1.TESTDIV");
            rs = db2.getResultSet();

            while ( rs.next() ){
                getTestdiv(db2, psTestDiv, rs.getString("TESTDIV"));                           //入試区分名称
                printSvfMeibo(db2, svf, rs.getString("TESTDIV"), ps2, ps3, ps4);    //名簿の印刷
            }
		} catch( Exception ex ){
			log.error("printSvfMain error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
		}

    }


	/**
     *  svf print 名簿の印刷
     */
    void printSvfMeibo(DB2UDB db2, Vrw32alp svf, String testdiv, PreparedStatement ps2, 
                                                                 PreparedStatement ps3, PreparedStatement ps4) {

        //名簿頁見出し印刷
        printSvfMeiboHead(db2, svf, testdiv, ps3, ps4);

        //名簿印刷
		ResultSet rs = null;
		try{
            int p = 0;
			ps2.setString( ++p, testdiv );
			rs = ps2.executeQuery();
			int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            String examno = null;
            totalnum = new int[5];
            for( int i = 0 ; i < totalnum.length ; i++ ) totalnum[i] = 0;

			while( rs.next() ){
                //受験番号のブレイク
                if( examno == null  ||  ! examno.equals( rs.getString("EXAMNO") ) ){
                    if( 50 == outcount ){
                        ret = svf.VrEndPage();
                        outcount = 0;
                        //ret = svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );       //ページ
                    }
                    if( outcount == 0 ) printSvfTestdiv(svf, testdiv, ps4 );
                    outcount++;
                    printSvfMeiboOut1(svf, rs, outcount);         //受験生別出力
                    examno = rs.getString("EXAMNO");
                    //統計
                    TotalNum(rs);                                 //統計処理
                }
                printSvfMeiboOut2(svf, rs, outcount);             //科目別出力
			}
            if( examno != null ){
                printTotalNum(svf);		                          //人数セット＆出力
                ret = svf.VrEndPage();
                nonedata = true;
            }
		} catch( Exception ex ){
			log.error("printSvfMeibo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
		}
    }


	/**
     *  svf print 名簿 見出印刷
     */
    void printSvfMeiboHead(DB2UDB db2, Vrw32alp svf, String testdiv, PreparedStatement ps3, PreparedStatement ps4)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        if (false && 0 != ret) { ret = 0; }
		ResultSet rs = null;
        //総ページ数
		try{
            int p = 0;
			ps3.setString( ++p, testdiv );
			rs = ps3.executeQuery();
            if( rs.next() ) totalpageno = rs.getInt("COUNT");
		} catch( Exception ex ){
			log.error("printSvfMeiboHead error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
            pageno = 0;
            outcount = 0;
		}

   }


	/**
     *  svf print 名簿 見出印刷 帳票別
     */
    void printSvfMeiboHead2(Vrw32alp svf)
	{
        return;
    }


	/**
     *  svf print 名簿 明細 受験生別出力
     */
    void printSvfMeiboOut1(Vrw32alp svf, ResultSet rs, int i)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try{
            if( rs.getString("JUDGEDIV") != null ){
                if( rs.getString("JUDGEDIV").equals("1") ) {
                    String judgeClass = rs.getString("JUDGECLASS");
                    if ("1".equals(rs.getString("HONORDIV"))) {
                        ret = svf.VrsOut( "JUDGEDIV" + i,  "☆" );   //合否
                    } else if ("3".equals(judgeClass)) {
                        ret = svf.VrsOut( "JUDGEDIV" + i,  "◎" );   //合否
                    } else if ("4".equals(judgeClass)) {
                        ret = svf.VrsOut( "JUDGEDIV" + i,  "○" );   //合否
                    } else if ("6".equals(judgeClass)) {
                        ret = svf.VrsOut( "JUDGEDIV" + i,  "○" );   //合否
                    } else if ("2".equals(judgeClass)) {
                        ret = svf.VrsOut( "JUDGEDIV" + i,  "◎" );   //合否
                    } else if ("1".equals(judgeClass)) {
                        ret = svf.VrsOut( "JUDGEDIV" + i,  "○" );   //合否
                    }
                } else
                    ret = svf.VrsOut( "JUDGEDIV" + i,  "×" );   //合否
            } else if( rs.getString("EXAMINEE_DIV") != null  &&  rs.getString("EXAMINEE_DIV").equals("2") )
	 		    ret = svf.VrsOut( "JUDGEDIV" + i,  "▲" );       //合否

	 		ret = svf.VrsOut( "EXAMNO"    + i,  rs.getString("EXAMNO")  );   //受験番号
	 		ret = svf.VrsOut( "NAME"      + i,  ( rs.getString("NAME")      != null )? "　" + rs.getString("NAME")    : ""  );  //氏名	// 04/12/30Modify by nakamoto ("　"を追加)
            ret = svf.VrsOut( "SEX"       + i,  ( rs.getString("SEXNAME")   != null )? rs.getString("SEXNAME") : ""  );  //性別
            ret = svf.VrsOut( "KATEN"     + i,  ( rs.getString("KATEN")     != null )? rs.getString("KATEN")   : ""  );  //加点
	 		ret = svf.VrsOut( "4TOTAL"    + i,  ( rs.getString("TOTAL4")    != null )? rs.getString("TOTAL4")  : ""  );  //４科目計
	 		ret = svf.VrsOut( "4RANK"     + i,  ( rs.getString("TOTAL_RANK4")    != null )? rs.getString("TOTAL_RANK4")  : ""  );  //４科目計
            ret = svf.VrsOut( "ABSENCE"   + i,  ( rs.getString("ABSENCE_DAYS")     != null )? rs.getString("ABSENCE_DAYS") : ""  );  //欠席数

            if( rs.getString("REMARK1")  !=  null )
                ret = svf.VrsOut( ( 15 <= rs.getString("REMARK1").length() )? "NOTE1_1" : "NOTE1_2_"   + i,  rs.getString("REMARK1") );  //備考
            if( rs.getString("REMARK2")  !=  null )
                ret = svf.VrsOut( ( 15 <= rs.getString("REMARK2").length() )? "NOTE2_1" : "NOTE2_2_"   + i,  rs.getString("REMARK2") );  //備考
/*
            if( rs.getString("HONORDIV") != null ){
                if( rs.getString("HONORDIV").equals("1") ){
                    ret = svf.VrsOut( "CONDITION" + i,  "特待"  );      //条件
	 		        ret = svf.VrsOut( "NAME"      + i,  ( rs.getString("NAME")      != null )? "※" + rs.getString("NAME")    : ""  );  //氏名
                }
            } else if( rs.getString("JUDGEMENT") != null ){
                if( rs.getString("JUDGEMENT").equals("2") )
                    ret = svf.VrsOut( "CONDITION" + i,  "繰上"  );      //条件
                else if( rs.getString("JUDGEMENT").equals("3") )
                    ret = svf.VrsOut( "CONDITION" + i,  "特別"  );      //条件
                else if( rs.getString("ENTDIV") != null  &&  rs.getString("ENTDIV").equals("2") )
                    ret = svf.VrsOut( "CONDITION" + i,  "辞退"  );      //条件 05/01/05
            }
*/

            if( rs.getString("ENTDIV") != null  &&  rs.getString("ENTDIV").equals("2") )
                ret = svf.VrsOut( "CONDITION" + i,  "辞退"  );          //条件 05/01/05
            else if( rs.getString("HONORDIV") != null  &&  rs.getString("HONORDIV").equals("1") ){
                    ret = svf.VrsOut( "CONDITION" + i,  "特待"  );      //条件
            } else if( rs.getString("SPECIAL_MEASURES") != null ){// NO001 (JUDGEMENT→SPECIAL_MEASURES)
                if( rs.getString("SPECIAL_MEASURES").equals("1") )// NO001 (JUDGEMENT→SPECIAL_MEASURES,2→1)
                    ret = svf.VrsOut( "CONDITION" + i,  "繰上"  );      //条件
                else if( rs.getString("SPECIAL_MEASURES").equals("2") )// NO001 (JUDGEMENT→SPECIAL_MEASURES,3→2)
                    ret = svf.VrsOut( "CONDITION" + i,  "特別"  );      //条件
            }

		} catch( Exception ex ){
			log.error("printSvfMeiboOut1 error!",ex);
		}
	}


	/**
     *  svf print 名簿 明細 科目別出力
     *            ENTEXAM_SCORE_DATのTESTSUBCLASSCDはNOT NULL
     */
    void printSvfMeiboOut2(Vrw32alp svf, ResultSet rs, int i)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try{
            if( rs.getString("SCORE") != null )
                if( rs.getString("TESTSUBCLASSCD").equals("1") )
	 		        ret = svf.VrsOut( "POINT1_" + i,    rs.getString("SCORE") ); //国語得点
                else if( rs.getString("TESTSUBCLASSCD").equals("2") )
	 		        ret = svf.VrsOut( "POINT2_" + i,    rs.getString("SCORE") ); //算数得点
                else if( rs.getString("TESTSUBCLASSCD").equals("3") )            // 05/01/05Modify
	 		        ret = svf.VrsOut( "POINT3_" + i,    rs.getString("SCORE") ); //社会得点
                else if( rs.getString("TESTSUBCLASSCD").equals("4") )            // 05/01/05Modify
	 		        ret = svf.VrsOut( "POINT4_" + i,    rs.getString("SCORE") ); //理科得点
		} catch( Exception ex ){
			log.error("printSvfMeiboOut2 error!",ex);
		}
	}


	/**
     *  svf print 入試区分出力
     */
    void printSvfTestdiv(Vrw32alp svf, String testdiv, PreparedStatement ps4 )
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if( pageno + 1 == totalpageno )
                ret = svf.VrSetForm("KNJL329_2.frm", 1);
            else
                ret = svf.VrSetForm("KNJL329_1.frm", 1);
 			ret = svf.VrsOut( "NENDO",       param[5] );
 			ret = svf.VrsOut( "DATE",        param[6] );
	 		ret = svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );       //ページ
	 		ret = svf.VrsOut( "TOTAL_PAGE",  String.valueOf( totalpageno )  );    //総ページ
		    ret = svf.VrsOut( "TESTDIV",     param[10] );                         //入試区分
        } catch( Exception ex ){
            log.error("printSvfMeiboHead error!",ex);
        }
	}


	/**
     *  svf print 最終人数カウント処理
     */
    void TotalNum(ResultSet rs)
	{
		try{
            if( rs.getString("APPLICANT_DIV") != null ){
                if( rs.getString("APPLICANT_DIV").equals("1") )
                    totalnum[0]++;                                      //志願者数
                if( rs.getString("EXAMINEE_DIV") != null  &&  rs.getString("EXAMINEE_DIV").equals("1") )
                    totalnum[1]++;                                      //受験者数
                if( rs.getString("EXAMINEE_DIV") != null  &&  rs.getString("EXAMINEE_DIV").equals("2") )
                    totalnum[2]++;                                      //欠席者数
                if( rs.getString("JUDGEDIV") != null  &&  rs.getString("JUDGEDIV").equals("1") )
                    totalnum[3]++;                                      //合格者
                if( rs.getString("JUDGEDIV") != null  &&  rs.getString("JUDGEDIV").equals("2") )
                    totalnum[4]++;                                      //不合格者
            }

		} catch( Exception ex ){
			log.error("printTotalNum error!",ex);
		}
	}


	/**
     *  svf print 最終人数出力
     */
    void printTotalNum(Vrw32alp svf)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
	 		ret = svf.VrsOut( "APPLICANT",  String.valueOf( totalnum[0] ) );    //志願者数
	 		ret = svf.VrsOut( "EXAMINEE",   String.valueOf( totalnum[1] ) );    //受験者数
	 		ret = svf.VrsOut( "ABSENTEE",   String.valueOf( totalnum[2] ) );    //欠席者数
	 		ret = svf.VrsOut( "SUCCESS",    String.valueOf( totalnum[3] ) );    //合格者
	 		ret = svf.VrsOut( "FAILURE",    String.valueOf( totalnum[4] ) );    //不合格者
		} catch( Exception ex ){
			log.error("printTotalNum error!",ex);
		}
	}


	/**
     *  svf print 見出項目取得
     */
    void getHead(DB2UDB db2, PreparedStatement ps4)
	{
        ResultSet rs = null;

        try {
 			param[5] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception ex ){
            log.error("printSvfMeiboHead error!",ex);
        }

	    //作成日(現在処理日)
		try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();		//各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;		//各情報を返すためのクラス
			returnval = getinfo.Control(db2);
			param[6] = KNJ_EditDate.h_format_JP(returnval.val3);
		} catch( Exception ex ){
			log.warn("ymd1 svf-out error!",ex);
		}

        //受験型
		try{
            int p = 0;
			ps4.setString( ++p , "L005" );
			ps4.setString( ++p , param[4] );
			rs = ps4.executeQuery();
			if ( rs.next()  &&  rs.getString(1) != null )
 			    param[7] = rs.getString(1);
        } catch( Exception ex ){
            log.error("printHead error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
        }

        //入試制度
		try{
            int p = 0;
			ps4.setString( ++p , "L003" );
			ps4.setString( ++p , param[1] );
			rs = ps4.executeQuery();
			if ( rs.next()  &&  rs.getString(1) != null )
 			    param[8] = rs.getString(1);
        } catch( Exception ex ){
            log.error("printHead error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
        }

   }


	/**
     *  svf print 入試区分名称セット
     */
    void getTestdiv(DB2UDB db2, PreparedStatement psTestDiv, String testdiv )
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rs = null;

        //入試区分
		try{
            psTestDiv.setString( 1, param[0] );
            psTestDiv.setString( 2, testdiv );
			rs = psTestDiv.executeQuery();
			if ( rs.next()  &&  rs.getString(1) != null )
 			    param[10] = rs.getString(1);
        } catch( Exception ex ){
            log.error("getTestdiv error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
        }
	}


	/** get parameter doGet()パラメータ受け取り */
    void getParam(HttpServletRequest request)
	{
		try {
	        param[0] = request.getParameter("YEAR");         	//卒業年度
			param[1] = request.getParameter("APDIV");         	//入試制度(全て：0)
			param[2] = request.getParameter("TESTDV");         	//入試区分(全て：0)
		} catch( Exception ex ) {
			log.error("get parameter error!" + ex);
		}
        getParam2( request );
//for( int i=0 ; i<param.length ; i++ )log.debug("param["+i+"]="+param[i]);
    }


	/** 
     *  get parameter doGet()パラメータ受け取り 帳票別
     */
    void getParam2(HttpServletRequest request)
	{
		try {
            param[4] = "1";         //受験型
		} catch( Exception ex ) {
			log.error("get parameter2 error!" + ex);
		}
    }


	/** print設定 */
    void setSvfInit(HttpServletResponse response ,Vrw32alp svf)
	{
		response.setContentType("application/pdf");
		int ret = svf.VrInit();											//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		try {
			ret = svf.VrSetSpoolFileStream(response.getOutputStream());   	//PDFファイル名の設定
 		} catch( java.io.IOException ex ){
			log.info("db new error:" + ex);
		}
   }


	/** svf close */
    void closeSvf(Vrw32alp svf)
	{
		if( !nonedata ){
		 	int ret = svf.VrSetForm("MES001.frm", 0);
            if (false && 0 != ret) { ret = 0; }
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}
		svf.VrQuit();
    }


	/** DB set */
	DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException
	{
		DB2UDB db2 = null;
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME")	, "db2inst1", "db2inst1", DB2UDB.TYPE2);	//Databaseクラスを継承したクラス
		} catch( Exception ex ){
			log.info("db new error:" + ex);
			if( db2 != null)db2.close();
		}
		return db2;
	}


	/** DB open */
	boolean openDb(DB2UDB db2)
	{
		try {
			db2.open();
		} catch( Exception ex ){
			log.error("db open error!"+ex );
			return true;
		}//try-cathの括り

		return false;

	}//boolean Open_db()


	/** DB close */
	void closeDb(DB2UDB db2)
	{
		try {
			db2.commit();
			db2.close();
		} catch( Exception ex ){
			log.error("db close error!"+ex );
		}//try-cathの括り
	}//Close_Db()


	/**
     *  preparedstatement 名簿、総ページ数STATEMENT共通部分
     */
	String statementMeiboCommon()
	{
        StringBuffer stb = new StringBuffer();
		try{
/* ************************
            stb.append(      "FROM    ENTEXAM_RECEPT_DAT W1 ");
            stb.append(              "INNER JOIN ENTEXAM_DESIRE_DAT W2 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(              "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");
            stb.append(              "INNER JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                 "W4.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                 "W4.EXAM_TYPE = W1.EXAM_TYPE AND ");
            stb.append(                                                 "W4.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                 "W4.RECEPTNO = W1.RECEPTNO ");
            stb.append(              "LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT W5 ON W5.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "   W5.EXAMNO = W1.EXAMNO ");
            stb.append(      "WHERE   W1.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(              "W2.APPLICANT_DIV = '1' AND ");

            if( Integer.parseInt(param[1]) != 0  )                                  //入試制度(全て：param[1] == 0)
                stb.append(         " W1.APPLICANTDIV = '" + param[1] + "' AND ");

            stb.append(              "W1.TESTDIV = ? ");
************************* */
            stb.append(      "FROM    ENTEXAM_DESIRE_DAT W1 ");
            stb.append(              "LEFT JOIN ENTEXAM_RECEPT_DAT W2 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(              "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");
            stb.append(              "LEFT JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
            stb.append(                                                 "W4.APPLICANTDIV = W2.APPLICANTDIV AND ");
            stb.append(                                                 "W4.EXAM_TYPE = W2.EXAM_TYPE AND ");
            stb.append(                                                 "W4.TESTDIV = W2.TESTDIV AND ");
            stb.append(                                                 "W4.RECEPTNO = W2.RECEPTNO ");
            stb.append(              "LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT W5 ON W5.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "   W5.EXAMNO = W1.EXAMNO ");
            stb.append(      "WHERE   W1.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(              "W1.APPLICANT_DIV = '1' AND ");
            stb.append(              "VALUE(W1.EXAMINEE_DIV,'0') = '1' AND ");

            if( Integer.parseInt(param[1]) != 0  )                                  //入試制度(全て：param[1] == 0)
                stb.append(         " W1.APPLICANTDIV = '" + param[1] + "' AND ");

            stb.append(              "W1.TESTDIV = ? ");
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 受験者名簿
     */
	String statementMeiboMeisai()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT  W1.EXAMNO, ");
            stb.append(        "W1.TESTDIV, ");
            stb.append(        "W2.JUDGEDIV, ");
            stb.append(        "W2.TOTAL2, ");
            stb.append(        "W2.TOTAL4, ");
            stb.append(        "W2.KATEN, ");
            stb.append(        "W2.TOTAL_RANK2, ");
            stb.append(        "W2.TOTAL_RANK4, ");
            stb.append(        "W1.EXAMINEE_DIV, ");
            stb.append(        "W1.APPLICANT_DIV, ");
            stb.append(        "W3.NAME, ");
            stb.append(        "W3.SEX, ");
            stb.append(        "MEISYOU_GET(W3.SEX,'Z002',1) AS SEXNAME, ");
            stb.append(        "W2.HONORDIV, ");	// 04/12/30Modify by nakamoto (W3.HONORDIV→W2.HONORDIV)
            stb.append(        "W3.SPECIAL_MEASURES, ");// NO001 (W3.JUDGEMENT→W3.SPECIAL_MEASURES)
            stb.append(        "W3.ENTDIV, ");      // 05/01/05
            stb.append(        "W3.REMARK1, ");
            stb.append(        "W3.REMARK2, ");
            stb.append(        "W4.TESTSUBCLASSCD, ");
            stb.append(        "W4.SCORE, ");
            stb.append(        "W5.ABSENCE_DAYS, ");
            stb.append(        "W2.JUDGECLASS ");

            stb.append( statementMeiboCommon() );   //共通部分

            stb.append("ORDER BY W1.EXAMNO, W4.TESTSUBCLASSCD ");

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 総ページ数
     */
	String statementMeiboTotalPage()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT CASE WHEN 0 < MOD(COUNT(DISTINCT W1.EXAMNO),50) THEN COUNT(DISTINCT W1.EXAMNO)/50 + 1 ELSE COUNT(DISTINCT W1.EXAMNO)/50 END AS COUNT ");

            stb.append( statementMeiboCommon() );   //共通部分

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 名称マスターから名称を取得
     */
	String statementMeishou()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = ? AND NAMECD2 = ? ");
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


    /**
     *  preparedstatement 入試区分マスターから名称を取得
     */
    String statementTestDivMst()
    {
        if( stb == null ) stb = new StringBuffer();
        else              stb.delete(0,stb.length());
        try{
            stb.append("SELECT NAME AS NAME1 FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = ? AND TESTDIV = ? ");
        } catch( Exception ex ){
            log.error("sql statement error!"+ex );
        }
        return stb.toString();
    }


	/**
     *  preparedstatement 出願区分名称を取得
     */
	String statementApplicant_div()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT  EXAMCOURSE_NAME ");
            stb.append("FROM    ENTEXAM_WISHDIV_MST W5 ");
            stb.append(        "LEFT JOIN ENTEXAM_COURSE_MST W6 ON W6.ENTEXAMYEAR = W5.ENTEXAMYEAR AND ");
            stb.append(                                           "W6.COURSECD = W5.COURSECD AND ");
            stb.append(                                           "W6.MAJORCD = W5.MAJORCD AND ");
            stb.append(                                           "W6.EXAMCOURSECD = W5.EXAMCOURSECD ");
            stb.append("WHERE   W5.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(        "W5.DESIREDIV = ? AND ");
            stb.append(        "W5.WISHNO = '1' ");
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

}//クラスの括り
