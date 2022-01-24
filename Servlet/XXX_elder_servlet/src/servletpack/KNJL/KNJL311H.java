/**
 *
 *	学校教育システム 賢者 [入試処理]  入学試験受験者点検票
 *
 *					＜ＫＮＪＬ３１１Ｈ＞  入学試験受験者点検票
 *
 *	2007/11/08 RTS 作成日
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


public class KNJL311H {

    private static final Log log = LogFactory.getLog(KNJL311H.class);
    private DecimalFormat dft = new DecimalFormat("#");

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
		param = new String[6];
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
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        try {
            ps2 = db2.prepareStatement( statementMeishou() );
            printHead(db2, svf, ps1, ps2);		                    //見出し項目のセット＆出力
            ps1 = db2.prepareStatement( statementMeiboMeisai() );
            printMeibo(db2, svf, ps1);                          	//名簿のセット＆出力
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        } finally{
            if( nonedata ) ret = svf.VrPrint();
        }
    }


	/**
     *  svf print 見出し印刷処理
     */
    private void printHead(DB2UDB db2, Vrw32alp svf, PreparedStatement ps1, PreparedStatement ps2)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		ResultSet rs = null;

        try {
            ret = svf.VrSetForm("KNJL311H.frm", 1);
        } catch( Exception ex ){
            log.error("printHead error!",ex);
        }

        //入試区分
		try{
			if (param[1].equals("1")){
				ps2.setString( 1, "L004" );
				ps2.setString( 2, param[2] );
			} else {
				ps2.setString( 1, "L003" );
				ps2.setString( 2, param[1] );
			}
			rs = ps2.executeQuery();
			if ( rs.next()  &&  rs.getString(1) != null )
 			    param[5] = rs.getString(1);
        } catch( Exception ex ){
            log.error("printHead error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
        }

   }


	/**
     *  svf print 名簿明細印刷処理
     */
    private void printMeibo(DB2UDB db2, Vrw32alp svf, PreparedStatement ps1)
	{
		ResultSet rs = null;
		try{
			rs = ps1.executeQuery();

			while( rs.next() ){
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
		String school_name = "";	//学校名称
		
        if (false && 0 != ret) { ret = 0; }
		try{
    		// 半角数字を全角数字に変換
    		String sNENDO = convZenkakuToHankaku(param[0]);
 			ret = svf.VrsOut( "NENDO",     sNENDO + "年度");
	 		ret = svf.VrsOut( "TESTDIV",   ( param[5] != null )? param[5] : "" );  //受験型
	 		ret = svf.VrsOut( "EXAM_TYPE", ( rs.getString("EXAM_TYPE_NAME") != null )? rs.getString("EXAM_TYPE_NAME") : "" );  //受験型
            ret = svf.VrAttribute( "RECEPTNO", "Hensyu=1" );
	 		ret = svf.VrsOut( "RECEPTNO",  String.valueOf(dft.format(Integer.parseInt(rs.getString("RECEPTNO"))))  );     //受付番号 05/01/05Modify
	 		ret = svf.VrsOut( "EXAMNO",    rs.getString("EXAMNO")  );               //受験番号
	 		ret = svf.VrsOut( "NAME",      ( rs.getString("NAME")      != null )? rs.getString("NAME")      : ""  ); //氏名
	 		ret = svf.VrsOut( "KANA",      ( rs.getString("NAME_KANA") != null )? rs.getString("NAME_KANA") : ""  ); //フリガナ
            ret = svf.VrsOut( "SEX",       ( rs.getString("SEXNAME")   != null )? rs.getString("SEXNAME")   : ""  ); //性別
	 		
			//出身学校名
			String ritu_name = nvlT(rs.getString("RITU_NAME"));
			String finschool_name = nvlT(rs.getString("FINSCHOOL_NAME"));
			if(ritu_name.equals("")){
				school_name =  finschool_name;
			} else {
				school_name =  ritu_name + "立" + finschool_name;
			}
			ret = svf.VrsOut("SCHOOLNAME",school_name);
	 		
            ret = svf.VrsOut( "EXAM_PLACE",( rs.getString("EXAMHALL_NAME") != null )? rs.getString("EXAMHALL_NAME") : ""  ); //会場名 05/01/12
            ret = svf.VrEndPage();
            nonedata = true;
		} catch( Exception ex ){
			log.error("printMeiboSvf error!",ex);
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
            stb.append("       LEFT JOIN FINSCHOOL_MST T1 ON  ");
			stb.append("              T1.FINSCHOOLCD = W3.FS_CD  ");
			stb.append("       LEFT JOIN NAME_MST T2 ON  ");
			stb.append("              T2.NAMECD1 = 'L001'  ");
			stb.append("          AND T2.NAMECD2 = T1.FINSCHOOL_DISTCD ");
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
            stb.append(      "(SELECT NAME1 FROM NAME_MST S1 WHERE NAMECD1='L005' AND NAMECD2 = W1.EXAM_TYPE) AS EXAM_TYPE_NAME, ");
            stb.append(       "W1.RECEPTNO, ");
            stb.append(       "W1.EXAMNO, ");
            stb.append(       "W2.EXAMHALLCD, ");
            stb.append(       "W2.EXAMHALL_NAME, ");
            stb.append(       "W3.NAME, ");
            stb.append(       "W3.NAME_KANA, ");
            stb.append(       "W3.FS_NAME, ");
            stb.append(       "W3.SEX, ");
			stb.append(       "T2.NAME1 AS RITU_NAME, ");
			stb.append(       "T1.FINSCHOOL_NAME, ");
            stb.append(      "(SELECT NAME1 FROM NAME_MST S1 WHERE NAMECD1='Z002' AND NAMECD2 = SEX) AS SEXNAME ");

            stb.append( statementMeiboCommon() );

            stb.append("ORDER BY W1.EXAM_TYPE, W2.EXAMHALLCD, W1.RECEPTNO ");

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 名称マスターから名称を取得
     */
	private String statementMeishou()
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
	 * NULL値を""として返す。
	 */
	private String nvlT(String val) {

		if (val == null) {
			return "";
		} else {
			return val;
		}
	}

}//クラスの括り
