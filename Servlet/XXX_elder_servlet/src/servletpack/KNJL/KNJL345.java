package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＬ３４５＞  オリエンテーション名簿
 *
 *	2005/02/03 nakamoto 作成日
 *	2005/02/10 nakamoto 「入学区分＝２：辞退」は対象外 NO001
 **/

public class KNJL345 {

    private static final Log log = LogFactory.getLog(KNJL345.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[3];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
	        param[1] = request.getParameter("APDIV");         				//入試制度
	        param[2] = request.getParameter("OUTPUT");         				//出力順 1:受験番号順,2:氏名かな順
		} catch( Exception ex ) {
			log.error("parameter error!");
		}

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
		int ret = svf.VrInit();						   		//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!");
			return;
		}


	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(preStat1(param));		//名簿一覧preparestatement
			ps2 = db2.prepareStatement(preStat2(param));		//総ページ数preparestatement
            log.debug("preStat2="+preStat2(param));
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		if( setSvfout(db2,svf,param,ps1, ps2) ){							//帳票出力のメソッド
			nonedata = true;
		}

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		preStatClose(ps1,ps2);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** 事前処理 **/
	private void setHeader(
		DB2UDB db2,
		Vrw32alp svf,
		String param[]
	) {
		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetForm("KNJL345.frm", 4);
		ret = svf.VrsOut("NENDO"	,KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			ret = svf.VrsOut("DATE"	,KNJ_EditDate.h_format_JP(returnval.val3));
		} catch( Exception e ){
			log.error("setHeader set error!");
		}

        //  ＳＶＦ属性変更--->改ページ
        ret = svf.VrAttribute("ENTCLASS","FF=1");

        getinfo = null;
		returnval = null;
	}



	/**総ページ数をセット**/
	private Map getTotalPageMap(
		DB2UDB db2,
		Vrw32alp svf,
		PreparedStatement ps2
	) {
        Map totalPageMap = new HashMap();
		try {
			ResultSet rs = ps2.executeQuery();

            int totalPage = 0;
			while( rs.next() ){
				if (rs.getString("TEST_CNT") != null) {
                    Integer page = Integer.valueOf(rs.getString("TEST_CNT"));
					totalPageMap.put(rs.getString("ENTCLASS"), page);
                    totalPage += page.intValue();
                }
            }
            totalPageMap.put("ALL", new Integer(totalPage));
            
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setTotalPage set error!", ex);
		}
		return totalPageMap;
	}



	/**帳票出力（名簿一覧をセット）**/
	private boolean setSvfout(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps1,
        PreparedStatement ps2
	) {
		boolean nonedata = false;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        Map totalPageMap = getTotalPageMap(db2,svf,ps2);                            //総ページ数メソッド
        svf.VrsOut("TOTAL_PAGE" , String.valueOf(totalPageMap.get("ALL")));
		try {
			ResultSet rs = ps1.executeQuery();

			int reccnt_man 		= 0;	//男レコード数カウント用
			int reccnt_woman 	= 0;	//女レコード数カウント用
			int reccnt = 0;				//合計レコード数
			int allPagecnt = 0;			//現在ページ数(通し)
			int eachPagecount = 0;      //入学クラスごとのページ
            int eachPageLast = 0;       //入学クラスごとの最後のページ
            int gyo = 1;				//現在ページ数の判断用（行）
			String honordiv = "";		//特待生記号
			String entclass = "";
			while( rs.next() ){
				//レコードを出力
				if (reccnt > 0) ret = svf.VrEndRecord();
				//５０行超えた場合、ページ数カウント
				if (gyo > 50 || entclass!=null && !entclass.equals(rs.getString("ENTCLASS"))) {
				    if (entclass!=null && !entclass.equals(rs.getString("ENTCLASS"))) {
                        eachPagecount = 0;
                        reccnt_man = 0;
                        reccnt_woman = 0;
                        reccnt = 0;
                    }
                    gyo = 1;
					allPagecnt++;
                    eachPagecount++;
                    entclass = (rs.getString("ENTCLASS") == null) ? "null" : rs.getString("ENTCLASS");
                    eachPageLast = ((Integer) totalPageMap.get(entclass)).intValue();
                    log.debug("entclass = " + entclass + ", each page last = "+eachPageLast + ", each page count = " + eachPagecount);
				}
				//ヘッダ
				ret = svf.VrsOut("PAGE"		,String.valueOf(allPagecnt));			//現在ページ数
				//明細
				honordiv = ( rs.getString("HONORDIV").equals("1") ) ? "※" : "　";//特待生記号
                String name = rs.getString("NAME");
                String kana = rs.getString("NAME_KANA");
                String nameField = (name != null && 10 < name.length()) ? "NAME2" : "NAME";
                String kanaField = (kana != null && 12 < kana.length()) ? "KANA2" : "KANA";
				ret = svf.VrsOut(nameField 	,honordiv + name);	                //名前
				ret = svf.VrsOut("EXAMNO" 	,rs.getString("EXAMNO"));			//受験番号
				ret = svf.VrsOut(kanaField 	,kana);		                        //ふりがな
				ret = svf.VrsOut("SEX" 		,rs.getString("SEX_NAME"));			//性別
				ret = svf.VrsOut("FINSCHOOL",rs.getString("FS_NAME"));			//出身学校名

                ret = svf.VrsOut("ENTCLASS", rs.getString("ENTCLASS_NAME"));
                //レコード数カウント
				reccnt++;
				if (rs.getString("SEX") != null) {
					if (rs.getString("SEX").equals("1")) reccnt_man++;
					if (rs.getString("SEX").equals("2")) reccnt_woman++;
				}
				//現在ページ数判断用
				gyo++;

				nonedata = true;
                ret = svf.VrsOut("NOTE" , (eachPagecount == eachPageLast) ? "男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名" : "");
			}
            if (reccnt > 0) ret = svf.VrEndRecord();
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfout set error!", ex);
		}
		return nonedata;
	}



	/**名簿一覧を取得**/
	private String preStat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("SELECT ");
			stb.append("    T1.EXAMNO, ");
			stb.append("    VALUE(T1.HONORDIV,'0') AS HONORDIV, ");
			stb.append("    T1.NAME, ");
			stb.append("    T1.NAME_KANA, ");
			stb.append("    T1.SEX, ");
			stb.append("    T5.ABBV1 AS SEX_NAME, ");
			stb.append("    T1.FS_NAME,  ");
            stb.append("    VALUE(T1.ENTCLASS, 'null') AS ENTCLASS,  ");
            stb.append("    T6.NAME1 AS ENTCLASS_NAME  ");
			stb.append("FROM ");
			stb.append("    (SELECT W2.EXAMNO, W2.NAME, W2.NAME_KANA, W2.SEX, W2.FS_NAME, W2.HONORDIV, W2.ENTCLASS ");
			stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT W2  ");
			stb.append("     WHERE  W2.ENTEXAMYEAR='"+param[0]+"'  ");
			stb.append("            AND W2.PROCEDUREDIV='1' ");	//基礎データ.手続区分
			stb.append("            AND (W2.ENTDIV='1' OR W2.ENTDIV IS NULL) ");//基礎データ.入学区分 NO001
			stb.append("            AND W2.APPLICANTDIV='"+param[1]+"' ) T1 ");
			stb.append("    LEFT JOIN NAME_MST T5 ON T5.NAMECD1='Z002' AND T5.NAMECD2=T1.SEX ");
            stb.append("    LEFT JOIN NAME_MST T6 ON T6.NAMECD1='L017' AND T6.NAMECD2=T1.ENTCLASS ");
			stb.append("ORDER BY INT(VALUE(T1.ENTCLASS, '0')) DESC, ");
			if (param[2].equals("1")) stb.append("    T1.EXAMNO ");
			if (param[2].equals("2")) stb.append("    T1.NAME_KANA ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り



	/**総ページ数を取得**/
	private String preStat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("    SELECT VALUE(W2.ENTCLASS, 'null') AS ENTCLASS, CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT  ");
			stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT W2  ");
			stb.append("     WHERE  W2.ENTEXAMYEAR='"+param[0]+"'  ");
			stb.append("            AND W2.PROCEDUREDIV='1' ");	//基礎データ.手続区分
			stb.append("            AND (W2.ENTDIV='1' OR W2.ENTDIV IS NULL) ");//基礎データ.入学区分 NO001
            stb.append("            AND W2.APPLICANTDIV='"+param[1]+"' ");
            stb.append("    GROUP BY W2.ENTCLASS ");
		} catch( Exception e ){
			log.error("preStat2 error!");
		}
		return stb.toString();

	}//preStat2()の括り



	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps1,
		PreparedStatement ps2
	) {
		try {
			ps1.close();
			ps2.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り



}//クラスの括り
