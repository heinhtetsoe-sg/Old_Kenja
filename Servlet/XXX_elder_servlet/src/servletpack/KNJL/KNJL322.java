/**
 *
 *	学校教育システム 賢者 [入試処理]  合格不合格一覧表
 *
 *	2004/12/20
 *  2005/01/05 科目コードを社会:3 理科:4に変更
 *             入試区分の履歴の表記において、受付データの合否区分=1の時は◎を出力、を追加
 *  2005/01/12 印刷されない不具合を修正 => SQLを改善、db2.commit()を可能な限りいれる。
 *  2005/01/12 db2.commit()を随所に入れる
 *	2005/01/14 nakamoto 受験番号○○◇◇◇の、○○の部分が変わったら改ページする	NO001
 *	2005/12/30 m-yama   受験番号○○◇◇◇の、○○の部分が変わったら改ページするを戻す	NO002
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


public class KNJL322 {

    private static final Log log = LogFactory.getLog(KNJL322.class);

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
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意

		Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;				// Databaseクラスを継承したクラス
		param = new String[13];
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

		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		PreparedStatement ps4 = null;
		PreparedStatement ps5 = null;
        PreparedStatement psTestDiv = null;
        PreparedStatement psTestDiv2 = null;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        try {
            ps2 = db2.prepareStatement( statementMeiboMeisai() );       //受験者名簿
            ps3 = db2.prepareStatement( statementMeiboTotalPage() );    //総頁数
            ps4 = db2.prepareStatement( statementMeishou() );           //名称マスター
            ps5 = db2.prepareStatement( statementApplicant_div() );     //出願コース
            psTestDiv = db2.prepareStatement( statementTestDivMst() );
            psTestDiv2 = db2.prepareStatement( statementTestDivMst2() );
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
//log.debug("ps2="+ps2.toString());
        printSvfMain(db2, svf, ps1, ps2, ps3, ps4, ps5, psTestDiv, psTestDiv2);	            //統計表・名簿の印刷

        try {
            if( ps1 != null ) ps1.close();
            if( ps2 != null ) ps2.close();
            if( ps3 != null ) ps3.close();
            if( ps4 != null ) ps4.close();
            if( ps5 != null ) ps5.close();
            if( psTestDiv != null ) psTestDiv.close();
            if( psTestDiv2 != null ) psTestDiv2.close();
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
    }


	/**
     *  svf print 印刷処理
     *            入試区分が指定されていれば( => param[2] !== "0" )１回の処理
     *            入試区分が複数の場合は全ての入試区分を舐める
     */
    void printSvfMain(DB2UDB db2, Vrw32alp svf, PreparedStatement ps1, PreparedStatement ps2,
                                                PreparedStatement ps3, PreparedStatement ps4,
                                                PreparedStatement ps5, PreparedStatement psTestDiv, PreparedStatement psTestDiv2) {

        getHead(db2, ps4);      //見出し項目

        if( ! param[2].equals("9") ){
            //getTestdiv(ps4, ps5, param[2]);                              //入試区分名称
            printSvfMeibo(db2, svf, param[2], ps2, ps3, ps4, ps5, psTestDiv, psTestDiv2);       //名簿の印刷
            return;
        }

        ResultSet rs = null;

		try{
            db2.query("SELECT W1.TESTDIV FROM ENTEXAM_RECEPT_DAT W1 WHERE  W1.ENTEXAMYEAR = '" + param[0] + "' GROUP BY W1.TESTDIV  ORDER BY W1.TESTDIV");
            rs = db2.getResultSet();

            while ( rs.next() ){
                //getTestdiv(ps4, ps5, rs.getString("TESTDIV"));                           //入試区分名称
                printSvfMeibo(db2, svf, rs.getString("TESTDIV"), ps2, ps3, ps4, ps5, psTestDiv, psTestDiv2);    //名簿の印刷
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
    void printSvfMeibo(DB2UDB db2, Vrw32alp svf, String testdiv, PreparedStatement ps2, PreparedStatement ps3,
                                                                 PreparedStatement ps4, PreparedStatement ps5, PreparedStatement psTestDiv, PreparedStatement psTestDiv2) {

        //名簿頁見出し印刷
        printSvfMeiboHead(db2, svf, testdiv, ps3, ps4, ps5, psTestDiv);

        //名簿印刷
		ResultSet rs = null;
		try{
            int p = 0;
//log.debug("ps2="+ps2.toString());
			ps2.setString( ++p, testdiv );
//			ps2.setString( ++p, testdiv );
			rs = ps2.executeQuery();
//log.debug("ps2 end");
			int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            String examno = null;
            String examno2 = null;    /* NO001 */
            int total2 = 0;
            int total4 = 0;
            int rank = 0;
            int rankcount = 0;
            totalnum = new int[3];
            for( int i = 0 ; i < totalnum.length ; i++ ) totalnum[i] = 0;

			while( rs.next() ){
                //受験番号のブレイク
                if( examno == null  ||  ! examno.equals( rs.getString("EXAMNO") ) ){
                    if( examno != null ){
                        ret = svf.VrEndRecord();
                        nonedata = true;
                    }
                    rankcount++;
                    if( rs.getInt("TOTAL2") != total2  ||  rs.getInt("TOTAL4") != total4 ){
                        rank = rankcount;
                        total2 = rs.getInt("TOTAL2");
                        total4 = rs.getInt("TOTAL4");
                    }
                    printSvfMeiboOut1(svf, rs, rank, examno2, psTestDiv2);         //受験生別出力    /* NO001 */
                    examno = rs.getString("EXAMNO");
                    examno2 = rs.getString("EXAMNO2");    /* NO001 */
                    //統計
                    if( rs.getString("SEX").equals("1") ) totalnum[0]++;
                    if( rs.getString("SEX").equals("2") ) totalnum[1]++;
                    totalnum[2]++;
                }
                printSvfMeiboOut2(svf, rs);             //科目別出力
			}
            if( examno != null ){
                printTotalNum(svf);		                //人数セット＆出力
                ret = svf.VrEndRecord();
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
    void printSvfMeiboHead(DB2UDB db2, Vrw32alp svf, String testdiv, PreparedStatement ps3, PreparedStatement ps4, 
                                                                                            PreparedStatement ps5, PreparedStatement psTestDiv)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		ResultSet rs = null;

        try {
            if( param[3].equals("1") )
                ret = svf.VrSetForm("KNJL322_1.frm", 4);
            else
                ret = svf.VrSetForm("KNJL322_2.frm", 4);
 			ret = svf.VrsOut("NENDO",          param[5] );
 			ret = svf.VrsOut("DATE",           param[6] );
	//	ＳＶＦ属性変更--->改ページ
//NO002
//            ret = svf.VrAttribute("EXAMNO2","FF=1");	/* NO001 */
        } catch( Exception ex ){
            log.error("printSvfMeiboHead error!",ex);
        }

        printSvfMeiboHead2( svf );

        //受験型
		try{
            int p = 0;
			ps4.setString( ++p , "L005" );
			ps4.setString( ++p , param[4] );
			rs = ps4.executeQuery();
			if ( rs.next()  &&  rs.getString(1) != null )
 			    ret = svf.VrsOut("EXAM_TYPE",  rs.getString(1) );
        } catch( Exception ex ){
            log.error("printHead error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
        }

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

        printSvfTestdiv(db2, svf, testdiv, ps4, ps5, psTestDiv);
   }


	/**
     *  svf print 名簿 見出印刷 帳票別
     */
    void printSvfMeiboHead2(Vrw32alp svf)
	{
        return;
    }

    private String getMark(String judgediv, String judgeclass, String honordiv) {
        String mark="";
        if (judgediv == null) mark= "";
        else if ("1".equals(judgediv) && "1".equals(honordiv)) mark= "☆";
        else if ("1".equals(judgediv) && "3".equals(judgeclass)) mark= "◎";
        else if ("1".equals(judgediv) && "4".equals(judgeclass)) mark= "○";
        else if ("1".equals(judgediv) && "2".equals(judgeclass)) mark= "◎";
        else if ("1".equals(judgediv) && "1".equals(judgeclass)) mark= "○";
        else if ("1".equals(judgediv) && "6".equals(judgeclass)) mark= "○";
        else if ("2".equals(judgediv)) mark= "×";
        return mark;
    }

	/**
     *  svf print 名簿 明細 受験生別出力
     */
    void printSvfMeiboOut1(Vrw32alp svf, ResultSet rs, int rank, String examno2, PreparedStatement psTestDiv2)    /* NO001 */
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rsTestDiv = null;
		try{
            if( rs.getString("HONORDIV") != null  &&  rs.getString("HONORDIV").equals("1") )
	 		    ret = svf.VrsOut( "HONORDIV",    "※"  );             //特待区分

	 		ret = svf.VrsOut( "EXAMNO2",    rs.getString("EXAMNO2")  );    //改ページ用フィールド（受験番号：頭２桁） NO001
	 		ret = svf.VrsOut( "EXAMNO",    rs.getString("EXAMNO")  );                                               //受験番号
	 		ret = svf.VrsOut( "NAME",      ( rs.getString("NAME")      != null )? rs.getString("NAME")    : ""  );  //氏名
            ret = svf.VrsOut( "SEX",       ( rs.getString("SEXNAME")   != null )? rs.getString("SEXNAME") : ""  );  //性別
	 		ret = svf.VrsOut( "4TOTAL",    ( rs.getString("TOTAL4")    != null )? rs.getString("TOTAL4")  : ""  );  //４科目計
            ret = svf.VrsOut( "KATEN",     ( rs.getString("KATEN")     != null )? rs.getString("KATEN")   : ""  );  //加点

            int num = 0;
	 		rsTestDiv = psTestDiv2.executeQuery();
	 		while (rsTestDiv.next()) {
	 		    num++;
	 		    String testdiv = rsTestDiv.getString("TESTDIV"); 
	 		    
	 		    ret = svf.VrsOut("CHECK" + num, getMark(rs.getString("JUDGEDIV" + testdiv), rs.getString("JUDGECLASS" + testdiv), rs.getString("HONORDIV" + testdiv)));
	 		}
	 		rsTestDiv.close();

            //備考欄・・・重複受験番号をカンマ区切りで出力する
            String remark = "";
            for (int i = 1; i <= 3; i++) {
                if (rs.getString("RECOM_EXAMNO" + i) != null) {
                    if (!"".equals(remark)) {
                        remark += ",";
                    }
                    remark += rs.getString("RECOM_EXAMNO" + i);
                }
            }
            svf.VrsOut( "REMARK",   remark );
//NO002
//            if( 50 == outcount || ( examno2 != null  &&  ! examno2.equals( rs.getString("EXAMNO2") ) ) ){/* NO001 */
            if( 50 == outcount ){/* NO001 */
                outcount = 0;
                ret = svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );       //ページ
            }
            outcount++;
		} catch( Exception ex ){
			log.error("printSvfMeiboOut1 error!",ex);
		}
	}


	/**
     *  svf print 名簿 明細 科目別出力
     *            ENTEXAM_SCORE_DATのTESTSUBCLASSCDはNOT NULL
     */
    void printSvfMeiboOut2(Vrw32alp svf, ResultSet rs)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try{
            if( rs.getString("SCORE") != null )
                if( rs.getString("TESTSUBCLASSCD").equals("1") )
	 		        ret = svf.VrsOut( "POINT1",    rs.getString("SCORE") ); //国語得点
                else if( rs.getString("TESTSUBCLASSCD").equals("2") )
	 		        ret = svf.VrsOut( "POINT2",    rs.getString("SCORE") ); //算数得点
                else if( rs.getString("TESTSUBCLASSCD").equals("3") )       // 05/01/05Modify
	 		        ret = svf.VrsOut( "POINT3",    rs.getString("SCORE") ); //社会得点
                else if( rs.getString("TESTSUBCLASSCD").equals("4") )       // 05/01/05Modify
	 		        ret = svf.VrsOut( "POINT4",    rs.getString("SCORE") ); //理科得点
		} catch( Exception ex ){
			log.error("printSvfMeiboOut2 error!",ex);
		}
	}


	/**
     *  svf print 入試区分出力
     */
    void printSvfTestdiv(DB2UDB db2, Vrw32alp svf, String testdiv, PreparedStatement ps4, PreparedStatement ps5, PreparedStatement psTestDiv)
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
 			    ret = svf.VrsOut("TESTDIV",  rs.getString(1) );
        } catch( Exception ex ){
            log.error("printSvfTestdiv error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
        }

        //文言セット
		try{
			ps5.setString( 1, testdiv );
			rs = ps5.executeQuery();
//log.debug("ps5="+ps5);
            if( stb == null ) stb = new StringBuffer();
            else              stb.delete(0,stb.length());
			while ( rs.next() ){
                if (rs.getString("EXAM_TYPE").equals("2")) {
                    stb.append("合計").append(rs.getString("BORDER_SCORE")).append( "点");
                    stb.append( ( param[3].equals("1") )? "以上  " : "未満  " );
                }
            }
            if( 0 < stb.length() )
 			    ret = svf.VrsOut("BORDERLINE",  stb.toString() );
        } catch( Exception ex ){
            log.error("getTestdiv error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
        }

		try{
	 		ret = svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );       //ページ
	 		ret = svf.VrsOut( "TOTAL_PAGE",  String.valueOf( totalpageno )  );    //総ページ
//log.debug("pageno1="+pageno);
		} catch( Exception ex ){
			log.error("printSvfTestdiv error!",ex);
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
            stb.append( "男" ).append( totalnum[0] ).append( "名, " );
            stb.append( "女" ).append( totalnum[1] ).append( "名, " );
            stb.append( "合計" ).append( totalnum[2] ).append( "名" );
	 		ret = svf.VrsOut( "NOTE",  stb.toString() );
		} catch( Exception ex ){
			log.error("printTotalNum error!",ex);
		}
//log.debug("totalnum print");
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

	/** get parameter doGet()パラメータ受け取り */
    void getParam(HttpServletRequest request)
	{
		try {
	        param[0] = request.getParameter("YEAR");         	//卒業年度
			param[1] = request.getParameter("APDIV");         	//入試制度(全て：0)
			param[2] = request.getParameter("TESTDV");         	//入試区分(全て：0)
			param[3] = request.getParameter("OUTPUT");         	//表種別
			param[11] = request.getParameter("COURSE");         //出願コース
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
            stb.append(      "FROM    ENTEXAM_RECEPT_DAT W1 ");
            stb.append(              "INNER JOIN ENTEXAM_DESIRE_DAT W2 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(              "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");
            stb.append(              "LEFT JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                 "W4.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                 "W4.EXAM_TYPE = W1.EXAM_TYPE AND ");
            stb.append(                                                 "W4.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                 "W4.RECEPTNO = W1.RECEPTNO ");
            stb.append(              "LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT W5 ON W5.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "   W5.EXAMNO = W1.EXAMNO ");
            stb.append(      "WHERE   W1.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(              "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.志願者区分
            if( Integer.parseInt(param[1]) != 0  )                                  //入試制度(全て：param[1] == 0)
                stb.append(         " W1.APPLICANTDIV = '" + param[1] + "' AND ");

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
            stb.append("SELECT  T1.EXAMNO, ");
			stb.append(        "SUBSTR(T1.EXAMNO,1,2) EXAMNO2, ");	/* NO001 */
            stb.append(        "T1.NAME, ");
            stb.append(        "T1.TESTSUBCLASSCD, ");
            stb.append(        "T1.SCORE, ");
            stb.append(        "T1.TOTAL2, ");
            stb.append(        "T1.TOTAL4, ");
            stb.append(        "T1.KATEN, ");
            stb.append(        "T1.SEX, ");
            stb.append(        "MEISYOU_GET(SEX,'Z002',1) AS SEXNAME, ");
            stb.append(        "T1.RECOM_EXAMNO1, ");
            stb.append(        "T1.RECOM_EXAMNO2, ");
            stb.append(        "T1.RECOM_EXAMNO3, ");
            stb.append(        "T1.HONORDIV, ");
            stb.append(        "T1.TESTDIV, ");
            stb.append(        "T2.JUDGEDIV0, ");
            stb.append(        "T2.JUDGEDIV1, ");
            stb.append(        "T2.JUDGEDIV2, ");
            stb.append(        "T2.JUDGEDIV3, ");
            stb.append(        "T2.JUDGEDIV4, ");
            stb.append(        "T2.JUDGEDIV5, ");
            stb.append(        "T2.JUDGEDIV6, ");
            stb.append(        "T2.JUDGECLASS0, ");
            stb.append(        "T2.JUDGECLASS1, ");
            stb.append(        "T2.JUDGECLASS2, ");
            stb.append(        "T2.JUDGECLASS3, ");
            stb.append(        "T2.JUDGECLASS4, ");
            stb.append(        "T2.JUDGECLASS5, ");
            stb.append(        "T2.JUDGECLASS6, ");
            stb.append(        "T2.HONORDIV0, ");
            stb.append(        "T2.HONORDIV1, ");
            stb.append(        "T2.HONORDIV2, ");
            stb.append(        "T2.HONORDIV3, ");
            stb.append(        "T2.HONORDIV4, ");
            stb.append(        "T2.HONORDIV5, ");
            stb.append(        "T2.HONORDIV6 ");
            stb.append("FROM (");
            stb.append(      "SELECT  W1.EXAMNO, ");
            stb.append(              "W3.NAME, ");
            stb.append(              "W4.TESTSUBCLASSCD, ");
            stb.append(              "W4.SCORE, ");
            stb.append(              "W1.TOTAL2, ");
            stb.append(              "W1.TOTAL4, ");
            stb.append(              "W1.KATEN, ");
            stb.append(              "W3.SEX, ");
            stb.append(              "W3.RECOM_EXAMNO1, ");
            stb.append(              "W3.RECOM_EXAMNO2, ");
            stb.append(              "W3.RECOM_EXAMNO3, ");
            stb.append(              "W1.HONORDIV, ");	// 04/12/30Modify by nakamoto (W3.HONORDIV→W1.HONORDIV)
            stb.append(              "W1.TESTDIV ");

            stb.append( statementMeiboCommon() );   //共通部分
            if( param[3].equals("1") )
                stb.append(          "W1.JUDGEDIV = '1' AND ");                        //受付データ.合否区分 => 合
            else
                stb.append(          "W1.JUDGEDIV = '2' AND ");                        //受付データ.合否区分 => 否

            stb.append(              "W1.TESTDIV = ? ");
            stb.append(      ")T1 ");
            stb.append(      "LEFT JOIN(");
            stb.append(      "SELECT  W1.EXAMNO, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '0' THEN W1.JUDGEDIV END) AS JUDGEDIV0, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '1' THEN W1.JUDGEDIV END) AS JUDGEDIV1, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '2' THEN W1.JUDGEDIV END) AS JUDGEDIV2, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '3' THEN W1.JUDGEDIV END) AS JUDGEDIV3, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '4' THEN W1.JUDGEDIV END) AS JUDGEDIV4, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '5' THEN W1.JUDGEDIV END) AS JUDGEDIV5, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '6' THEN W1.JUDGEDIV END) AS JUDGEDIV6, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '0' THEN W1.JUDGECLASS END) AS JUDGECLASS0, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '1' THEN W1.JUDGECLASS END) AS JUDGECLASS1, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '2' THEN W1.JUDGECLASS END) AS JUDGECLASS2, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '3' THEN W1.JUDGECLASS END) AS JUDGECLASS3, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '4' THEN W1.JUDGECLASS END) AS JUDGECLASS4, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '5' THEN W1.JUDGECLASS END) AS JUDGECLASS5, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '6' THEN W1.JUDGECLASS END) AS JUDGECLASS6, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '0' THEN W1.HONORDIV END) AS HONORDIV0, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '1' THEN W1.HONORDIV END) AS HONORDIV1, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '2' THEN W1.HONORDIV END) AS HONORDIV2, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '3' THEN W1.HONORDIV END) AS HONORDIV3, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '4' THEN W1.HONORDIV END) AS HONORDIV4, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '5' THEN W1.HONORDIV END) AS HONORDIV5, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '6' THEN W1.HONORDIV END) AS HONORDIV6 ");

            stb.append( statementMeiboCommon() );   //共通部分

            stb.append(              "W1.ENTEXAMYEAR = '" + param[0] + "' ");                        //受付データ.入試区分
            stb.append(      "GROUP BY W1.EXAMNO ");
            stb.append(      ")T2 ON T1.EXAMNO = T2.EXAMNO ");

            stb.append("ORDER BY T1.EXAMNO, T1.TESTSUBCLASSCD ");

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
			stb.append("SELECT ");								/* NO001 */
			stb.append("    SUM(T1.COUNT) COUNT  ");			/* NO001 */
			stb.append("FROM (");								/* NO001 */
            stb.append("SELECT CASE WHEN 0 < MOD(COUNT(DISTINCT W1.EXAMNO),50) THEN COUNT(DISTINCT W1.EXAMNO)/50 + 1 ELSE COUNT(DISTINCT W1.EXAMNO)/50 END AS COUNT ");

            stb.append( statementMeiboCommon() );   //共通部分
            if( param[3].equals("1") )
                stb.append(          "W1.JUDGEDIV = '1' AND ");                        //受付データ.合否区分 => 合
            else
                stb.append(          "W1.JUDGEDIV = '2' AND ");                        //受付データ.合否区分 => 否

            stb.append(       "W1.TESTDIV = ? ");                                   //受付データ.入試区分
//NO002
//			stb.append("     GROUP BY W1.TESTDIV, SUBSTR(W1.EXAMNO,1,2) ) T1  ");	/* NO001 */
			stb.append("     GROUP BY W1.TESTDIV ) T1  ");	/* NO001 */
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
     *  preparedstatement 入試区分マスターから入試区分を取得
     */
    String statementTestDivMst2()
    {
        if( stb == null ) stb = new StringBuffer();
        else              stb.delete(0,stb.length());
        try{
            stb.append("SELECT TESTDIV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '"+param[0]+"' ORDER BY SHOWORDER, TESTDIV ");
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
            stb.append("SELECT  W5.EXAM_TYPE, W5.BORDER_SCORE ");
            stb.append("FROM    ENTEXAM_PASSINGMARK_MST W5 ");
            stb.append("WHERE   W5.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(        "W5.APPLICANTDIV = '" + param[1] + "' AND ");    //入試制度
            stb.append(        "W5.TESTDIV = ? AND ");                          //入試区分
            stb.append(        "W5.SHDIV = '1' AND ");                          //専併区分
            stb.append(        "W5.COURSECD||W5.MAJORCD||W5.EXAMCOURSECD = '" + param[11] + "' ");  //コース
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

}//クラスの括り
