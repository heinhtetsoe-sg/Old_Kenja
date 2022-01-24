/**
 *
 *	学校教育システム 賢者 [入試処理]  入試試験合否一覧表
 *
 *					＜ＫＮＪＬ３２８Ｈ＞  入試試験合否一覧表
 *
 *	2007/11/13 RTS 作成日
 */

package servletpack.KNJL;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJL328H {

    private static final Log log = LogFactory.getLog(KNJL328H.class);

	String param[];
	boolean nonedata;
    int pageno;
    int totalpageno;
    int totalnum[];
    int outcount;
    private StringBuffer stb = new StringBuffer();
    DecimalFormat df = new DecimalFormat("#.#");
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

        try {
            ps2 = db2.prepareStatement( statementMeiboMeisai() );       //受験者名簿
            ps3 = db2.prepareStatement( statementMeiboTotalPage() );    //総頁数
            ps4 = db2.prepareStatement( statementMeishou() );           //名称マスター
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
        printSvfMain(db2, svf, ps2, ps3, ps4);	            //統計表・名簿の印刷

        try {
            if( ps2 != null ) ps2.close();
            if( ps3 != null ) ps3.close();
            if( ps4 != null ) ps4.close();
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
                                                PreparedStatement ps3, PreparedStatement ps4 ) {

    	//見出し項目
        getHead(db2, ps4);

        if( ! param[2].equals("0") ){
            getTestdiv(db2, ps4, param[2]);						//入試区分名称
            printSvfMeibo(db2, svf, param[2], ps2, ps3, ps4);	//名簿の印刷
            return;
        }

        ResultSet rs = null;
        int testdiv = 0;

		try{
            db2.query("SELECT MAX(W1.TESTDIV) FROM ENTEXAM_RECEPT_DAT W1 WHERE  W1.ENTEXAMYEAR = '" + param[0] + "' ");
            rs = db2.getResultSet();
            if( rs.next() ) testdiv = Integer.parseInt( rs.getString(1) );
			if( rs != null ) rs.close();
            db2.commit();

            for( int i = 1 ; i <= testdiv ; i++ ){
                getTestdiv( db2, ps4, String.valueOf( i ) );					//入試区分名称
                printSvfMeibo( db2, svf, String.valueOf( i ), ps2, ps3, ps4 );	//名簿の印刷
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
        printSvfMeiboHead(db2, svf, testdiv, ps3);

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
                    }
                    if( outcount == 0 ) printSvfTestdiv(db2,svf, testdiv );
                    outcount++;
                    printSvfMeiboOut1(db2, svf, rs, outcount);    //受験生別出力
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
    void printSvfMeiboHead(DB2UDB db2, Vrw32alp svf, String testdiv, PreparedStatement ps3)
	{
		int ret = 0;
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
    void printSvfMeiboOut1(DB2UDB db2, Vrw32alp svf, ResultSet rs, int i)
	{
		int ret = 0;
		String retSql = "";

        if (false && 0 != ret) { ret = 0; }
		try{

		    //*------------------------------------------------------------------*
		    //* <判定の設定>                                                     *
		    //* ・合格者			⇒受付データの合否区分＝1(合格)              *
		    //* ・不合格者			⇒受付データの合否区分＝2(不合格)            *
		    //* ・繰上合格者		⇒受付データの合否区分＝3(繰上合格候補)      *
		    //*						  且つ 基礎データの合否判定＝1(合格)         *
		    //* ・繰上合格候補者	⇒受付データの合否区分＝3(繰上合格候補)      *
		    //*						  且つ 基礎データの合否判定＝3(繰上合格候補) *
		    //*------------------------------------------------------------------*
            if( rs.getString("JUDGEDIV") != null ){
                if( rs.getString("JUDGEDIV").equals("1") ){
        			// 合格
                	retSql = createNameMstSql("L013", "1");
                } else {
                	// 不合格
                    if( rs.getString("JUDGEDIV").equals("2") ){
            			retSql = createNameMstSql("L013", "2");
        			}
                    if( rs.getString("JUDGEDIV").equals("3") ){
            			if( rs.getString("JUDGEMENT") != null){
                			// 繰上合格
            				if(rs.getString("JUDGEMENT").equals("1")){
                    			retSql = createNameMstSql("L010", "1");
                        	} else if(rs.getString("JUDGEMENT").equals("3")){
                            	// 繰上合格候補者
                    			retSql = createNameMstSql("L013", "3");
                        	}
            			}
        			}
                }
			    svf.VrsOut("JUDGEMENT" + i	,	getNameMast(db2, retSql));
            } else if( rs.getString("EXAMINEE_DIV") != null  &&  rs.getString("EXAMINEE_DIV").equals("2")){
	 		    //未受験を設定
            	ret = svf.VrsOut( "JUDGEMENT" + i,  "未受験" );
            }
            
            //受験番号
	 		ret = svf.VrsOut( "EXAMNO"    + i,  rs.getString("EXAMNO")  );
	 		//氏名
	 		String testNAME = "NAME" + Integer.toString(i);
	 		ret = svf.VrsOut( setformatArea(testNAME, 10, rs.getString("NAME")),
	 				( rs.getString("NAME")      != null )? "　" + rs.getString("NAME")    : ""  );
			//性別
            ret = svf.VrsOut( "SEX"       + i,  ( rs.getString("SEXNAME")   != null )? rs.getString("SEXNAME") : ""  );
			//合計
	 		ret = svf.VrsOut( "TOTAL"    + i,  ( rs.getString("TOTAL4")    != null )? rs.getString("TOTAL4")  : ""  );

	 		//順位
	 		ret = svf.VrsOut( "RANK"     + i,  ( rs.getString("TOTAL_RANK4")    != null )? rs.getString("TOTAL_RANK4")  : ""  );
            if( rs.getString("ENTDIV") != null ){
    	 		//手続き
    			retSql = createNameMstSql("L012", rs.getString("ENTDIV"));
			    svf.VrsOut("PROCEDUREDIV" + i	,	getNameMast(db2, retSql));
			    //備考
			    if(rs.getString("ENTDIV").equals("2")){
			 		String sRemark = "REMARK" + Integer.toString(i);
			 		ret = svf.VrsOut( setformatArea(sRemark, 10, rs.getString("REMARK1")),
			 				( rs.getString("REMARK1")      != null )? "　" + rs.getString("REMARK1")	: ""  );
			    }
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
    	int idx_kamoku = 1;
    	
        if (false && 0 != ret) { ret = 0; }
		try{
		    // 入試制度が高校一般または高校推薦の場合
			if(param[1].equals("2") || param[1].equals("3")){
				svf.VrsOut( "POINT" + i + "_" + idx_kamoku ,	rs.getString("TOTAL_ALL") ); //内申点
				++idx_kamoku;
		    }
            if( rs.getString("SCORE") != null ){
            	// 志願者得点データより試験科目コードを取得
                String sTestKamoku = rs.getString("TESTSUBCLASSCD");
				// 試験科目が名称マスタの試験科目(L009)に存在する場合、設定
				for(int j=0;j<hkamoku.size();j++){
	                if(hkamoku.get(String.valueOf(idx_kamoku)).equals(sTestKamoku)){
						ret = svf.VrsOut( "POINT" + i + "_" + idx_kamoku ,	rs.getString("SCORE") ); //各教科得点
	                }
	                ++idx_kamoku;
				}
            }
		    if(param[1].equals("2")){
				svf.VrsOut( "POINT" + i + "_" + idx_kamoku ,	rs.getString("KASANTEN_ALL") ); //加算点
		    }
		} catch( Exception ex ){
			log.error("printSvfMeiboOut2 error!",ex);
		}
	}


	/**
     *  svf print 入試区分出力
     */
    void printSvfTestdiv(DB2UDB db2, Vrw32alp svf, String testdiv )
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if( pageno + 1 == totalpageno ){
                ret = svf.VrSetForm("KNJL328H_2.frm", 1);
            } else {
                ret = svf.VrSetForm("KNJL328H_1.frm", 1);
            }
 			ret = svf.VrsOut( "NENDO",       param[5] );
 			ret = svf.VrsOut( "DATE",        param[6] );
	 		ret = svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );     //ページ
	 		ret = svf.VrsOut( "TOTAL_PAGE",  String.valueOf( totalpageno )  );  //総ページ
		    ret = svf.VrsOut( "TESTDIV",     param[10] );                       //入試区分
		    
		    //指示画面より指定された試験科目の取得
        	String retsql = getSubClass();
		    //指示画面より指定された試験科目の設定
        	setSubClass(db2, svf, retsql);
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
    		String sNENDO = convZenkakuToHankaku(param[0]);
 			param[5] = sNENDO + "年度";
        } catch( Exception ex ){
            log.error("printSvfMeiboHead error!",ex);
        }

	    //作成日(現在処理日)
		try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();		//各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;		//各情報を返すためのクラス
			returnval = getinfo.Control(db2);
			param[6] = fomatSakuseiDate(returnval.val3);
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
    void getTestdiv(DB2UDB db2, PreparedStatement ps4, String testdiv )
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rs = null;

        //入試区分
		try{
			if (param[1].equals("1")){
				ps4.setString( 1, "L004" );
				ps4.setString( 2, testdiv );
			} else {
				ps4.setString( 1, "L003" );
				ps4.setString( 2, param[1] );
			}
			rs = ps4.executeQuery();
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
			param[3] = request.getParameter("SORT");         	//印刷順(1⇒成績順、2⇒受験番号順)
		} catch( Exception ex ) {
			log.error("get parameter error!" + ex);
		}
        getParam2( request );
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
			//Databaseクラスを継承したクラス
			db2 = new DB2UDB(request.getParameter("DBNAME")	, "db2inst1", "db2inst1", DB2UDB.TYPE2);
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
            //入試制度(全て：param[1] == 0)
            if( Integer.parseInt(param[1]) != 0  )
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
            stb.append(        "W2.TOTAL4, ");
            stb.append(        "W2.TOTAL_RANK4, ");
            stb.append(        "W1.EXAMINEE_DIV, ");
            stb.append(        "W1.APPLICANT_DIV, ");
            stb.append(        "W3.NAME, ");
            stb.append(        "W3.SEX, ");
            stb.append(        "MEISYOU_GET(W3.SEX,'Z002',1) AS SEXNAME, ");
            stb.append(        "W2.HONORDIV, ");
            stb.append(        "W3.JUDGEMENT, ");
            stb.append(        "W3.SPECIAL_MEASURES, ");
            stb.append(        "W3.ENTDIV, ");
            stb.append(        "W3.REMARK1, ");
            stb.append(        "W4.TESTSUBCLASSCD, ");
            stb.append(        "W4.SCORE, ");
            stb.append(        "W5.TOTAL_ALL, ");
            stb.append(        "W5.KASANTEN_ALL ");

            stb.append( statementMeiboCommon() );

            if(param[3].equals("1")){
                stb.append("ORDER BY W2.TOTAL_RANK4, W1.EXAMNO, W4.TESTSUBCLASSCD ");
            } else {
                stb.append("ORDER BY W1.EXAMNO, W4.TESTSUBCLASSCD ");
            }

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

            stb.append( statementMeiboCommon() );

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
			if (param[1].equals("1")){
	            stb.append("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = ? AND NAMECD2 = ? ");
			} else {
	            stb.append("SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = ? AND NAMECD2 = ? ");
			}
			
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

    /**
     * 半角数字を全角数字に変換する
     * @param s
     * @return
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
     * @param s
     * @return
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
     * 帳票に設定する文字数が制限文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name	帳票出力エリア
     * @param area_len		制限文字数
     * @param sval			値
     * @return
     */
    private String setformatArea(String area_name, int area_len, String sval) {

    	String retAreaName = "";
		// 値がnullの場合はnullが返される
    	if (sval == null) {
			return null;
		}
    	// 設定値が制限文字超の場合、帳票設定エリアの変更を行う
    	if(area_len >= sval.length()){
   			retAreaName = area_name + "_1";
    	} else {
   			retAreaName = area_name + "_2";
    	}
        return retAreaName;
    }
    
	/**
     *  入試科目テーブルより、入試年度・入試制度に紐つく
     *  入試科目コードを取得する
     * @return	stb
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
     *  入試科目テーブルより、入試年度・入試制度に紐つく
     *  入試科目コードを取得する
     * @param rs	実行結果オブジェクト
     * @param svf	帳票オブジェクト
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
		    // 試験制度が高校(一般)の場合、帳票の試験科目名称を
		    // 『内申』より設定する
		    if(param[1].equals("2") || param[1].equals("3")){
		    	svf.VrsOut("SUBCLASS_NAME" + idx_kamoku	,	"内申");
				++idx_kamoku;
		    }
			while( rs.next() ){
			    svf.VrsOut("SUBCLASS_NAME" + idx_kamoku	,	rs.getString("NAME1"));
				hkamoku.put(String.valueOf(idx_kamoku)	,rs.getString("NAMECD2"));
				++idx_kamoku;
			}
		    // 試験制度が高校(一般)の場合、帳票の試験科目名称の最後に
		    // 『加算』を設定する
		    if(param[1].equals("2")){
		    	svf.VrsOut("SUBCLASS_NAME" + idx_kamoku	, "加算");
		    }

		} catch( Exception ex ){
            log.error("getSUBCLASS error!",ex);
    	} finally {
    		db2.commit();
    		DbUtils.closeQuietly(null, ps, rs);
    	}
	}

	/**
     *  名称マスタより、引数より取得した名称区分、名称コードを元に
     *  SQLモジュールを作成する。
     * @param sNamecd1		名称区分
     * @param sNamecd2		名称コード
     * @return	stb
     */
    private String createNameMstSql(String sNamecd1, String sNamecd2)
	{
    	stb = new StringBuffer();
        
		try{
            stb.append("SELECT  NAME1 ");
            stb.append(      "FROM    NAME_MST ");
            stb.append(      "WHERE   NAMECD1  = '" + sNamecd1 + "' AND ");
            stb.append(      "        NAMECD2  = '" + sNamecd2 + "'");

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}
	/**
     *  引数より取得したＳＱＬを発行し
     *  名称マスタより名称１を取得する。
     * @param rs		実行結果オブジェクト
     * @return	retName	名称マスタより取得した名称
     */
    private String getNameMast(DB2UDB db2, String stb)
	{
    	String retName = "";
		PreparedStatement ps = null;
    	ResultSet rs = null;
		try{
			// SQL発行
			ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            
   			if ( rs.next()  &&  rs.getString(1) != null ){
   				retName = rs.getString("NAME1");
   			}
		} catch( Exception ex ){
            log.error("setJudgementSql error!",ex);
    	} finally {
    		db2.commit();
    		DbUtils.closeQuietly(null, ps, rs);
    	}
		return retName;
	}

}//クラスの括り
