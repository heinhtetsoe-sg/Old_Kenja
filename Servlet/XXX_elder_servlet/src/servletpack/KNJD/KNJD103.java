/**
 *
 *	学校教育システム 賢者 [成績管理]  個人成績表（テスト）[大宮]
 *
 *	2005/06/22 nakamoto  新規作成
 *
 */

package servletpack.KNJD;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.text.DecimalFormat;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJDefineCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJD103 {

    private static final Log log = LogFactory.getLog(KNJD103.class);
    private StringBuffer stb;
    private int ret;
    private boolean nonedata;
	private String printname;   			//プリンタ名
	private String schno[];	                //学籍番号
    private PrintWriter outstrm;
	private DecimalFormat dmf1 = new DecimalFormat("0");
	private DecimalFormat dmf2 = new DecimalFormat("0.0");


	/**
	  *
	  *  KNJD.classから最初に起動されるクラス
	  *
	  **/
	public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;				// Databaseクラスを継承したクラス
		String param[] = new String[15];

	// パラメータの取得
	    getParam( request, param );

	// print svf設定
		setSvfInit(response, svf);

	// ＤＢ接続
		db2 = setDb(request);
		if( openDb(db2) ){
			log.error("db open error");
			return;
		}

	// 印刷処理
		printSvf( request, db2, svf, param );

	// 終了処理
		closeSvf(svf);
		closeDb(db2);

	}	//doGetの括り


    /**
     *  印刷処理
     */
    private void printSvf( HttpServletRequest request, DB2UDB db2, Vrw32alp svf, String param[] ){

		try {
			param[5] = Set_Schno( schno );		//学籍番号の編集
			param[14] = getItem( param );		//対象成績フィールド編集(SQL用) 
			setHead( db2, svf, param );			//見出し項目
			printSvfMain( db2, svf, param);		//SVF-FORM出力処理
		} catch( Exception ex ){
			log.error("error! ",ex);
		}
    }


	/**
     *  対象生徒学籍番号編集(SQL用) 
     */
	private String Set_Schno(String schno[]){

        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

		for( int ia=0 ; ia<schno.length ; ia++ ){
			if( ia==0 )	stb.append("('");
			else		stb.append("','");
			stb.append(schno[ia]);
		}
		stb.append("')");

		return stb.toString();
	}


	/**
     *  対象成績フィールド編集(SQL用) 
     */
	private String getItem( String param[] ){

        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );
		stb.append("SEM").append(param[1]).append("_");
		stb.append( ( param[3].equals("01") )? "INTR" : "TERM" ).append("_");
		stb.append("SCORE");
		return stb.toString();

	}


	/** 
     *  SVF-FORMセット＆見出し項目
     */
	private void setHead(DB2UDB db2,Vrw32alp svf,String param[]){

		int ret = 0;
		ret = svf.VrSetForm("KNJD103.frm", 4);			//SVF-FORM

		param[8] = nao_package.KenjaProperties.gengou( Integer.parseInt( param[0] ) ) + "年度";	//年度

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;

		try {
			returnval = getinfo.Semester( db2, param[0], param[1] );
            param[9] = returnval.val1;			                    //学期名称
			param[10] = returnval.val2;								//学期期間FROM
			param[11] = returnval.val3;								//学期期間TO
		} catch( Exception ex ){
			log.error("error! ",ex);
        } finally {
			if( param[9] == null ) param[9] = "  学期";
            if( param[10] == null ) param[10] = param[0] + "-04-01";
            if( param[11] == null ) param[11] = ( Integer.parseInt(param[0]) + 1 ) + "-03-31";
		}

		try {
			returnval = getinfo.Staff_name(db2,param[0],param[1],param[2],"");
			param[12] = returnval.val1;								//学級担任名
		} catch( Exception ex ){
			log.error("error! ",ex);
		}

		try {
			returnval = getinfo.Control( db2 );
			param[13] = KNJ_EditDate.h_format_JP( returnval.val3);	//作成日
		} catch( Exception ex ){
			log.error("error! ", ex );
		}

		getinfo = null;
		returnval = null;

	}//setHead()の括り


	/** 
     *
     * SVF-OUT 印刷処理
     */
	private void printSvfMain( DB2UDB db2, Vrw32alp svf, String param[] )
	{
for(int i = 0 ; i < param.length ; i++ )log.debug( "param[" + i + "] = " + param[i] );

		int ret = 0;
		float arrtotal[] = { 0, 0, 0, 0 };
		float arrcnt[] = { 0, 0, 0, 0 };

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = db2.prepareStatement( prestatementStdData( param ) );		    //生徒別明細
			rs = ps.executeQuery();
			String schno = null;
			int gno = 0;
			while( rs.next() ){
				if( schno == null  ||  ! rs.getString("SCHREGNO").equals( schno ) ){
					if( schno != null )printsvfOutTotal( svf, rs, arrtotal, arrcnt );  //総合成績印刷
					printsvfOutHead( svf, rs, param );                         //学籍データ等印刷
					schno = rs.getString("SCHREGNO");
					gno = 0;
				}
				printsvfOutDetail( svf, rs, param, ++gno );       //成績データ等印刷
				accumuDetail( rs, arrtotal, arrcnt );             //成績データ累積処理
			}
			if( schno != null )printsvfOutTotal( svf, rs, arrtotal, arrcnt );          //総合成績印刷
		} catch( Exception ex ) {
			log.error("error! ",ex);
		} finally{
			db2.commit();
			if( rs != null ) try { rs.close(); } catch( SQLException ex ){ log.error("error! ",ex); }
			if( ps != null ) try { ps.close(); } catch( SQLException ex ){ log.error("error! ",ex); }
		}

	}


	/** 
     *
     * SVF-FORM-OUT 学籍データ等印刷
     *
     */
	private void printsvfOutHead( Vrw32alp svf, ResultSet rs, String param[] )
	{
		try {
			int ret = 0;
			ret = svf.VrsOut("TEST",      ( param[3].equals("01") )? "中間テスト" : "期末テスト"  );    //テスト名称
			ret = svf.VrsOut("NENDO",     param[8]);								        //年度
			ret = svf.VrsOut("SEMESTER",  param[9]);								        //学期
			ret = svf.VrsOut("DATE",      param[13]);								        //作成日
            ret = svf.VrsOut("STAFFNAME", param[12]);							            //学級担任
			ret = svf.VrsOut("NAME",      rs.getString("HR_NAME")
                                          + " " + String.valueOf( Integer.parseInt( rs.getString("ATTENDNO") ) )
                                          + "番   " + rs.getString("NAME") );       //生徒名

			if( rs.getString("CLSAVG") != null )
				ret = svf.VrsOut( "AVE_CLS_AVERAGE",  rs.getString("CLSAVG") );     //総合成績：学級平均
			if( rs.getString("RNKAVG_CLS") != null )
				ret = svf.VrsOut( "AVE_CLS_ORDER",    rs.getString("RNKAVG_CLS") ); //総合成績：学級順位
			if( rs.getString("GRDAVG") != null )
				ret = svf.VrsOut( "AVE_GRD_AVERAGE",  rs.getString("GRDAVG") );     //総合成績：学年平均
			if( rs.getString("RNKAVG_GRD") != null )
				ret = svf.VrsOut( "AVE_GRD_ORDER",    rs.getString("RNKAVG_GRD") ); //総合成績：学年順位

		} catch( SQLException ex ){
			log.error("error! ",ex);
		}
	}


	/** 
     *
     * SVF-FORM-OUT 成績データ等印刷
     *
     */
	private void printsvfOutDetail( Vrw32alp svf, ResultSet rs, String param[], int gno )
	{
		try {
			int f = ( gno < 13 )? 1 : 2;
			int g = ( gno < 13 )? gno : gno - 12;
			int ret = 0;
			ret = svf.VrsOutn( "SUBCLASS" + f, g, rs.getString("SUBCLASSABBV") );//科目名

			ret = svf.VrsOutn( "RECORD" + f, g, rs.getString("SCORE") );         //成績
			ret = svf.VrsOutn( "CLS_AVERAGE" + f, g, rs.getString("AVG_CLS") );  //学級：平均点
			ret = svf.VrsOutn( "CLS_ORDER" + f, g, rs.getString("RNK_CLS") );    //学級：順位
			ret = svf.VrsOutn( "GRD_AVERAGE" + f, g, rs.getString("AVG_GRD") );  //学年：平均点
			ret = svf.VrsOutn( "GRD_ORDER" + f, g, rs.getString("RNK_GRD") );    //学年：順位
		} catch( SQLException ex ){
			log.error("error! ",ex);
		}
	}


	/** 
     *
     * SVF-FORM-OUT 成績データ累積処理
     *
     */
	private void accumuDetail( ResultSet rs, float arrtotal[], float arrcnt[] )
	{
		try {

			if( rs.getString("SCORE") != null ){
				arrtotal[2] += Float.parseFloat( rs.getString("SCORE") );
				arrcnt[2]++;
			}

		} catch( SQLException ex ){
			log.error("error! ",ex);
		}
	}


	/** 
     *
     * SVF-FORM-OUT 総合成績印刷
     *
     */
	private void printsvfOutTotal( Vrw32alp svf, ResultSet rs, float arrtotal[], float arrcnt[] )
	{
		try {
			int ret = 0;
			if( 0 < arrcnt[2] ){
				ret = svf.VrsOut( "TOTAL_RECORD",  String.valueOf( dmf1.format( arrtotal[2] ) ) );   //合計
				ret = svf.VrsOut( "AVE_RECORD",    String.valueOf( dmf2.format( (float)Math.round( arrtotal[2] / arrcnt[2] * 10 ) / 10 ) ) );   //平均
            }


			ret = svf.VrEndRecord();
			nonedata = true;
		} catch( Exception ex ){
			log.error("error! ",ex);
		}

		for( int i = 0 ; i < arrtotal.length ; i++ ) arrtotal[i] = 0;
		for( int i = 0 ; i < arrcnt.length ; i++ ) arrcnt[i] = 0;
	}


	/** 
     *  PrepareStatement作成
     *  生徒別明細
     *  2005/06/20 yamashiro・ペナルティ欠課の算出式を修正
     *
     */
	String prestatementStdData(String param[])
	{
        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

		try {
            //在籍の表
			stb.append("WITH SCHNO_A AS( ");
			stb.append("    SELECT  T2.SCHREGNO, T3.NAME, T4.HR_NAME, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
			stb.append("    FROM    SCHREG_REGD_DAT T2, ");
			stb.append("            SCHREG_BASE_MST T3, ");
			stb.append("            SCHREG_REGD_HDAT T4 ");
			stb.append("    WHERE   T2.YEAR = '" + param[0] + "' AND ");
			stb.append("            T2.SEMESTER = '" + param[1] + "' AND ");
			stb.append("            T2.GRADE = '" + param[4] + "' AND ");
			stb.append("            T2.SCHREGNO = T3.SCHREGNO AND T4.YEAR = T2.YEAR AND ");
			stb.append("            T4.SEMESTER = T2.SEMESTER AND T4.GRADE = T2.GRADE AND ");
			stb.append("            T4.HR_CLASS = T2.HR_CLASS ");
			stb.append("    ) ");

		    //成績の表
			stb.append(",RECORD_A AS( ");
			stb.append("	SELECT  SCHREGNO, SUBCLASSCD, CHAIRCD,'CMN' AS CMN_FLG, ");
			stb.append("		    " + param[14] + " AS SCORE ");
			stb.append("	FROM    RECORD_DAT T1 ");
			stb.append("	WHERE   YEAR='" + param[0] + "' AND ");
			stb.append(" 	        " + param[14] + " IS NOT NULL ");
			stb.append("    ) ");

		    //学級：平均点および順位の人数
			stb.append(",AVG_CLS AS ( ");
			stb.append("    SELECT  COUNT(*) AS CNT_CLS, ");
			stb.append("            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS AVG_CLS, ");
			stb.append("            SUBCLASSCD ");
			stb.append("    FROM    SCHNO_A T1 ");
			stb.append("            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
			stb.append("    WHERE   GRADE||HR_CLASS = '" + param[2] + "' ");
			stb.append("    GROUP BY SUBCLASSCD ");
			stb.append("    ) ");

		    //学年：平均点および順位の人数
			stb.append(",AVG_GRD AS ( ");
			stb.append("    SELECT  COUNT(*) AS CNT_GRD, ");
			stb.append("            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS AVG_GRD, ");
			stb.append("            SUBCLASSCD ");
			stb.append("    FROM    SCHNO_A T1 ");
			stb.append("            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
			stb.append("    GROUP BY SUBCLASSCD ");
			stb.append("    ) ");

		    //順位：個人
			stb.append(",RNK_GRD_CLS AS ( ");
			stb.append("    SELECT  RANK() OVER (PARTITION BY SUBCLASSCD ORDER BY SCORE DESC) AS RNK_GRD, ");
			stb.append("            RANK() OVER (PARTITION BY SUBCLASSCD,HR_CLASS ORDER BY SCORE DESC) AS RNK_CLS, ");
			stb.append("            T2.SCHREGNO, ");
			stb.append("            SUBCLASSCD ");
			stb.append("    FROM    SCHNO_A T1 ");
			stb.append("            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
			stb.append("    ) ");

		    //総合成績：学級順位：個人
			stb.append(",RNK_CLS_SCH AS ( ");
			stb.append("    SELECT  T2.SCHREGNO, ");
			stb.append("            RANK() OVER (ORDER BY AVG(FLOAT(SCORE)) DESC) AS RNK_CLS_SCH ");
			stb.append("    FROM    SCHNO_A T1 ");
			stb.append("            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
			stb.append("    WHERE   GRADE||HR_CLASS = '" + param[2] + "' ");
			stb.append("    GROUP BY T2.SCHREGNO ");
			stb.append("    ) ");

		    //総合成績：学年順位：個人
			stb.append(",RNK_GRD_SCH AS ( ");
			stb.append("    SELECT  T2.SCHREGNO, ");
			stb.append("            RANK() OVER (ORDER BY AVG(FLOAT(SCORE)) DESC) AS RNK_GRD_SCH ");
			stb.append("    FROM    SCHNO_A T1 ");
			stb.append("            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
			stb.append("    GROUP BY T2.SCHREGNO ");
			stb.append("    ) ");

		    //総合成績：学級順位の人数
			stb.append(",CNT_CLS AS ( ");
			stb.append("    SELECT  COUNT(*) AS CNT,'CMN' AS CMN_FLG FROM RNK_CLS_SCH ");
			stb.append("    ) ");

		    //総合成績：学年順位の人数
			stb.append(",CNT_GRD AS ( ");
			stb.append("    SELECT  COUNT(*) AS CNT,'CMN' AS CMN_FLG FROM RNK_GRD_SCH ");
			stb.append("    ) ");

		    //総合成績：学級平均
			stb.append(",CLSAVG AS ( ");
			stb.append("    SELECT  'CMN' AS CMN_FLG, ");
			stb.append("            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS CLSAVG ");
			stb.append("    FROM    SCHNO_A T1 ");
			stb.append("            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
			stb.append("    WHERE   GRADE||HR_CLASS = '" + param[2] + "' ");
			stb.append("    ) ");

		    //総合成績：学年平均
			stb.append(",GRDAVG AS ( ");
			stb.append("    SELECT  'CMN' AS CMN_FLG, ");
			stb.append("            DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS GRDAVG ");
			stb.append("    FROM    SCHNO_A T1 ");
			stb.append("            INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
			stb.append("    ) ");

			//メイン表
			stb.append("SELECT  T1.HR_NAME, ");
			stb.append("        T1.SCHREGNO, ");
			stb.append("        T1.ATTENDNO, ");
			stb.append("        T1.NAME, ");
			stb.append("        T2.SUBCLASSCD, ");
			stb.append("        T5.SUBCLASSABBV, ");
			stb.append("		T2.SCORE, ");
			stb.append("		S1.AVG_CLS, ");
			stb.append("		S2.AVG_GRD, ");
			stb.append("		RTRIM(CHAR(S3.RNK_CLS))||'/'||RTRIM(CHAR(S1.CNT_CLS)) AS RNK_CLS, ");
			stb.append("		RTRIM(CHAR(S3.RNK_GRD))||'/'||RTRIM(CHAR(S2.CNT_GRD)) AS RNK_GRD, ");
			stb.append("		G1.CLSAVG,G2.GRDAVG, ");
			stb.append("		RTRIM(CHAR(R1.RNK_CLS_SCH))||'/'||RTRIM(CHAR(R3.CNT)) AS RNKAVG_CLS, ");
			stb.append("		RTRIM(CHAR(R2.RNK_GRD_SCH))||'/'||RTRIM(CHAR(R4.CNT)) AS RNKAVG_GRD ");
			stb.append("FROM    SCHNO_A T1 ");
			stb.append("        INNER JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
			stb.append("		LEFT JOIN SUBCLASS_MST T5 ON T5.SUBCLASSCD = T2.SUBCLASSCD ");
			stb.append("		LEFT JOIN AVG_CLS S1 ON S1.SUBCLASSCD = T2.SUBCLASSCD ");
			stb.append("		LEFT JOIN AVG_GRD S2 ON S2.SUBCLASSCD = T2.SUBCLASSCD ");
			stb.append("		LEFT JOIN RNK_GRD_CLS S3 ON S3.SCHREGNO=T2.SCHREGNO AND S3.SUBCLASSCD=T2.SUBCLASSCD ");
			stb.append("        LEFT JOIN CLSAVG G1 ON G1.CMN_FLG = T2.CMN_FLG ");
			stb.append("        LEFT JOIN GRDAVG G2 ON G2.CMN_FLG = T2.CMN_FLG ");
			stb.append("        LEFT JOIN RNK_CLS_SCH R1 ON R1.SCHREGNO = T2.SCHREGNO ");
			stb.append("        LEFT JOIN RNK_GRD_SCH R2 ON R2.SCHREGNO = T2.SCHREGNO ");
			stb.append("        LEFT JOIN CNT_CLS R3 ON R3.CMN_FLG = T2.CMN_FLG ");
			stb.append("        LEFT JOIN CNT_GRD R4 ON R4.CMN_FLG = T2.CMN_FLG ");
			stb.append("WHERE   T1.SCHREGNO IN " + param[5] + " ");
			stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T2.SUBCLASSCD ");
		} catch( Exception ex ){
			log.warn("prestatementAttend error!",ex);
		}
		return stb.toString();
	}


	/** 
     *  get parameter doGet()パラメータ受け取り 
	 */
    private void getParam( HttpServletRequest request, String param[] ){

	    //param = new String[9];
		try {
		    printname = request.getParameter("PRINTNAME");   			    //プリンタ名
		    schno = request.getParameterValues("category_selected");	    //学籍番号
	        param[0] = request.getParameter("YEAR");         				//年度
			param[1] = request.getParameter("GAKKI");   					//1-3:学期
			param[2] = request.getParameter("GRADE_HR_CLASS");  			//学年・組
			param[3] = request.getParameter("TESTKINDCD");  			    //中間:01,期末:02
			param[4] = param[2].substring(0,2);  			                //学年
		} catch( Exception ex ) {
			log.error("request.getParameter error!",ex);
		}
    }


	/** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf){

		try {
            outstrm = new PrintWriter (response.getOutputStream());
            if( printname!=null )	response.setContentType("text/html");
            else					response.setContentType("application/pdf");

            int ret = svf.VrInit();						   		//クラスの初期化

            if( printname!=null ){
                ret = svf.VrSetPrinter("", printname);			//プリンタ名の設定
                if( ret < 0 ) log.info("printname ret = " + ret);
            } else
                ret = svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定
 		} catch( java.io.IOException ex ){
			log.error("db new error:" + ex);
		}
  }


	/** svf close */
    private void closeSvf(Vrw32alp svf){
		if( printname!=null ){
			outstrm.println("<HTML>");
			outstrm.println("<HEAD>");
			outstrm.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=euc-jp\">");
			outstrm.println("</HEAD>");
			outstrm.println("<BODY>");
			if( !nonedata )	outstrm.println("<H1>対象データはありません。</h1>");
			else			outstrm.println("<H1>印刷しました。</h1>");
			outstrm.println("</BODY>");
			outstrm.println("</HTML>");
		} else if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "");
			ret = svf.VrEndPage();
		}
		int ret = svf.VrQuit();
		if( ret == 0 )log.info("===> VrQuit():" + ret);
		outstrm.close();			//ストリームを閉じる 
    }


	/** DB set */
	private DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException{
		DB2UDB db2 = null;
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME")	, "db2inst1", "db2inst1", DB2UDB.TYPE2);	//Databaseクラスを継承したクラス
		} catch( Exception ex ){
			log.error("db new error:" + ex);
			if( db2 != null)db2.close();
		}
		return db2;
	}


	/** DB open */
	private boolean openDb(DB2UDB db2){
		try {
			db2.open();
		} catch( Exception ex ){
			log.error("db open error!"+ex );
			return true;
		}//try-cathの括り
		return false;
	}//private boolean Open_db()


	/** DB close */
	private void closeDb(DB2UDB db2){
		try {
			db2.commit();
			db2.close();
		} catch( Exception ex ){
			log.error("db close error!"+ex );
		}//try-cathの括り
	}//private Close_Db()


}//クラスの括り
