package servletpack.KNJH;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [指導情報管理]
 *
 *					＜ＫＮＪＨ１００＞  賞罰一覧
 *
 * 2003/11/01 nakamoto ----- 指示画面にて資格カット！（帳票は修正なし）
 * 2003/11/12 nakamoto ----- 和暦変換に対応
 * 2004/02/11 nakamoto ----- 名称マスタのコード変更('H011'→'H303','H012'→'H304')
 * 2004/02/13 nakamoto ----- 賞罰内容・備考の表示方法を修正
 * 2006/02/01 m-yama   NO001 SCHREG_AWARD_DAT→SCHREG_DETAILHIST_DATに変更
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJH100 {
	Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB    db2;					// Databaseクラスを継承したクラス
	String dbname = new String();
	boolean nonedata; 			// 該当データなしフラグ
	int ret;		// ＳＶＦ応答値


	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

					/*
						0	YEAR 				2002 
						1	GAKKI 				1 
						2	CLASS_SELECTED 		101 配列
						5	HYOSHO 				on 
						6	BATSU 				on 
						7	SIKAKU 				on 

						3	HS_KUBUN_FROM 01 
						4	HS_KUBUN_TO 01 
						9	BS_KUBUN_FROM 01 
						10	BS_KUBUN_TO 01 
						11	ST_KUBUN_FROM 01 
						12	ST_KUBUN_TO 01 

							DBNAME 				kenjadb 
						[8]作成日
					*/

	// パラメータの取得
	    String param[] = new String[13];
		try {
			dbname   = request.getParameter("DBNAME");      	// データベース名
	        param[0] = request.getParameter("YEAR");         	// 年度
			param[1] = request.getParameter("GAKKI");   		// 学期

			//学年・組の編集
			String ghclass[] = request.getParameterValues("CLASS_SELECTED");   	// クラス
			int i = 0;
			param[2] = "(";
			while(i < ghclass.length){
				if(ghclass[i] == null ) break;
				if(i > 0) param[2] = param[2] + ",";
				param[2] = param[2] + "'" + ghclass[i] + "'";
				i++;
			}
			param[2] = param[2] + ")";


			//表彰
			param[5] = "off";
			if(request.getParameter("HYOSHO") != null){
				param[5] = request.getParameter("HYOSHO");
				param[3] = request.getParameter("HS_KUBUN_FROM");  	// コードfrom
				param[4] = request.getParameter("HS_KUBUN_TO");  	// コードto
			}
			//罰則
			param[6] = "off";
			if(request.getParameter("BATSU") != null){
				param[6] = request.getParameter("BATSU");
				param[9] = request.getParameter("BS_KUBUN_FROM");  	// コードfrom
				param[10] = request.getParameter("BS_KUBUN_TO");  	// コードto
			}

            param[7] = request.getParameter("OUTPUT"); //1:クラス全員 2:対象者のみ

		} catch( Exception ex ) {
			System.out.println("[KNJH100]parameter error!");
			System.out.println(ex);
		}


	// print設定
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	// svf設定
		ret = svf.VrInit();							//クラスの初期化
		ret = svf.VrSetSpoolFileStream(outstrm);   	//PDFファイル名の設定

	// ＤＢ接続
		db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
		try {
			db2.open();
		} catch( Exception ex ) {
			System.out.println("[KNJH100]DB2 open error!");
		}


		/*作成日取得*/
		try {
			KNJ_Control date = new KNJ_Control();								//取得クラスのインスタンス作成
			KNJ_Control.ReturnVal returnval = date.Control(db2);
			param[8] = returnval.val3;											//作成日
		} catch( Exception e ){
			System.out.println("[KNJH100]DB2 CONTROL_MST query error!");
			System.out.println( e );
		}

		for(int ia=0 ; ia<param.length ; ia++) System.out.println("[KNJH100]param[" + ia + "]=" + param[ia]);


	/*-----------------------------------------------------------------------------
	    ＳＶＦ作成処理       
	  -----------------------------------------------------------------------------*/
		nonedata = false; 		// 該当データなしフラグ(MES001.frm出力用)

		set_detail(param);

	// ＳＶＦフォーム出力
		/*該当データ無し*/
		if(nonedata == false){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndRecord();
			ret = svf.VrEndPage();
		}

	// 終了処理
		db2.close();		// DBを閉じる
		ret = svf.VrQuit();
		outstrm.close();	// ストリームを閉じる 

	}	//doGetの括り


	/*----------------------------*
	 * 賞罰明細出力               *
	 *----------------------------*/
	public void set_detail(String param[])
	                 throws ServletException, IOException
	{
		try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.DETAIL_SDATE, ");
            stb.append("     T2.GRADE, ");
            stb.append("     T2.HR_CLASS, ");
            stb.append("     T2.ATTENDNO, ");
            stb.append("     T4.HR_NAMEABBV, ");
            stb.append("     T3.NAME_SHOW, ");
            if ("on".equals(param[5])) {
                stb.append("     CASE WHEN T1.DETAIL_DIV = '1' AND T1.DETAILCD BETWEEN '" + param[3] + "' AND '" + param[4] + "' ");
                stb.append("          THEN (SELECT ST1.NAME1 FROM NAME_MST ST1 WHERE ST1.NAMECD1 = 'H303' AND ST1.NAMECD2= T1.DETAILCD) ");
                stb.append("          ELSE ' ' END AS HYOSHO, ");
            } else {
                stb.append("     ' ' AS HYOSHO, ");
            }
            if ("on".equals(param[6])) {
                stb.append("     CASE WHEN T1.DETAIL_DIV = '2' AND T1.DETAILCD BETWEEN '" + param[9] + "' AND '" + param[10] + "' ");
                stb.append("          THEN (SELECT ST1.NAME1 FROM NAME_MST ST1 WHERE ST1.NAMECD1 = 'H304' AND ST1.NAMECD2= T1.DETAILCD) ");
                stb.append("          ELSE ' ' END AS BATSU, ");
            } else {
                stb.append("     ' ' AS BATSU, ");
            }
            stb.append("     VALUE(T1.CONTENT,' ') AS CONTENT, ");
            stb.append("     VALUE(T1.REMARK,' ') AS REMARK ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T2 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T4 ");
            stb.append("         ON  T4.YEAR     = T2.YEAR ");
            stb.append("         AND T4.SEMESTER = T2.SEMESTER ");
            stb.append("         AND T4.GRADE    = T2.GRADE ");
            stb.append("         AND T4.HR_CLASS = T2.HR_CLASS ");
            if ("1".equals(param[7])) {
                stb.append("     LEFT JOIN SCHREG_DETAILHIST_DAT T1 ");
            } else {
                stb.append("     INNER JOIN SCHREG_DETAILHIST_DAT T1 ");
            }
            stb.append("         ON  T1.YEAR     = T2.YEAR ");
            stb.append("         AND T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("         AND ( ");
            if ("on".equals(param[5])) {
                stb.append("         (T1.DETAIL_DIV = '1' AND T1.DETAILCD BETWEEN '" + param[3] + "' AND '" + param[4] + "') ");
            }
            if ("on".equals(param[5]) && "on".equals(param[6])) {
                stb.append("         OR ");
            }
            if ("on".equals(param[6])) {
                stb.append("         (T1.DETAIL_DIV = '2' AND T1.DETAILCD BETWEEN '" + param[9] + "' AND '" + param[10] + "') ");
            }
            stb.append("         ) ");
            stb.append(" WHERE ");
            stb.append("     T2.GRADE || T2.HR_CLASS  IN " + param[2] + " ");
            stb.append("     AND T2.YEAR      = '" + param[0] + "' ");
            stb.append("     AND T2.SEMESTER  = '" + param[1] + "' ");
            if ("1".equals(param[7])) {
                stb.append(" ORDER BY ");
                stb.append("     T2.GRADE, ");
                stb.append("     T2.HR_CLASS, ");
                stb.append("     T2.ATTENDNO, ");
                stb.append("     T1.DETAIL_SDATE ");
            } else {
                stb.append(" ORDER BY ");
                stb.append("     T1.DETAIL_SDATE, ");
                stb.append("     T2.GRADE, ");
                stb.append("     T2.HR_CLASS, ");
                stb.append("     T2.ATTENDNO ");
            }
            String sql = stb.toString();

            System.out.println("[KNJH100]set_detail sql="+sql);
            db2.query(sql);
			java.sql.ResultSet rs = db2.getResultSet();
			System.out.println("[KNJH100]set_detail sql ok!");

  	  	   /** 照会結果の取得とsvf_formへ出力 **/
            if ("1".equals(param[7])) {
                ret = svf.VrSetForm("KNJH100_2.frm", 4);
            } else {
                ret = svf.VrSetForm("KNJH100.frm", 4);
            }
            ret = svf.VrsOut("nendo"    , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
            ret = svf.VrsOut("TODAY"    , KNJ_EditDate.h_format_JP(param[8]));

            String strone = new String();					//文字列
			String strx = new String();						//SVF出力用(備考)
			String stry = new String();						//SVF出力用(賞罰内容)
			String strz = new String();						//１文字
            String hrName = "";

			while( rs.next() ){
                if ("1".equals(param[7]) && !hrName.equals(rs.getString("HR_NAMEABBV"))) {
                    ret = svf.VrSetForm("KNJH100_2.frm", 4);
                    ret = svf.VrsOut("nendo"    , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
                    ret = svf.VrsOut("TODAY"    , KNJ_EditDate.h_format_JP(param[8]));
                }

				ret = svf.VrsOut("FIELD1"     , KNJ_EditDate.h_format_JP(rs.getString("DETAIL_SDATE")));	//登録日
                ret = svf.VrsOut("KUMI"       , rs.getString("HR_NAMEABBV") + "-" + String.valueOf(rs.getInt("ATTENDNO")));   //学年-組-出席番号
//				ret = svf.VrsOut("HR_NAME"    , rs.getString("HR_NAMEABBV"));	//学年-組
//				ret = svf.VrsOut("ATTENDNO"   , String.valueOf(rs.getInt("ATTENDNO")));	//出席番号
				ret = svf.VrsOut("NAME"   	  , rs.getString("NAME_SHOW"));	//氏名
				ret = svf.VrsOut("data1"   	  , rs.getString("HYOSHO"));	//表彰
				ret = svf.VrsOut("data3"   	  , rs.getString("BATSU"));	//罰則
				//備考
				strone = rs.getString("REMARK");
				for( int ic=0 ; ic<strone.length() ; ic++ ){
					strz = strone.substring(ic,ic+1);					//1文字
					if( !strz.equals("\r") && !strz.equals("\n") ){		//改行マークは出力しない
						strx = strx + strz;
					}
				}
				ret = svf.VrsOut("data4"   	  , strx);	//備考
				strone = strz = "";		//初期化
				//賞罰内容
				strone = rs.getString("CONTENT");
				for( int ic=0 ; ic<strone.length() ; ic++ ){
					strz = strone.substring(ic,ic+1);					//1文字
					if( !strz.equals("\r") && !strz.equals("\n") ){		//改行マークは出力しない
						stry = stry + strz;
					}
				}
				ret = svf.VrsOut("CONTENTS1"  , stry);	//賞罰内容

				ret = svf.VrEndRecord();
				strone = strx = stry = strz = "";		//初期化
				nonedata = true; //該当データなしフラグ
                hrName = rs.getString("HR_NAMEABBV");//年組保持・改ページ用
			}
			db2.commit();
			System.out.println("[KNJH100]set_detail read ok!");
		} catch( Exception ex ){
			System.out.println("[KNJH100]set_detail read error!");
			System.out.println( ex );
		}

	}	//set_detailの括り


}	//クラスの括り

