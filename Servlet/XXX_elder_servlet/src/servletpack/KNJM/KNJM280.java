package servletpack.KNJM;

import java.io.IOException;
import java.io.OutputStream;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [通信制]
 *
 *					＜ＫＮＪＭ２８０＞  レポート入力チェックリスト
 *
 *	2005/04/19 m-yama 作成日
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJM280 extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJM280.class);
	int len = 0;			//列数カウント用
	int ccnt    = 0;
	String staffnm[];
	String staffcd[];
	boolean nonedata = false;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[]  = new String[8];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         						//日付
			param[1] = request.getParameter("STAFF");   							//学期
			param[2] = request.getParameter("OUTPUT");   							//出力種別
	        param[3] = request.getParameter("DATE");         						//日付
            param[7] = request.getParameter("useCurriculumcd");                     //教育課程
log.debug("class"+param[0]);
log.debug("date"+param[1]);
		} catch( Exception ex ) {
			log.error("Param read error!");
		}
	//	print設定
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	//	svf設定
		int ret = svf.VrInit();						   	//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetSpoolFileStream(outstrm);   		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!");
		}


	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		Set_Head(db2,svf,param);								//見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJM280]param["+ia+"]="+param[ia]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(Pre_Stat1(param));		//設定データpreparestatement
			ps2 = db2.prepareStatement(Pre_Stat2(param));		//講座担当コードpreparestatement
			ps3 = db2.prepareStatement(Pre_Stat3(param));		//講座担当コードpreparestatement
		} catch( Exception ex ) {
			log.error("SQL read error!");
		}
		//カウンタ
		//SVF出力
		//固定項目GET

		if (param[2].equals("2")) {
			if (param[1].equals("0")) {
				Allclassdata(db2,svf,param,ps3);
log.debug("alltantou");
			}else {
				Classdata(db2,svf,param,ps2);
log.debug("tantou");
			}
			for( int ia=0 ; ia<staffnm.length ; ia++ ){
				try {
log.debug("staff"+staffcd[ia]);
log.debug("data"+staffnm[ia]);
				} catch( Exception ex ) {
					log.error("SQL read error!");
				}
				if( Set_Detail_1(db2,svf,param,staffnm[ia],staffcd[ia],ps1) );
			}
		}else {
				if( Set_Detail_2(db2,svf,param,ps1) );
		}
	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		preStatClose(ps1,ps2,ps3);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** SVF-FORM **/
	private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		if (param[2].equals("1")){
			ret = svf.VrSetForm("KNJM280_1.frm", 1);
		}else {
			ret = svf.VrSetForm("KNJM280_2.frm", 1);
		}
	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			param[6] = KNJ_EditDate.h_format_thi(returnval.val3,0);
		} catch( Exception ex ){
			log.error("setHeader set error!");
		}

	}//Set_Head()の括り

	/** SVF-FORM **/
	private void Allclassdata(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps3){

		int Allcnt = 0;
		try {
log.debug("Allclass");
			ResultSet rs3 = ps3.executeQuery();
			while( rs3.next() ){
				Allcnt++;
			}
			rs3.close();
		} catch( Exception ex ) {
			log.error("SQL read error!");
		}

		staffnm = new String[Allcnt];
		staffcd = new String[Allcnt];

		try {
			ResultSet rs3 = ps3.executeQuery();
			while( rs3.next() ){
				staffnm[ccnt] = rs3.getString("STAFFNAME");
				staffcd[ccnt] = rs3.getString("STAFFCD");
				ccnt++;
			}
			rs3.close();
		} catch( Exception ex ) {
			log.error("SQL read error!");
		}

	}//Set_Head()の括り

	/** SVF-FORM **/
	private void Classdata(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps2){

		int Allcnt = 0;
		try {
			ResultSet rs2 = ps2.executeQuery();
			while( rs2.next() ){
				Allcnt++;
			}
			rs2.close();
		} catch( Exception ex ) {
			log.error("SQL read error!");
		}

		staffnm = new String[Allcnt];
		staffcd = new String[Allcnt];
log.debug("classdata");

		try {
			ResultSet rs2 = ps2.executeQuery();
			while( rs2.next() ){
				staffnm[ccnt] = rs2.getString("STAFFNAME");
				staffcd[ccnt] = rs2.getString("STAFFCD");
				ccnt++;
			}
			rs2.close();
		} catch( Exception ex ) {
			log.error("SQL read error!");
		}

	}//Set_Head()の括り


	/**SVF-FORM**/
	private boolean Set_Detail_1(
	DB2UDB db2,
	Vrw32alp svf,
	String param[],
	String staffnm,
	String staffcd,
	PreparedStatement ps1)
	{
log.debug("main");
		boolean dataflg  = false;	//データフラグ
		int ret     = 0;
        if (false && 0 != ret) { ret = 0; }
		int kensuu  = 0;			//件数
		String seqflg  ;			//回数設定
		String nenhitoketa ;		//年1桁
		String subclass    ;		//科目コード
		String kaisuu      ;		//回数
		String saisuu      ;		//再提出
		String reprtno     ;		//レポート番号
		try {
			ps1.setString(1,staffcd);	//講座コード
log.debug("start");
			ResultSet rs = ps1.executeQuery();
log.debug("end");
			int gyo   = 1;			//行数カウント用
			while( rs.next() ){
				if ( gyo > 50 ){
					gyo = 1;
					ret = svf.VrEndPage();					//SVFフィールド出力
				}

				//ヘッダ出力
				ret = svf.VrsOut("NENDO1" 		, String.valueOf(param[0]) );
				ret = svf.VrsOut("GRAD_DATE" 	, String.valueOf(param[3]) );
				ret = svf.VrsOut("DATE"			, String.valueOf(param[6]) );
				//担当名・レポート番号・学籍・生徒・科目名・再提出・回数・受付月日・返信日付・評価
				nenhitoketa = param[0].substring(3);
				kaisuu      = rs.getString("STANDARD_SEQ");
log.debug("kai"+String.valueOf(kaisuu));
				if (Integer.parseInt(kaisuu) > 9){
					kaisuu      = rs.getString("STANDARD_SEQ");
				}else {
					kaisuu      = "0" + rs.getString("STANDARD_SEQ");
				}
				subclass    = rs.getString("SUBCLASSCD");
				saisuu      = rs.getString("REPRESENT_SEQ");
				reprtno     = String.valueOf(nenhitoketa) + String.valueOf(subclass) + String.valueOf(kaisuu) + String.valueOf(saisuu);
log.debug("nen"+String.valueOf(nenhitoketa));
log.debug("kai"+String.valueOf(kaisuu));
log.debug("sub"+String.valueOf(subclass));
log.debug("sai"+String.valueOf(saisuu));
log.debug("repno"+String.valueOf(reprtno));

				ret = svf.VrsOutn("reportnum"		,gyo, String.valueOf(reprtno));
				ret = svf.VrsOutn("SCHREGNO"		,gyo, rs.getString("SCHREGNO"));
				ret = svf.VrsOutn("SCHREGNAME"		,gyo, rs.getString("NAME"));
				ret = svf.VrsOutn("SUBCLASSNAME"		,gyo, rs.getString("SUBCLASSABBV"));
				if (rs.getString("STANSEQFLG").equals("A")){
					seqflg = String.valueOf("第");
					seqflg = seqflg + rs.getString("STANDARD_SEQ");
					seqflg = seqflg + String.valueOf("回");
				}else {
					seqflg = String.valueOf("");
				}
				ret = svf.VrsOutn("SEQ"		,gyo, String.valueOf(seqflg));

				if (rs.getString("REPSEQFLG").equals("A") && !rs.getString("REPRESENT_SEQ").equals("0")){
					seqflg = rs.getString("REPRESENT_SEQ");
					seqflg = seqflg + String.valueOf("回");
				}else {
					seqflg = String.valueOf("");
				}
				ret = svf.VrsOutn("SAI"		,gyo, String.valueOf(seqflg));
				//担当名・評価
				if (param[2].equals("2")){
					ret = svf.VrsOut("ATTESTOR"		, String.valueOf(staffnm) );
					ret = svf.VrsOutn("GRAD_VALUE"		,gyo, rs.getString("NAME1"));
				}

				nonedata = true;
				dataflg  = true;
				kensuu++;
				gyo++;			//行数カウント用
			}
			if (dataflg){
				ret = svf.VrsOut("kensuu"		,"計　　" + String.valueOf(kensuu) + "件");
				ret = svf.VrEndPage();
log.debug("syuturyoku");
			}
			rs.close();

		} catch( Exception ex ) {
			log.error("Set_Detail_1 read error!");
		}
		return nonedata;

	}//Set_Detail_1()の括り


	/**SVF-FORM**/
	private boolean Set_Detail_2(
	DB2UDB db2,
	Vrw32alp svf,
	String param[],
	PreparedStatement ps1)
	{
log.debug("main");
		boolean dataflg  = false;	//データフラグ
		int ret     = 0;
        if (false && 0 != ret) { ret = 0; }
		int kensuu  = 0;			//件数
		String seqflg  ;			//回数設定
		String nenhitoketa ;		//年1桁
		String subclass    ;		//科目コード
		String kaisuu      ;		//回数
		String saisuu      ;		//再提出
		String reprtno     ;		//レポート番号
		try {
//			ps1.setString(1,staffcd);	//講座コード
log.debug("start");
			ResultSet rs = ps1.executeQuery();
log.debug("end");
			int gyo   = 1;			//行数カウント用
			while( rs.next() ){
				if ( gyo > 50 ){
					gyo = 1;
					ret = svf.VrEndPage();					//SVFフィールド出力
				}

				//ヘッダ出力
				ret = svf.VrsOut("NENDO1" 		, String.valueOf(param[0]) );
				ret = svf.VrsOut("GRAD_DATE" 	, String.valueOf(param[3]) );
				ret = svf.VrsOut("DATE"			, String.valueOf(param[6]) );
				//担当名・レポート番号・学籍・生徒・科目名・再提出・回数・受付月日・返信日付・評価
				nenhitoketa = param[0].substring(3);
				kaisuu      = rs.getString("STANDARD_SEQ");
log.debug("kai"+String.valueOf(kaisuu));
				if (Integer.parseInt(kaisuu) > 9){
					kaisuu      = rs.getString("STANDARD_SEQ");
				}else {
					kaisuu      = "0" + rs.getString("STANDARD_SEQ");
				}
				subclass    = rs.getString("SUBCLASSCD");
				saisuu      = rs.getString("REPRESENT_SEQ");
				reprtno     = String.valueOf(nenhitoketa) + String.valueOf(subclass) + String.valueOf(kaisuu) + String.valueOf(saisuu);
log.debug("nen"+String.valueOf(nenhitoketa));
log.debug("kai"+String.valueOf(kaisuu));
log.debug("sub"+String.valueOf(subclass));
log.debug("sai"+String.valueOf(saisuu));
log.debug("repno"+String.valueOf(reprtno));

				ret = svf.VrsOutn("reportnum"		,gyo, String.valueOf(reprtno));
				ret = svf.VrsOutn("SCHREGNO"		,gyo, rs.getString("SCHREGNO"));
				ret = svf.VrsOutn("SCHREGNAME"		,gyo, rs.getString("NAME"));
				ret = svf.VrsOutn("SUBCLASSNAME"		,gyo, rs.getString("SUBCLASSABBV"));
				if (rs.getString("STANSEQFLG").equals("A")){
					seqflg = String.valueOf("第");
					seqflg = seqflg + rs.getString("STANDARD_SEQ");
					seqflg = seqflg + String.valueOf("回");
				}else {
					seqflg = String.valueOf("");
				}
				ret = svf.VrsOutn("SEQ"		,gyo, String.valueOf(seqflg));

				if (rs.getString("REPSEQFLG").equals("A") && !rs.getString("REPRESENT_SEQ").equals("0")){
					seqflg = rs.getString("REPRESENT_SEQ");
					seqflg = seqflg + String.valueOf("回");
				}else {
					seqflg = String.valueOf("");
				}
				ret = svf.VrsOutn("SAI"		,gyo, String.valueOf(seqflg));

				nonedata = true;
				dataflg  = true;
				kensuu++;
				gyo++;			//行数カウント用
			}
			if (dataflg){
				ret = svf.VrsOut("kensuu"		,"計　　" + String.valueOf(kensuu) + "件");
				ret = svf.VrEndPage();
log.debug("syuturyoku");
			}
			rs.close();

		} catch( Exception ex ) {
			log.error("Set_Detail_1 read error!");
		}
		return nonedata;

	}//Set_Detail_2()の括り

	/**PrepareStatement作成**/
	private String Pre_Stat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT ");
            if ("1".equals(param[7])) {
                stb.append("     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     t1.SUBCLASSCD, ");
            }
			stb.append("    t1.SCHREGNO, ");
			stb.append("    t2.NAME, ");
			stb.append("    t3.SUBCLASSABBV, ");
			stb.append("    t1.REPRESENT_SEQ, ");
			stb.append("    CASE WHEN REPRESENT_SEQ IS NULL THEN 'N' ELSE 'A' END AS REPSEQFLG, ");
			stb.append("    t1.STANDARD_SEQ, ");
			stb.append("    CASE WHEN STANDARD_SEQ IS NULL THEN 'N' ELSE 'A' END AS STANSEQFLG, ");
			stb.append("    t1.RECEIPT_DATE, ");
			stb.append("    t1.GRAD_DATE, ");
			stb.append("    t4.NAME1 ");
			stb.append("FROM ");
			stb.append("    REP_PRESENT_DAT t1 LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ");
			stb.append("    LEFT JOIN SUBCLASS_MST t3 ON t1.SUBCLASSCD = t3.SUBCLASSCD ");
            if ("1".equals(param[7])) {
                stb.append("       AND t1.CLASSCD = t3.CLASSCD ");
                stb.append("       AND t1.SCHOOL_KIND = t3.SCHOOL_KIND ");
                stb.append("       AND t1.CURRICULUM_CD = t3.CURRICULUM_CD ");
            }
			stb.append("    LEFT JOIN V_NAME_MST t4 ON t1.GRAD_VALUE = t4.NAMECD2 AND  t4.YEAR = t1.YEAR AND t4.NAMECD1 = 'M003' ");
			stb.append("WHERE ");
			stb.append("    t1.YEAR = '"+param[0]+"' AND ");
			if (param[2].equals("2")){
				stb.append("    t1.STAFFCD = ? AND ");
			}
			if (param[2].equals("1")){
				stb.append("    t1.RECEIPT_DATE = '"+param[3].replace('/','-')+"' ");
			}else {
				stb.append("    t1.GRAD_DATE = '"+param[3].replace('/','-')+"' ");
			}
			stb.append("ORDER BY ");
            if (param[2].equals("2")){
                stb.append("    t1.STAFFCD, ");
            }
            stb.append("    t1.SCHREGNO, ");
            if ("1".equals(param[7])) {
                stb.append("       t1.CLASSCD, ");
                stb.append("       t1.SCHOOL_KIND, ");
                stb.append("       t1.CURRICULUM_CD, ");
            }
            stb.append("    t1.SUBCLASSCD, ");
			stb.append("    t1.STANDARD_SEQ ");
