/**
 *
 *	学校教育システム 賢者 [入試管理] 入学許可証
 *
 *					＜ＫＮＪＬ３４３Ｈ＞  入学許可証
 *
 *	2007/11/19 RTS 作成日
 *
 **/

package servletpack.KNJL;

import java.io.File;
import java.io.IOException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_Get_Info;



public class KNJL343H {

    private static final Log log = LogFactory.getLog(KNJL343H.class);
	private boolean nonedata;
    private String param[];
    private	static String arraykansuuji[] = {"〇","一","二","三","四","五","六","七","八","九","十"};
	private Map hmm = new HashMap();			//組アルファベットの変換用
	private static final char hSpace = ' ';	//全角半角混在フィードの前後空白削除用
	private static final char zSpace = '　'; //全角半角混在フィードの前後空白削除用

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

        int ret = svf.VrSetForm("KNJL343H.frm", 4);
        if (false && 0 != ret) { ret = 0; }
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;

        try {
            ps1 = db2.prepareStatement( statementPermission() );
            ps2 = db2.prepareStatement( getPrincipalNameAndSchoolName() );
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
        printSvfMain(db2, svf, ps1, ps2);

        try {
            if( ps1 != null ) ps1.close();
            if( ps2 != null ) ps2.close();
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
    }

    private String getImagePath(DB2UDB db2) {
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        String folderpath = null;
        try {
            returnval = getinfo.Control(db2);
            folderpath = returnval.val4;      //格納フォルダ
        } catch( Exception e ){
            log.error("setHeader set error!");
        }
        String path = param[9] + "/";
        if (folderpath != null) {
            path += folderpath + "/";
        }
        return path;
    }

	/**帳票出力（各通知書をセット）**/
	private void printSvfMain(
		DB2UDB db2,
		Vrw32alp svf,
		PreparedStatement ps1,
		PreparedStatement ps2
	) {
		int ret = 0;
		String sSchool_Name = "";
		String sPrincipal_Name = "";
        String sJob_Name = "";
        if (false && 0 != ret) { ret = 0; }
		try {
			ResultSet rs  = ps1.executeQuery();
			ResultSet rs2 = ps2.executeQuery();

			// 学校名、校長名を取得
            if( rs2.next() ){
            	sSchool_Name    = rs2.getString("SCHOOL_NAME");
            	sPrincipal_Name = rs2.getString("PRINCIPAL_NAME");
                sJob_Name       = rs2.getString("JOB_NAME");
            }
            String stamp_check = getImagePath(db2) + "SCHOOLSTAMP_JL.jpg";  //学校長印
            File stampFile = new File(stamp_check);
            log.debug("stamp file path="+stampFile.getAbsolutePath()+", exists="+stampFile.exists());

			while( rs.next() ){
				ret = svf.VrsOut("SCHOOL" 	,Ztrim(sSchool_Name));		// 学校名
				ret = svf.VrsOut("NAME2" 	,Ztrim(sPrincipal_Name));	// 校長名
                ret = svf.VrsOut("JOB_NAME" , Ztrim(sJob_Name));        // 役職名
				ret = svf.VrsOut(setformatArea("NAME1", 16, rs.getString("NAME")) 	,rs.getString("NAME"));
				ret = svf.VrsOut("DATE"		,param[3]);					             		// 通知日付
                ret = svf.VrsOut("EXAMNO"   ,rs.getString("EXAMNO"));   // 受験番号
                if (stampFile.exists()) ret = svf.VrsOut("STAMP", stamp_check);
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
			stb.append(" SELECT  W1.EXAMNO,W1.NAME ");
			stb.append(" FROM    ENTEXAM_APPLICANTBASE_DAT W1 ");
	        stb.append(" INNER   JOIN ENTEXAM_RECEPT_DAT W2 ON");
	        stb.append("            W2.ENTEXAMYEAR  = W1.ENTEXAMYEAR  and");
	        stb.append("            W2.APPLICANTDIV = W1.APPLICANTDIV and");
	        stb.append("            W2.EXAMNO       = W1.EXAMNO");
			stb.append(" WHERE   W1.ENTEXAMYEAR = '" + param[0] + "' AND ");
			stb.append("         W1.APPLICANTDIV     = '" + param[7] + "' AND ");	//基礎データ・入試制度
			stb.append("         W2.TESTDIV     = '" + param[8] + "' AND ");		//受付データ・入試区分
			if (param[5].equals("2")) {
				stb.append("     W1.EXAMNO = '" + param[6] + "' AND ");				//受験者指定の場合
			}
			// 繰上合格者判定
            stb.append("         (( W2.JUDGEDIV = '1' ) OR ");
            stb.append("          ( W2.JUDGEDIV = '3' AND W1.JUDGEMENT = '1' AND W1.SPECIAL_MEASURES = '1')) ");
			stb.append("GROUP BY  W1.EXAMNO,W1.NAME ");
			stb.append("ORDER BY  W1.EXAMNO,W1.NAME ");
		} catch( Exception e ){
			log.error("statementPermission error!");
		}
		return stb.toString();

	}

	/**
     *  学校名及び校長名を取得
     **/
	private String getPrincipalNameAndSchoolName()
	{
		StringBuffer stb = new StringBuffer();
		
		// 学校名および校長名を取得する際は前年度を取得する
		int iyear = Integer.valueOf(param[0]).intValue();
		String searchYear = String.valueOf(iyear - 1);
		
	//	パラメータ（なし）
		try {
			stb.append("SELECT  SCHOOL_NAME, PRINCIPAL_NAME, JOB_NAME ");
			stb.append("FROM    CERTIF_SCHOOL_DAT ");
			stb.append("WHERE   YEAR = '" + searchYear + "' AND ");
			stb.append(        "CERTIF_KINDCD = '105'");
		} catch( Exception e ){
			log.error("getPrincipalNameAndSchoolName error!");
		}
		return stb.toString();

	}


	/** get parameter doGet()パラメータ受け取り */
    private void getParam(HttpServletRequest request){
	    param = new String[14];
		try {
	        param[0] = request.getParameter("YEAR");								// 年度
	        
			String sDate_yyyy	= request.getParameter("NOTICEDAY").substring(0,4); // 日付(年)
			String sDate_mm		= request.getParameter("NOTICEDAY").substring(5,7); // 日付(月)
			String sDate_dd		= request.getParameter("NOTICEDAY").substring(8,10);// 日付(日)
			// 漢数字に変換
			sDate_yyyy = convertKansuuji(sDate_yyyy);
			sDate_mm = convertKansuuji(Integer.valueOf(sDate_mm).intValue());
			sDate_dd = convertKansuuji(Integer.valueOf(sDate_dd).intValue());
			// YYYY年M月DD日の形式に設定
			param[3] =  sDate_yyyy + "年" + sDate_mm + "月" + sDate_dd + "日";
			
			if( request.getParameter("OUTPUT") != null ) param[5] = request.getParameter("OUTPUT");		//受験者(1,5) 1:全員2:指定
            if( request.getParameter("EXAMNO") != null ) param[6] = request.getParameter("EXAMNO");		//受験番号
            if( request.getParameter("APDIV") != null ) param[7] = request.getParameter("APDIV");		//入試制度
            if( request.getParameter("TESTDIV") != null ) param[8] = request.getParameter("TESTDIV");	//入試区分
            if( request.getParameter("DOCUMENTROOT") != null) param[9] = request.getParameter("DOCUMENTROOT");
		} catch( Exception ex ) {
			System.out.println("get parameter error!" + ex);
		}
		for(int i=0 ; i<param.length ; i++) if( param[i] != null ) log.debug("[KNJL313]param[" + i + "]=" + param[i]);
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


	/**
     *  svf print 日付を漢数字変換 
     */
    private String getKansuujiWareki(String hdate)
	{
        StringBuffer stb = new StringBuffer();
		try{
            boolean dflg = false;		//数値？
            int ia = 0;
            for( int i=0 ; i<hdate.length() ; i++ ){
                if( ( Character.isDigit(hdate.charAt(i)) && !dflg ) || ( !Character.isDigit(hdate.charAt(i)) && dflg ) ){
                    if( i == 0 )continue;
                    if( !dflg )stb.append(hdate.substring(ia,i));
                    else      stb.append(convertKansuuji(Integer.parseInt(hdate.substring(ia,i))));
                    ia = i;
                    dflg = Character.isDigit(hdate.charAt(i));
                }
            }
            if( ia > 0 )stb.append(hdate.substring(ia));
		} catch( Exception ex ){
			log.error("getGraduateDate error!",ex);
        }
        return stb.toString();
    }
    
	/**
     *  svf print 数字を漢数字へ変換.百の位まで(数値単位) 
     */
    private String convertKansuuji(int suuji)
	{
		StringBuffer stb = new StringBuffer();
		int kurai = (String.valueOf(suuji)).length();
		if(Integer.parseInt((String.valueOf(suuji)).substring(kurai-1)) != 0){
			if( kurai > 0 ) stb.append( arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai-1))] );
		}
		if( kurai >= 2 ){
            stb.insert(0,"十");
            if( Integer.parseInt((String.valueOf(suuji)).substring(kurai-2,kurai-1)) > 1 )
                stb.insert(0, arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai-2,kurai-1))] );
        }
		if( kurai >= 3 ){
            stb.insert(0,"百");
            if( Integer.parseInt((String.valueOf(suuji)).substring(kurai-3,kurai-2)) > 1 )
		        stb.insert(0, arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai-3,kurai-2))] );
        }
		stb.append("");

		return stb.toString();
	}

	/**
     *  svf print 数字を漢数字へ変換(文字単位) 
     */
    private String convertKansuuji(String suuji)
	{
		StringBuffer stb = new StringBuffer();
		for( int i=0 ; i<suuji.length() ; i++ ){
			if( Character.isDigit(suuji.charAt(i)) )
                stb.append( arraykansuuji[Integer.parseInt(suuji.substring(i,i+1))] );
			else{
                if( hmm.get(suuji.substring(i,i+1)) == null )
                    stb.append( suuji.substring(i,i+1) );
                else
                    stb.append( (hmm.get(suuji.substring(i,i+1))).toString() );
            }
		}
		stb.append("");
		return stb.toString();
	}

    /**
     * 日付をフォーマットYYYY年MM月DD日に設定する
     * @param s
     * @return
     */
    private String fomatSakuseiDate(String cnvDate) {

    	String retDate = "";
    	try {
			DateFormat foramt = new SimpleDateFormat("yyyy/MM/dd"); 
			//文字列よりDate型へ変換
			Date date1 = foramt.parse(cnvDate); 
			// 年月日のフォーマットを指定
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'M'月'dd'日'");
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
    	if(area_len > sval.length()){
   			retAreaName = area_name + "_1";
    	} else {
   			retAreaName = area_name + "_2";
    	}
        return retAreaName;
    }

	/* 全角スペースも含めてtrimする */ 
	private static String Ztrim(String original){
		char c;
		int head = 0;
		int length = original.length();
		int tail = length - 1; //最後の文字を指す

		while (head < length 
		    && ((c = original.charAt(head)) <= hSpace || c == zSpace)){
		  ++head;
		}
		while (tail > head 
		    && ((c = original.charAt(tail)) <= hSpace || c == zSpace)){
		    --tail;
		}
		//substring()の第二引数は最後の文字の直後を指定する
		return original.substring(head, tail + 1); 
	}
    
}//クラスの括り
