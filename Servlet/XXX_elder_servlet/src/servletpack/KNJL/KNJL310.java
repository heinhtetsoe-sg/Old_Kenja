/**
 *
 *	学校教育システム 賢者 [入試処理]  受験者チェックリスト（当日分含む）
 *
 *	2004/12/12
 *  2005/01/05 座席番号（受付番号）の前ゼロは出力しない
 *  2005/01/12 db2.commit()を随所に入れる
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


public class KNJL310 {

    private static final Log log = LogFactory.getLog(KNJL310.class);
    private DecimalFormat dft = new DecimalFormat("#");                 // 05/01/05

	private	String param[];
	private boolean nonedata;
    private int pageno;
    private int totalpageno;
    private int totalnum[];
    private int outcount;
    private StringBuffer stb = new StringBuffer();


	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;				// Databaseクラスを継承したクラス
		param = new String[5];
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
    private void printSvf(DB2UDB db2, Vrw32alp svf)
	{
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        try {
            ps3 = db2.prepareStatement( statementMeiboTotalPage() );
            ps2 = db2.prepareStatement( statementTestDivMst() );
            printHead(db2, svf, ps2);		                        //見出し項目のセット＆出力
            ps1 = db2.prepareStatement( statementMeiboMeisai() );
            printMeibo(db2, svf, ps1, ps3);                        	//名簿のセット＆出力
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        } finally{
            if( nonedata ) ret = svf.VrPrint();
            try {
                if( ps1 != null ) ps1.close();
                if( ps2 != null ) ps2.close();
                if( ps3 != null ) ps3.close();
            } catch( Exception ex ){
                log.error("printSvf error!",ex);
            }
        }

    }


	/**
     *  svf print 見出し印刷処理
     */
    private void printHead(DB2UDB db2, Vrw32alp svf, PreparedStatement ps2)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		ResultSet rs = null;

        try {
            ret = svf.VrSetForm("KNJL310.frm", 4);
 			ret = svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
        } catch( Exception ex ){
            log.error("printHead error!",ex);
        }

	//	作成日(現在処理日)
		try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();		//各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;		//各情報を返すためのクラス
			returnval = getinfo.Control(db2);
			ret = svf.VrsOut("DATE",   KNJ_EditDate.h_format_JP(returnval.val3));
		} catch( Exception ex ){
			log.warn("ymd1 svf-out error!",ex);
		}

        //入試区分
		try{
			ps2.setString( 1, param[0] );
			ps2.setString( 2, param[2] );
			rs = ps2.executeQuery();
			if ( rs.next()  &&  rs.getString(1) != null )
 			    ret = svf.VrsOut("TESTDIV",  rs.getString(1) );
        } catch( Exception ex ){
            log.error("printHead error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
        }
/* *********************************
        //総ページ数
		try{
			rs = ps1.executeQuery();
            if( rs.next() ) totalpageno = rs.getInt("COUNT");
		} catch( Exception ex ){
			log.error("printMeibo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
		}
************************************ */
   }


	/**
     *  svf print 名簿明細印刷処理
     */
    private void printMeibo(DB2UDB db2, Vrw32alp svf, PreparedStatement ps1, PreparedStatement ps3)
	{
		ResultSet rs = null;
		try{
			rs = ps1.executeQuery();
			int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            String exam_type_hall = null;
            totalnum = new int[3];
            //for( int i = 0 ; i < totalnum.length ; i++ ) totalnum[i] = 0;

			while( rs.next() ){
                //会場のブレイク
                if( exam_type_hall == null  ||  ! exam_type_hall.equals( rs.getString("EXAM_TYPE_HALL") ) ){
                    if( exam_type_hall != null ){
                        for( ; 0 < outcount  &&  outcount < 40 ; outcount++ ) ret = svf.VrEndRecord();
                        outcount = 0;
                    }
                    printsvfKaijyou(db2, svf, rs, ps3);
                    exam_type_hall = rs.getString("EXAM_TYPE_HALL");
                } else{
                    if( 40 == outcount ){
                        outcount = 0;
                        ret = svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );       //ページ
                    }
                }
                if( "1".equals(rs.getString("SEX")) ) totalnum[0]++;
                if( "2".equals(rs.getString("SEX")) ) totalnum[1]++;
                totalnum[2]++;
                printsvfMeibo(svf, rs);
			}
		} catch( Exception ex ){
			log.error("printMeibo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
		}
    }


	/**
     *  svf print 名簿明細出力
     */
    private void printsvfMeibo(Vrw32alp svf, ResultSet rs)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try{
	 		//ret = svf.VrsOut( "RECEPTNO",  rs.getString("RECEPTNO")  );     //受付番号
            ret = svf.VrAttribute( "RECEPTNO", "Hensyu=1" );    // 05/01/05
	 		ret = svf.VrsOut( "RECEPTNO",  String.valueOf(dft.format(Integer.parseInt(rs.getString("RECEPTNO"))))  );     //受付番号 05/01/05Modify
	 		ret = svf.VrsOut( "EXAMNO",    rs.getString("EXAMNO")  );       //受験番号
            ret = svf.VrsOut( "MARK",      ("1".equals(rs.getString("NOT_REGISTERED")) ? "*" : ""));
	 		ret = svf.VrsOut( "NAME",      ( rs.getString("NAME")      != null )? rs.getString("NAME")      : ""  ); //氏名
	 		ret = svf.VrsOut( "KANA",      ( rs.getString("NAME_KANA") != null )? rs.getString("NAME_KANA") : ""  ); //フリガナ
            ret = svf.VrsOut( "SEX",       ( rs.getString("SEXNAME")   != null )? rs.getString("SEXNAME")   : ""  ); //性別
	 		ret = svf.VrsOut( "FINSCHOOL", ( rs.getString("FS_NAME")   != null )? rs.getString("FS_NAME")   : ""  ); //出身学校
            printTotalNum(svf);		    //人数セット＆出力
            ret = svf.VrEndRecord();
            nonedata = true;
            outcount++;
		} catch( Exception ex ){
			log.error("printMeiboSvf error!",ex);
		}
	}


	/**
     *  svf print 会場名出力
     *            2004/12/27 会場ごとの総ページ数をココへ移動
     */
    private void printsvfKaijyou(DB2UDB db2, Vrw32alp svf, ResultSet rs1, PreparedStatement ps3)
	{
        //総ページ数
        ResultSet rs = null;
		try{
			rs = ps3.executeQuery();
            if( rs.next() ) totalpageno = rs.getInt("COUNT");
		} catch( Exception ex ){
			log.error("printMeibo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
		}

		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try{
	 		ret = svf.VrsOut( "EXAM_PLACE",  ( rs1.getString("EXAMHALL_NAME") != null )? rs1.getString("EXAMHALL_NAME") : ""  ); //会場名
	 		ret = svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );       //ページ
	 		ret = svf.VrsOut( "TOTAL_PAGE",  String.valueOf( totalpageno )  );    //総ページ
		} catch( Exception ex ){
			log.error("printsvfKaijyou error!",ex);
		}

        for( int i = 0 ; i < totalnum.length ; i++ ) totalnum[i] = 0;
	}


	/**
     *  svf print 最終人数出力
     */
    private void printTotalNum(Vrw32alp svf)
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append( "男" ).append( totalnum[0] ).append( "名, " );
            stb.append( "女" ).append( totalnum[1] ).append( "名, " );
            stb.append( "合計" ).append( totalnum[2] ).append( "名" );
	 		svf.VrsOut( "NOTE",  stb.toString() );
		} catch( Exception ex ){
			log.error("printTotalNum error!",ex);
		}
	}


	/** get parameter doGet()パラメータ受け取り */
    private void getParam(HttpServletRequest request)
	{
		try {
	        param[0] = request.getParameter("YEAR");         	//卒業年度
			param[1] = request.getParameter("APDIV");         	//入試制度(全て：0)
			param[2] = request.getParameter("TESTDV");         	//入試区分
			param[3] = request.getParameter("EXAM_TYPE");       //受験型(全て：0)

			String hrclass[] = request.getParameterValues("category_name");		//会場
			stb.delete(0,stb.length());
			stb.append("(");
			for( int i=0 ; i<hrclass.length ; i++ ){
				if( i > 0 ) stb.append(",");
				stb.append("'").append(hrclass[i]).append("'");
			}
			stb.append(")");
			param[4] = stb.toString();									//対象組(カンマで接続)
		} catch( Exception ex ) {
			log.error("get parameter error!" + ex);
		}
for( int i=0 ; i<param.length ; i++ )log.debug("param["+i+"]="+param[i]);
    }


	/** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf)
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
    private void closeSvf(Vrw32alp svf)
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
	private DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException
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
	private boolean openDb(DB2UDB db2)
	{
		try {
			db2.open();
		} catch( Exception ex ){
			log.error("db open error!"+ex );
			return true;
		}//try-cathの括り

		return false;

	}//private boolean Open_db()


	/** DB close */
	private void closeDb(DB2UDB db2)
	{
		try {
			db2.commit();
			db2.close();
		} catch( Exception ex ){
			log.error("db close error!"+ex );
		}//try-cathの括り
	}//private Close_Db()


	/**
     *  preparedstatement 名簿、総ページ数STATEMENT共通部分
     */
	private String statementMeiboCommon()
	{
        StringBuffer stb = new StringBuffer();
		try{
            stb.append("FROM   ENTEXAM_RECEPT_DAT W1 ");
            stb.append(       "INNER JOIN ENTEXAM_HALL_DAT W2 ON W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                         "W2.EXAM_TYPE = W1.EXAM_TYPE AND ");
            stb.append(                                         "W2.EXAM_TYPE||W2.EXAMHALLCD IN " + param[4] + " AND ");
            stb.append(                                         "W1.RECEPTNO BETWEEN W2.S_RECEPTNO AND W2.E_RECEPTNO ");
            stb.append(       "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                         "W3.EXAMNO = W1.EXAMNO ");
            stb.append("WHERE  W1.ENTEXAMYEAR = '" + param[0] + "' AND ");

            if( Integer.parseInt(param[1]) != 0  )                                  //入試制度(全て：param[1] == 0)
                stb.append(   "W1.APPLICANTDIV = '" + param[1] + "' AND ");

            if( Integer.parseInt(param[3]) != 0  )                                  //受験型(全て：param[3] == 0)
                stb.append(   "W1.EXAM_TYPE = '" + param[3] + "' AND ");

            stb.append(       "W1.TESTDIV = '" + param[2] + "' ");                  //入試区分
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 受験者名簿
     */
	private String statementMeiboMeisai()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT W1.APPLICANTDIV, ");
            stb.append(       "W1.EXAM_TYPE || EXAMHALLCD AS EXAM_TYPE_HALL, ");
            stb.append(       "W1.TESTDIV, ");
            stb.append(       "W1.EXAM_TYPE, ");
            stb.append(       "W1.RECEPTNO, ");
            stb.append(       "W1.EXAMNO, ");
            stb.append(       "W2.EXAMHALLCD, ");
            stb.append(       "W2.EXAMHALL_NAME, ");
            stb.append(       "W3.NAME, ");
            stb.append(       "W3.NAME_KANA, ");
            stb.append(       "W3.FS_NAME, ");
            stb.append(       "W3.SEX, ");
            stb.append(      "(SELECT NAME1 FROM NAME_MST S1 WHERE NAMECD1='Z002' AND NAMECD2 = SEX) AS SEXNAME, ");
            stb.append(      "(CASE WHEN (W3.TESTDIV"+param[2]+" IS NULL) THEN '1' ELSE '0' END) AS NOT_REGISTERED ");
            

            stb.append( statementMeiboCommon() );   //共通部分

            stb.append("ORDER BY W1.EXAM_TYPE, W2.EXAMHALLCD, W1.RECEPTNO ");

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 総ページ数
     *                    2004/12/27 会場ごとの総頁に変更
     */
	private String statementMeiboTotalPage()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
/* ************************
            stb.append("SELECT SUM(EXAM_TYPE_HALL_COUNT) AS COUNT ");
            stb.append("FROM(");
            stb.append(       "SELECT CASE WHEN 0 < MOD(COUNT(*),40) THEN COUNT(*)/40 + 1 ELSE COUNT(*)/40 END AS EXAM_TYPE_HALL_COUNT ");

            stb.append( statementMeiboCommon() );   //共通部分

            stb.append(       "GROUP BY W1.EXAM_TYPE, W2.EXAMHALLCD ");
            stb.append(    ")T1");
************************** */
            
            stb.append("WITH PAGES AS( ");
            stb.append("SELECT CASE WHEN 0 < MOD(COUNT(*),40) THEN COUNT(*)/40 + 1 ELSE COUNT(*)/40 END AS COUNT ");
            stb.append( statementMeiboCommon() );   //共通部分
            stb.append("GROUP BY ");
            stb.append("    W2.EXAM_TYPE || W2.EXAMHALLCD ");
            stb.append(") SELECT ");
            stb.append("    SUM(COUNT) AS COUNT ");
            stb.append("FROM ");
            stb.append("    PAGES ");

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 入試区分マスターから名称を取得
     */
	private String statementTestDivMst()
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

}//クラスの括り
