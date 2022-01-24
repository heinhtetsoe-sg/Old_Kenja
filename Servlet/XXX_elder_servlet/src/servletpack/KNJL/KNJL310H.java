/**
 *
 *	学校教育システム 賢者 [入試処理]  受験者チェックリスト（当日分含む）
 *
 *					＜ＫＮＪＬ３１０Ｈ＞  受験者チェックリスト
 *
 *	2007/11/07 RTS 作成日
 *
 */

package servletpack.KNJL;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJL310H {

    private static final Log log = LogFactory.getLog(KNJL310H.class);
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
            ps2 = db2.prepareStatement( statementMeishou() );
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
            ret = svf.VrSetForm("KNJL310H.frm", 4);
    		// 半角数字を全角数字に変換
    		String sNENDO = convZenkakuToHankaku(param[0]);
 			ret = svf.VrsOut("NENDO", sNENDO + "年度");
        } catch( Exception ex ){
            log.error("printHead error!",ex);
        }

        // 作成日(現在処理日)
		try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();		//各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;		//各情報を返すためのクラス
			returnval = getinfo.Control(db2);
			ret = svf.VrsOut("DATE",   fomatSakuseiDate(returnval.val3));
		} catch( Exception ex ){
			log.warn("ymd1 svf-out error!",ex);
		}

        // 入試区分
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
 			    ret = svf.VrsOut("TESTDIV",  rs.getString(1) );
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
    private void printMeibo(DB2UDB db2, Vrw32alp svf, PreparedStatement ps1, PreparedStatement ps3)
	{
		ResultSet rs = null;
		try{
			rs = ps1.executeQuery();
			int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            String exam_type_hall = null;
            totalnum = new int[3];

			while( rs.next() ){
                //会場のブレイク
                if( exam_type_hall == null  ||  ! exam_type_hall.equals( rs.getString("EXAM_TYPE_HALL") ) ){
                    if( exam_type_hall != null ){
                        for( ; 0 < outcount  &&  outcount < 40 ; outcount++ ) ret = svf.VrEndRecord();
                        outcount = 0;
                        pageno = 0;
                    }
                    printsvfKaijyou(db2, svf, rs, ps3);
                    exam_type_hall = rs.getString("EXAM_TYPE_HALL");
                } else{
                    if( 40 == outcount ){
                        outcount = 0;
                        ret = svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );       //ページ
                    }
                }
                if( rs.getString("SEX").equals("1") ) totalnum[0]++;
                if( rs.getString("SEX").equals("2") ) totalnum[1]++;
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
		String school_name = "";	//学校名称
		
        if (false && 0 != ret) { ret = 0; }
		try{
            ret = svf.VrAttribute( "RECEPTNO", "Hensyu=1" );    // 05/01/05
	 		ret = svf.VrsOut( "RECEPTNO",  String.valueOf(dft.format(Integer.parseInt(rs.getString("RECEPTNO"))))  );     //受付番号 05/01/05Modify
	 		ret = svf.VrsOut( "EXAMNO",    rs.getString("EXAMNO")  );       //受験番号
	 		ret = svf.VrsOut( setformatArea("NAME", 10, rs.getString("NAME"))
	 				,( rs.getString("NAME")      != null )? rs.getString("NAME")      : ""  ); //氏名
	 		ret = svf.VrsOut( setformatArea("KANA", 12, rs.getString("NAME_KANA"))
	 				,( rs.getString("NAME_KANA") != null )? rs.getString("NAME_KANA") : ""  ); //フリガナ
            ret = svf.VrsOut( "SEX",       ( rs.getString("SEXNAME")   != null )? rs.getString("SEXNAME")   : ""  ); //性別
	 		
			//出身学校名
			String ritu_name = nvlT(rs.getString("RITU_NAME"));
			String finschool_name = nvlT(rs.getString("FINSCHOOL_NAME"));
			if(ritu_name.equals("")){
				school_name =  finschool_name;
			} else {
				school_name =  ritu_name + "立" + finschool_name;
			}
			ret = svf.VrsOut(setformatArea("FINSCHOOL", 13, school_name)
					,school_name);
            
            if( pageno == totalpageno ) printTotalNum(svf);		    //人数セット＆出力
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
            int p = 0;
			ps3.setString( ++p, rs1.getString("EXAM_TYPE") ); //受験型
			ps3.setString( ++p, rs1.getString("EXAMHALLCD") ); //会場コード
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
            stb.append("SELECT CASE WHEN 0 < MOD(COUNT(*),40) THEN COUNT(*)/40 + 1 ELSE COUNT(*)/40 END AS COUNT ");
            stb.append( statementMeiboCommon() );   //共通部分
            stb.append(       "AND W1.EXAM_TYPE = ? ");
            stb.append(       "AND W2.EXAMHALLCD = ? ");

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
   			retAreaName = area_name + "1";
    	} else {
   			retAreaName = area_name + "2";
    	}
        return retAreaName;
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