log.debug(stb);
		} catch( Exception e ){
			log.error("Pre_Stat1 error!");
		}
		return stb.toString();

	}//Pre_Stat1()の括り


	/**担当者指定時抽出**/
	private String Pre_Stat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT ");
			stb.append("    t1.STAFFCD, ");
			stb.append("    t2.STAFFNAME ");
			stb.append("FROM ");
			stb.append("    REP_PRESENT_DAT t1 LEFT JOIN STAFF_MST t2 ON t1.STAFFCD = t2.STAFFCD ");
			stb.append("WHERE ");
			stb.append("    t1.YEAR = '"+param[0]+"' AND ");
			stb.append("    t1.STAFFCD = '"+param[1]+"' ");
			stb.append("GROUP BY ");
			stb.append("    t1.STAFFCD, ");
			stb.append("    t2.STAFFNAME ");

		} catch( Exception e ){
			log.error("Pre_Stat2 error!");
		}
		return stb.toString();

	}//Pre_Stat2()の括り

	/**全担当者抽出**/
	private String Pre_Stat3(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {

			stb.append("SELECT ");
			stb.append("    t1.STAFFCD, ");
			stb.append("    t2.STAFFNAME ");
			stb.append("FROM ");
			stb.append("    REP_PRESENT_DAT t1 LEFT JOIN STAFF_MST t2 ON t1.STAFFCD = t2.STAFFCD ");
			stb.append("WHERE ");
			stb.append("    t1.YEAR = '"+param[0]+"' AND ");
			stb.append("    t1.STAFFCD IS NOT NULL ");
			stb.append("GROUP BY ");
			stb.append("    t1.STAFFCD, ");
			stb.append("    t2.STAFFNAME ");

log.debug(stb);
		} catch( Exception e ){
			log.error("Pre_Stat3 error!");
		}
		return stb.toString();

	}//Pre_Stat3()の括り

	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps1,
		PreparedStatement ps2,
		PreparedStatement ps3
	) {
		try {
			ps1.close();
			ps2.close();
			ps3.close();
		} catch( Exception ex ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り

}//クラスの括り
