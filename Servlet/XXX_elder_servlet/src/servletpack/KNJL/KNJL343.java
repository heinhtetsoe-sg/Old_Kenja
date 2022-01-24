/**
 *
 *	学校教育システム 賢者 [入試管理] 入学許可証
 *
 *	2005/01/13 yamashiro
 *	2005/02/10 nakamoto 「入学区分＝２：辞退」は対象外 NO001
 *
 **/

package servletpack.KNJL;

import java.io.IOException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;


public class KNJL343 {

    private static final Log log = LogFactory.getLog(KNJL343.class);
	private boolean nonedata;
    private String param[];

	/**
	  *
	  *  KNJD.classから最初に起動されるクラス
	  *
	  **/
	public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
			System.out.println("db open error");
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

        int ret = svf.VrSetForm("KNJL343.frm", 4);
        if (false && 0 != ret) { ret = 0; }
		PreparedStatement ps1 = null;

        try {
            ps1 = db2.prepareStatement( statementPermission() );
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
        printSvfMain(db2, svf, ps1);

        try {
            if( ps1 != null ) ps1.close();
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
    }



	/**帳票出力（各通知書をセット）**/
	private void printSvfMain(
		DB2UDB db2,
		Vrw32alp svf,
		PreparedStatement ps1
	) {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
//log.debug("ps1="+ps1.toString());
			ResultSet rs = ps1.executeQuery();

			while( rs.next() ){
				ret = svf.VrsOut("EXAMNO" 	,rs.getString("EXAMNO"));		                //受験番号
                if( rs.getString("NAME") != null  &&  12 < rs.getString("NAME").length() )
                    ret = svf.VrsOut("NAME2" 	,rs.getString("NAME"));			            //名前
                else
                    ret = svf.VrsOut("NAME1" 	,rs.getString("NAME"));			            //名前
				ret = svf.VrsOut("DATE"		,param[3]);					                //通知日付
                ret = svf.VrsOut("ENTCLASS" ,rs.getString("ENTCLASS_NAME"));            //入学クラス
				ret = svf.VrEndRecord();//レコードを出力
				nonedata = true;
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfout set error!");
		}
	}


	/**
     *  入学許可対象者を取得
     **/
	private String statementPermission()
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("SELECT  T1.EXAMNO, T1.NAME, T2.NAME1 AS ENTCLASS_NAME ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(     "LEFT JOIN NAME_MST T2 ON ");
            stb.append(         "T2.NAMECD1 = 'L017' AND ");
            stb.append(         "T2.NAMECD2 = T1.ENTCLASS ");
			stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' AND ");
			if (param[5].equals("2")) {
				stb.append(    "T1.EXAMNO = '" + param[6] + "' AND ");        //受験者指定の場合
                stb.append(    "T1.JUDGEMENT = '1'  ");                       //合格者
            } else {
    			stb.append(        "T1.PROCEDUREDIV = '1'  ");                    //基礎データ・手続区分
    			stb.append("        AND (T1.ENTDIV='1' OR T1.ENTDIV IS NULL) ");     //基礎データ.入学区分 NO001
            }
			stb.append("ORDER BY  T1.EXAMNO ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り


	/** get parameter doGet()パラメータ受け取り */
    private void getParam(HttpServletRequest request){
	    param = new String[14];
		try {
	        param[0] = request.getParameter("YEAR");         				                         //年度
	        //param[3] = request.getParameter("NOTICEDAY");     			                             //作成日
			param[3] = KNJ_EditDate.h_format_JP( request.getParameter("NOTICEDAY") );
            if( request.getParameter("OUTPUT") != null ) param[5] = request.getParameter("OUTPUT");//受験者(1,5) 1:全員2:指定
            if( request.getParameter("EXAMNO") != null ) param[6] = request.getParameter("EXAMNO");//受験番号
		} catch( Exception ex ) {
			System.out.println("get parameter error!" + ex);
		}
for(int i=0 ; i<param.length ; i++) if( param[i] != null ) System.out.println("[KNJL313]param[" + i + "]=" + param[i]);
    }


	/** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf){
		response.setContentType("application/pdf");
		int ret = svf.VrInit();											//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		try {
			ret = svf.VrSetSpoolFileStream(response.getOutputStream());   	//PDFファイル名の設定
 		} catch( java.io.IOException ex ){
			System.out.println("db new error:" + ex);
		}
   }


	/** svf close */
    private void closeSvf(Vrw32alp svf){
		if( !nonedata ){
		 	int ret = svf.VrSetForm("MES001.frm", 0);
            if (false && 0 != ret) { ret = 0; }
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}
		svf.VrQuit();
    }


	/** DB set */
	private DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException{
		DB2UDB db2 = null;
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME")	, "db2inst1", "db2inst1", DB2UDB.TYPE2);	//Databaseクラスを継承したクラス
			//db2 = new DB2UDB("oomiya"	, "db2inst1", "db2inst1", DB2UDB.TYPE2);	//Databaseクラスを継承したクラス
		} catch( Exception ex ){
			System.out.println("db new error:" + ex);
			if( db2 != null)db2.close();
		}
		return db2;
	}


	/** DB open */
	private boolean openDb(DB2UDB db2){
		try {
			db2.open();
		} catch( Exception ex ){
			System.out.println("db open error!"+ex );
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
			System.out.println("db close error!"+ex );
		}//try-cathの括り
	}//private Close_Db()


}//クラスの括り
