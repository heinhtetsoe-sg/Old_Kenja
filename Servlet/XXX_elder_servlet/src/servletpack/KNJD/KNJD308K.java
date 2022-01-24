/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ３０８＞  補充試験（中間・期末）報告書
 *
 *	2004/08/25 yamashiro・新規作成
 *	2004/10/01 yamashiro・DB2のSQLにおいてINT関数の不具合を修正
 *	2004/10/20 nakamoto 出欠はkin_record_datにもたすような仕様に変更。この対応でattend_dat,testscore_hdat部分を修正。
 *	2004/11/09 nakamoto 欠課している科目に○が表示されないバグを修正。
 *	2004/12/03 nakamoto 欠課状況の記号とコメントの文言を変更
 *	2004/12/06 nakamoto 公欠表示をカット。項目名を異動に変更。
 *	2005/02/05 nakamoto db2.commit追加。処理速度改善
 *	2005/02/14 nakamoto 出廷は、対象 NO002
 *  2005/06/24 nakamoto 期末試験のみ実施する講座を欠課した場合は、黒●とする。(期末テストのみ実施テストとは、中間にkk、ksがなくて、中間素点に点数がない)
 ***********************************************************************************************
 *  2005/10/20 nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO025
 *  2005/10/26 nakamoto 補点報告書と同様に、氏名の右側に異動日付の表示を追加---NO026
 **/

package servletpack.KNJD;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJD308K {

    private static final Log log = LogFactory.getLog(KNJD308K.class);
    
    String _useCurriculumcd;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
        log.fatal("$Revision: 67196 $ $Date: 2019-04-26 00:14:31 +0900 (金, 26 4 2019) $"); // CVSキーワードの取り扱いに注意
		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[10];

	//	パラメータの取得
		String classcd[] = request.getParameterValues("GRADE");   			//学年
		try {
	        param[0] = request.getParameter("YEAR");         						//年度
			param[1] = request.getParameter("GAKKI");   							//学期
			param[2] = (request.getParameter("TESTKINDCD")).substring(0,2); 		//0101:中間/0201:期末
			String idobi = request.getParameter("DATE");   							//異動対象日付 04/12/03Add
			param[4] = idobi.replace('/','-');
			_useCurriculumcd = request.getParameter("useCurriculumcd");
		} catch (Exception ex) {
			log.warn("parameter error!", ex);
		}

	//	print設定
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	//	svf設定
		svf.VrInit();						   	//クラスの初期化
		svf.VrSetSpoolFileStream(outstrm);   		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch (Exception ex ) {
			log.error("DB2 open error!");
			return;
		}

	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		Set_Head(db2,svf,param);								//見出し出力のメソッド
		//SQL作成
		try {
			ps1 = db2.prepareStatement(Pre_Stat1(param));		//生徒及び公欠・欠席者
			ps2 = db2.prepareStatement(Pre_Stat2(param));		//試験日欠課
			ps3 = db2.prepareStatement(Pre_Stat3(param));		//科目
		} catch (Exception ex) {
			log.warn("DB2 open error!", ex);
		}
		//SVF出力
		for (int ia=0 ; ia<classcd.length ; ia++) {
			if (Set_Detail_1(db2,svf,param,classcd[ia],ps1,ps2,ps3)) {
			    nonedata = true;
			}
		}

	//	該当データ無し
		if (!nonedata) {
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		Pre_Stat_f(ps1,ps2,ps3);	//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** SVF-FORM **/
	private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]) {

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		svf.VrSetForm("KNJD_KYOTU.frm", 4);				//共通フォーム
		svf.VrsOut("NENDO2",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");//年度
		if(param[2].equals("01")){								//中間
			svf.VrsOut("PRGID","KNJD308_1");
			svf.VrsOut("TITLE2","中間試験補充報告書");
			svf.VrsOut("NOTE1","〇中間のみ欠席で受験していない場合");	// 04/12/03Modify
			svf.VrsOut("NOTE2","△中間のみ公欠で受験していない場合");	// 04/12/03Add
		} else{													//期末
			svf.VrsOut("PRGID","KNJD308_2");
			svf.VrsOut("TITLE2","期末試験補充報告書");
			svf.VrsOut("NOTE1","〇期末のみ欠席で受験していない場合、但し中間でも欠課している場合は●です");// 04/12/03Modify
			svf.VrsOut("NOTE2","△期末のみ公欠で受験していない場合、但し中間でも欠課している場合は▲です");// 04/12/03Modify
		}

	//	ＳＶＦ属性変更--->改ページ
		svf.VrAttribute("GRADE","FF=1");

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));		//作成日
		} catch (Exception e ){
			log.warn("ctrl_date get error!", e);
		}
	//	学期名称の取得
		try {
			returnval = getinfo.Semester(db2,param[0],param[1]);
			svf.VrsOut("SEMESTER2"	,returnval.val1);	//学期名称
			param[3] = returnval.val2;						//学期開始日
			//param[4] = returnval.val3;						//学期終了日
		} catch (Exception e ){
			log.warn("Semester name get error!", e);
		}
		getinfo = null;
		returnval = null;

		//各学期成績出欠情報 04/10/20Modify
		if(param[2].equals("01")){						//中間
			param[5] = "SEM"+param[1]+"_INTER_REC_DI";
			param[9] = "SEM"+param[1]+"_INTER_REC";
		} else {										//期末
			param[5] = "SEM"+param[1]+"_TERM_REC_DI";
			param[6] = "SEM"+param[1]+"_INTER_REC_DI";
			param[9] = "SEM"+param[1]+"_TERM_REC";
		}

	}//Set_Head()の括り



	/**SVF-FORM**/
	private boolean Set_Detail_1(DB2UDB db2,Vrw32alp svf,String param[],String classcd
										,PreparedStatement ps1,PreparedStatement ps2,PreparedStatement ps3)
	{
		boolean nonedata = false;
		try {
			int pp = 0;
			ps1.setString(++pp,classcd);	//学年
			ps1.setString(++pp,classcd);	//学年
			ps1.setString(++pp,classcd);	//学年
			ps1.setString(++pp,classcd);	//学年
			ps1.setString(++pp,classcd);	//学年
			//ps1.setString(++pp,classcd);	//学年 NO025
			ResultSet rs = ps1.executeQuery();
			svf.VrsOut("GRADE",	classcd);							//学年（改ページ用）

			Map hm1 = new HashMap();										//学籍番号と行番号の保管
			int schno = 0;
			String schregno = "";//学籍番号 04/10/28Modify
			while (rs.next()){
				schregno = rs.getString("SCHREGNO");
				if (!rs.getString("DI_NAME").equals("KK") && !rs.getString("DI_NAME").equals("KS")) {
					schregno = "00000000";//異動生徒（留学・休学・停学・編入）なら行番号に学籍番号"00000000"をセット
				}

				hm1.put(schregno,new Integer(++schno));		//行番号に学籍番号を付ける
				Set_Detail_1_1(svf,rs,schno);								//生徒名等出力のメソッド
				if (schno == 1) {
				    param[7] = rs.getString("ATTENDNO2");			//開始生徒
				}
				param[8] = rs.getString("ATTENDNO2");						//終了生徒
				if (schno == 30) {
					if (Set_Detail_2(db2,svf,param,hm1,ps2,ps3,classcd)) nonedata = true;//科目、欠課出力のメソッド
					hm1.clear();											//行番号情報を削除
					schno = 0;
					param[7] = null;										//開始生徒
					param[8] = null;										//終了生徒
				}
			}
			rs.close();
			db2.commit();//05.02.05
			if (schno > 0) {
				if (Set_Detail_2(db2,svf,param,hm1,ps2,ps3,classcd)) {
				    nonedata = true;//科目、欠課出力のメソッド
				}
			}
		} catch (Exception ex) {
			log.warn("Set_Detail_1 read error!", ex);
		}
		return nonedata;

	}//boolean Set_Detail_1()の括り



	/** 生徒名等出力 **/
	private void Set_Detail_1_1(Vrw32alp svf,ResultSet rs,int ia) {

		try {
			svf.VrsOutn("HR_CLASS",ia ,rs.getString("HR_NAMEABBV"));			//組略称
			svf.VrsOutn("ATTENDNO",ia ,rs.getString("ATTENDNO"));				//出席番号
			svf.VrsOutn("NAME"    ,ia ,rs.getString("NAME"));					//生徒名
			//公欠カット 04/12/06Modify
			String di_name= "";
			if (!rs.getString("DI_NAME").equals("KK") && !rs.getString("DI_NAME").equals("KS")) 
				di_name= rs.getString("DI_NAME");//異動情報（留学・休学・停学・編入）
			svf.VrsOutn("ABSENT"  ,ia ,di_name);
			//svf.VrsOutn("ABSENT"  ,ia , (rs.getInt("DI_NAME")==1 )?"公":"欠");//公欠区分

			//異動日付をセット---NO026
			String mongon= "";
			if (!rs.getString("DI_NAME").equals("KK") && !rs.getString("DI_NAME").equals("KS")) {
				String di_date = rs.getString("DI_DATE");				//2004-10-05
				String wareki  = KNJ_EditDate.h_format_JP_N(di_date);	//平成16年
				if (wareki.substring(2).startsWith("元")) {
					mongon  = wareki.substring(2,3) + di_date.substring(5,7) + di_date.substring(8);	//161005
				} else {
					mongon  = wareki.substring(2,4) + di_date.substring(5,7) + di_date.substring(8);	//161005
				}
			}
			svf.VrsOutn("TRANSFER",ia		,mongon);			//異動日付
		} catch (SQLException ex) {
			log.warn("[KNJC051]Set_Detail_1_1 rs1 svf error!", ex);
		}

	}//Set_Detail_1_1()の括り


	// 04/10/26 Modify add
	/** 科目、欠課内容出力 **/
	private boolean Set_Detail_2(DB2UDB db2,Vrw32alp svf,String param[],Map hm1
									,PreparedStatement ps2,PreparedStatement ps3,String classcd)
	{
		boolean nonedata = false;
		try {
			int pp = 0;
			ps3.setString(++pp,classcd);				//学年
			ResultSet rs3 = ps3.executeQuery();			//科目表のレコードセット
			pp = 0;
			//ps2.setString(++pp,classcd);				//学年 04/10/26Modify
			ps2.setString(++pp,param[7]);				//開始生徒
			ps2.setString(++pp,param[8]);				//終了生徒
			ResultSet rs = ps2.executeQuery();			//欠課者表のレコードセット
//for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

			String subclass = "0";			//科目コードの保存
			String schno = "0";				//学籍番号の保存
			int lcount = 0;					//列出力カウント]
			int testnum = 0;				//試験回数
//			int absentnum = 0;				//欠課回数

			String di_name = "";			//公欠・欠席コードの保存（中間・期末） 04/12/03Add
			String di_name_inter = "";		//公欠・欠席コードの保存（中間） 04/12/03Add
			int hoten = -2;					//補充-->初期値は-2とする
			while (rs.next()) {
				//学籍番号のブレイク 04/11/09Modify
				if (!schno.equals(rs.getString("SCHREGNO")) || !subclass.equals(rs.getString("SUBCLASSCD"))) {
					if (!schno.equals("0"))
						Set_Detail_2_2(svf,schno,param,hm1,di_name,di_name_inter,testnum,hoten);	//欠課内容出力のメソッド
					schno = rs.getString("SCHREGNO");
					testnum = 0;
					// absentnum = 0;
					hoten = -2;		//初期値にセット
				}
				//科目コードのブレイク
				if (!subclass.equals(rs.getString("SUBCLASSCD"))) {
					if (!subclass.equals("0")) {
						svf.VrEndRecord();
						nonedata = true;
						lcount++;
					}
					subclass = rs.getString("SUBCLASSCD");
					//欠課者なしの科目列を出力
					for (; rs3.next();) {
						Set_Detail_2_1(svf,rs3,param);				//科目出力のメソッド
						if (rs3.getString("SUBCLASSCD").equals(subclass))
							break;
						else{
							svf.VrEndRecord();
							nonedata = true;
							lcount++;
						}
					}
				}
				//期末試験の場合-->中間試験の欠課と合わせてカウント
				if (param[2].equals("02") && !param[1].equals("3")) {
					di_name_inter = rs.getString(param[6]);	//公欠・欠席コードの保存 04/12/03Add
   					if (rs.getString("SEM"+param[1]+"_INTER_REC") != null) testnum++;//2005.06.24
				}
				di_name = rs.getString(param[5]);			//公欠・欠席コードの保存 04/12/03Add
				if (rs.getInt(param[9]+"_FLG") == 2) hoten = rs.getInt(param[9]);	//補充 04/12/06
			}
			rs.close();		//欠課者表のレコードセットをクローズ
			db2.commit();//05.02.05
			//最後の列を出力
			if (!schno.equals("0")) {
				Set_Detail_2_2(svf,schno,param,hm1,di_name,di_name_inter,testnum,hoten);	//欠課内容出力のメソッド
				svf.VrEndRecord();
				nonedata = true;
				lcount++;
			}
			if (nonedata) {
				//残りの科目列を出力-->欠課者なし
				for (; rs3.next();) {
					Set_Detail_2_1(svf,rs3,param);				//科目出力のメソッド
						svf.VrEndRecord();
						lcount++;
				}
				//空列の出力-->学年で改ページ
				for (; lcount % 17 > 0; lcount++) svf.VrEndRecord();
			}
			Svf_Int(svf);		//SVFフィールド初期化
			rs3.close();		//科目表のレコードセットをクローズ
			db2.commit();//05.02.05

		} catch (Exception ex) {
			log.warn("Set_Detail_2 read error!", ex);
		}

		return nonedata;

	}//boolean Set_Detail_2()の括り



	/** 科目名出力 **/
	private void Set_Detail_2_1(Vrw32alp svf,ResultSet rs,String param[]) {

		try {
			boolean boo_elect = false;
			//科目マスタの選択区分＝１の時、科目名を網掛けにする。
			if (rs.getString("ELECTDIV") != null)
				if (rs.getString("ELECTDIV").equals("1"))// 04/10/20Modify 2→1
					boo_elect = true;
			if (boo_elect) svf.VrAttribute("SUBCLASS1" 	,"Paint=(2,60,1),Bold=1"); 	//網掛け
			svf.VrsOut("SUBCLASS1"		,rs.getString("SUBCLASSABBV"));				//科目
			if (boo_elect) svf.VrAttribute("SUBCLASS1" 	,"Paint=(0,0,0),Bold=0");   //網掛けクリア
		} catch (SQLException ex) {
			log.warn("Set_Detail_2_1 svf error!", ex);
		}

	}//Set_Detail_2_1()の括り



	/** 欠課内容出力 04/12/03**/
	private void Set_Detail_2_2(
		Vrw32alp svf,
		String schno,
		String param[],
		Map hm1,
		String di_name,
		String di_name_inter,
		int testnum,
		int hoten
	) {

		try {
		//	学籍番号（生徒）に対応した行に欠課内容をセットする。（丸：欠席、三角：公欠）
			Integer int1 = (Integer) hm1.get(schno);
			if (int1 != null) {
				svf.VrAttributen("POINT1",int1.intValue(),"Hensyu=3");
				//--->中間OR期末試験の補充
				String stry = "";
				if (hoten > -2) {
					String strx = "   "+String.valueOf(hoten);
					stry = strx.substring(strx.length()-3,strx.length());
				}
				//中間試験の時は、白
				if (param[2].equals("01")) {
					if (di_name.equals("KS")) svf.VrsOutn("POINT1", int1.intValue(), "〇"+stry);
					if (di_name.equals("KK")) svf.VrsOutn("POINT1", int1.intValue(), "△"+stry);
				//３学期は、期末試験のみ実施のため、白→黒//2005.06.24
				} else if (param[1].equals("3")) {
					if (di_name.equals("KS")) svf.VrsOutn("POINT1", int1.intValue(), "●"+stry);
					if (di_name.equals("KK")) svf.VrsOutn("POINT1", int1.intValue(), "▲"+stry);
				//１・２学期の期末試験の時
				} else {
					//期末・中間試験を両方欠課は、黒
					if (di_name_inter != null) {
						if (di_name.equals("KS")) svf.VrsOutn("POINT1", int1.intValue(), "●"+stry);
						if (di_name.equals("KK")) svf.VrsOutn("POINT1", int1.intValue(), "▲"+stry);
					//期末試験のみ実施する講座を欠課した場合は、黒//2005.06.24
					} else if (testnum == 0) {
						if (di_name.equals("KS")) svf.VrsOutn("POINT1", int1.intValue(), "●"+stry);
						if (di_name.equals("KK")) svf.VrsOutn("POINT1", int1.intValue(), "▲"+stry);
					//期末試験のみ欠課は、白
					} else {
						if (di_name.equals("KS")) svf.VrsOutn("POINT1", int1.intValue(), "〇"+stry);
						if (di_name.equals("KK")) svf.VrsOutn("POINT1", int1.intValue(), "△"+stry);
					}
				}
			}
		} catch (Exception ex) {
			log.warn("Set_Detail_2_2 svf error!", ex);
		}

	}//Set_Detail_2_2()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat1(String param[]) {

	//	生徒及び公欠・欠席者データ
		StringBuffer stb = new StringBuffer();
		try {
			// 05/02/05Modify
			stb.append("WITH KIN_REC AS ( ");
			stb.append("    SELECT SCHREGNO,MIN("+param[5]+") DI_NAME,'1900-10-10' di_date ");//NO026
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND "+param[5]+" in ('KS','KK') AND ");
			stb.append("           ("+param[9]+"_FLG='0' OR "+param[9]+"_FLG='2' OR "+param[9]+"_FLG IS NULL) ");
			stb.append("    GROUP BY SCHREGNO ) ");
            //NO025Modify----------↓----------
			stb.append(",SCHNO AS ( ");
			stb.append(	    "SELECT W1.schregno ");
	        stb.append(	    "FROM   SCHREG_REGD_DAT W1,SEMESTER_MST T1 ");
	        stb.append(		"WHERE  W1.YEAR = '" + param[0] + "' AND ");
	        stb.append(	  	       "W1.SEMESTER = '" + param[1] + "' AND ");
	        stb.append(            "W1.GRADE = ? AND ");
	        stb.append(	  	       "W1.YEAR = T1.YEAR AND ");
	        stb.append(	  	       "W1.SEMESTER = T1.SEMESTER AND ");
	        stb.append(            "NOT EXISTS( SELECT 'X' FROM SCHREG_BASE_MST S1 ");
	        stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
	        stb.append(                             "((S1.GRD_DIV IN ('2','3') AND ");
	        stb.append(                               "S1.GRD_DATE < CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END) OR ");
	        stb.append(                              "(S1.ENT_DIV IN ('4','5') AND ");
	        stb.append(                               "S1.ENT_DATE > CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END))) AND ");
	        stb.append(            "NOT EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
	        stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
	        stb.append(                              "(S1.TRANSFERCD IN ('1','2') AND ");
	        stb.append(                               "CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ");
			stb.append(     " ) ");
            //NO025Modify----------↑----------

			stb.append("SELECT TBL2.HR_NAMEABBV,TBL2.GRADE,TBL2.HR_CLASS,TBL2.ATTENDNO,TBL5.NAME,TBL1.DI_NAME,TBL1.DI_DATE,");//NO026
			stb.append(       "TBL2.SCHREGNO,TBL2.GRADE||TBL2.HR_CLASS||TBL2.ATTENDNO AS ATTENDNO2 ");
						//学期内の異動情報 04/10/28Modify
			stb.append("FROM ( SELECT w3.schregno,w3.transfer_sdate di_date, ");//NO026
			stb.append("             (select name1 from name_mst ");
			stb.append("              where namecd1='A004' and namecd2=w3.transfercd) di_name ");
			stb.append("       FROM   schreg_regd_dat w1, schreg_transfer_dat w3 ,SEMESTER_MST T1 ");//NO025
			stb.append("       WHERE  w1.year='"+param[0]+"' AND w1.semester='"+param[1]+"' AND  ");
			stb.append("              w1.grade=? and w3.schregno=w1.schregno and  ");
            //NO025Modify----------↓----------
	        stb.append(	  	       "W1.YEAR = T1.YEAR AND ");
	        stb.append(	  	       "W1.SEMESTER = T1.SEMESTER AND ");
	        stb.append(            "(W3.TRANSFERCD IN ('1','2') AND ");
			stb.append("              T1.SDATE <= w3.transfer_sdate and  ");//---NO025
	        stb.append(            "CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END BETWEEN W3.TRANSFER_SDATE AND W3.TRANSFER_EDATE) ");
			stb.append("       union ");
			stb.append("       SELECT w3.schregno,w3.grd_date di_date, ");//NO026
			stb.append("             (select name1 from name_mst ");
			stb.append("              where namecd1='A003' and namecd2=w3.grd_div) di_name ");
			stb.append("       FROM   schreg_regd_dat w1, schreg_base_mst w3 ,SEMESTER_MST T1 ");//NO025
			stb.append("       WHERE  w1.year='"+param[0]+"' AND w1.semester='"+param[1]+"' AND  ");
			stb.append("              w1.grade=? and w3.schregno=w1.schregno and  ");
	        stb.append(	  	       "W1.YEAR = T1.YEAR AND ");
	        stb.append(	  	       "W1.SEMESTER = T1.SEMESTER AND ");
	        stb.append(            "(W3.GRD_DIV IN ('2','3') AND ");
			stb.append("              T1.SDATE <= w3.GRD_DATE and  ");//---NO025
	        stb.append(             "W3.GRD_DATE < CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END) ");
			stb.append("       union ");
			stb.append("       SELECT w3.schregno,w3.ENT_DATE di_date, ");//NO026
			stb.append("             (select name1 from name_mst ");
			stb.append("              where namecd1='A002' and namecd2=w3.ENT_DIV) di_name ");
			stb.append("       FROM   schreg_regd_dat w1, schreg_base_mst w3 ,SEMESTER_MST T1 ");//NO025
			stb.append("       WHERE  w1.year='"+param[0]+"' AND w1.semester='"+param[1]+"' AND  ");
			stb.append("              w1.grade=? and w3.schregno=w1.schregno and  ");
	        stb.append(	  	       "W1.YEAR = T1.YEAR AND ");
	        stb.append(	  	       "W1.SEMESTER = T1.SEMESTER AND ");
	        stb.append(            "(W3.ENT_DIV IN ('4','5') AND ");
			stb.append("              w3.ENT_DATE <= T1.EDATE and  ");//---NO025
	        stb.append(             "W3.ENT_DATE > CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END) ");
            //NO025Modify----------↑----------
						//試験日の出欠情報（異動生徒は省く） 04/10/28Modify
			stb.append("       union ");
            //NO025Modify----------↓----------
			stb.append("       SELECT w1.schregno, w1.DI_DATE, w1.DI_NAME ");//NO026
			stb.append(       "FROM   KIN_REC w1, SCHNO w2 ");
			stb.append(       "WHERE  w2.schregno=w1.schregno ");
            //NO025Modify----------↑----------
			stb.append(       " )TBL1, ");
			//stb.append(       "GROUP BY w1.schregno )TBL1, ");// 05/02/05Modify
						//学籍情報
			stb.append(     "( SELECT W1.SCHREGNO,W1.GRADE,W1.HR_CLASS,W1.ATTENDNO,W2.HR_NAMEABBV ");
			stb.append(       "FROM   SCHREG_REGD_DAT W1,SCHREG_REGD_HDAT W2 ");
			stb.append(       "WHERE  W2.YEAR='"+param[0]+"' AND W2.SEMESTER='"+param[1]+"' AND ");
			stb.append(              "W2.GRADE =? AND ");
			stb.append(              "W1.YEAR=W2.YEAR AND W1.SEMESTER=W2.SEMESTER AND ");
			stb.append(              "W1.GRADE = W2.GRADE AND W1.HR_CLASS = W2.HR_CLASS )TBL2, ");
			stb.append(     "SCHREG_BASE_MST TBL5 ");

			stb.append("WHERE TBL1.SCHREGNO=TBL2.SCHREGNO AND TBL1.SCHREGNO=TBL5.SCHREGNO ");
			stb.append("ORDER BY TBL2.GRADE,TBL2.HR_CLASS,TBL2.ATTENDNO ");
		} catch (Exception e) {
			log.warn("Pre_Stat1 error!", e);
		}
		return stb.toString();

	}//Pre_Stat1()の括り



	/** 試験日欠課者データPrepareStatement作成 **/
	String Pre_Stat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("WITH KIN_REC2 AS ( ");
			stb.append("    SELECT SCHREGNO,MIN("+param[5]+") DI_NAME ");
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND "+param[5]+" in ('KS','KK') AND ");
			stb.append("           ("+param[9]+"_FLG='0' OR "+param[9]+"_FLG='2' OR "+param[9]+"_FLG IS NULL) ");
			stb.append("    GROUP BY SCHREGNO ");
			stb.append("    ), ");
			stb.append("SCHNO AS ( ");
			stb.append("    SELECT K2.SCHREGNO ");
			stb.append("    FROM   SCHREG_REGD_DAT K1,KIN_REC2 K2 ");
			stb.append("    WHERE  K2.SCHREGNO=K1.SCHREGNO AND YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
			stb.append("           GRADE||HR_CLASS||ATTENDNO >=? AND ");
			stb.append("           GRADE||HR_CLASS||ATTENDNO <=?   ");
			stb.append("    ), ");
			stb.append("KIN_REC AS ( ");
			stb.append("    SELECT * ");
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND "+param[5]+" in ('KS','KK') AND ");
			stb.append("           ("+param[9]+"_FLG='0' OR "+param[9]+"_FLG='2' OR "+param[9]+"_FLG IS NULL) ");
			stb.append("    ) ");
			stb.append("SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(" CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
			stb.append("    w1.subclasscd as subclasscd, w1.schregno, w1.chaircd,  ");
			stb.append("    sem1_inter_rec, sem1_term_rec, sem1_inter_rec_di, sem1_term_rec_di, ");
			stb.append("    sem2_inter_rec, sem2_term_rec, sem2_inter_rec_di, sem2_term_rec_di, ");
			stb.append("    sem3_term_rec, sem3_term_rec_di, ");
			stb.append("    sem1_inter_rec_flg, sem1_term_rec_flg, ");
			stb.append("    sem2_inter_rec_flg, sem2_term_rec_flg, ");
			stb.append("    sem3_term_rec_flg ");
			stb.append("FROM   KIN_REC W1, SCHNO W2 ");
			stb.append("WHERE  W2.SCHREGNO=W1.SCHREGNO ");
            stb.append("ORDER BY ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(" CLASSCD,  SCHOOL_KIND,  CURRICULUM_CD, ");
            }
			stb.append(" W1.SUBCLASSCD,W1.SCHREGNO ");
		} catch (Exception e) {
			log.warn("Pre_Stat2 error!", e);
		}
		return stb.toString();

	}//Pre_Stat2()の括り


	/** 試験科目PrepareStatement作成 **/
	String Pre_Stat3(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("WITH KIN_REC2 AS ( ");
			stb.append("    SELECT SCHREGNO,MIN("+param[5]+") DI_NAME ");
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND "+param[5]+" in ('KS','KK') AND ");
			stb.append("           ("+param[9]+"_FLG='0' OR "+param[9]+"_FLG='2' OR "+param[9]+"_FLG IS NULL) ");
			stb.append("    GROUP BY SCHREGNO ");
			stb.append("    ), ");
			stb.append("SCHNO AS ( ");
			stb.append("    SELECT K2.SCHREGNO ");
			stb.append("    FROM   SCHREG_REGD_DAT K1,KIN_REC2 K2 ");
			stb.append("    WHERE  K2.SCHREGNO=K1.SCHREGNO AND YEAR='"+param[0]+"' AND ");
			stb.append("           SEMESTER='"+param[1]+"' AND GRADE=? ), ");
			stb.append("KIN_REC AS ( ");
            stb.append("    SELECT SUBCLASSCD,SCHREGNO ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(", CLASSCD , SCHOOL_KIND , CURRICULUM_CD ");
            }
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND "+param[5]+" in ('KS','KK') AND ");
			stb.append("           ("+param[9]+"_FLG='0' OR "+param[9]+"_FLG='2' OR "+param[9]+"_FLG IS NULL) ");
			stb.append("    ) ");

            stb.append("SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
			stb.append(" T1.SUBCLASSCD AS SUBCLASSCD,T2.SUBCLASSABBV,T2.ELECTDIV ");
			stb.append("FROM    ");
			stb.append("    (SELECT DISTINCT W1.SUBCLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(", CLASSCD , SCHOOL_KIND , CURRICULUM_CD ");
            }
			stb.append("     FROM   KIN_REC W1, SCHNO W2 ");
			stb.append("     WHERE  W2.SCHREGNO=W1.SCHREGNO ");
			stb.append("    ) T1, SUBCLASS_MST T2 ");
			stb.append("WHERE  T2.SUBCLASSCD=T1.SUBCLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
			stb.append("ORDER BY ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(" T1.CLASSCD,  T1.SCHOOL_KIND,  T1.CURRICULUM_CD, ");
            }
			stb.append("  T1.SUBCLASSCD ");
		} catch (Exception e) {
			log.warn("Pre_Stat3 error!", e);
		}
		return stb.toString();

	}//Pre_Stat3()の括り



	/**PrepareStatement close**/
	private void Pre_Stat_f(PreparedStatement ps1,PreparedStatement ps2,PreparedStatement ps3)
	{
		try {
			ps1.close();
			ps2.close();
			ps3.close();
		} catch (Exception e) {
			log.warn("Pre_Stat_f error!", e);
		}
	}//Pre_Stat_f()の括り



	/**SVF-FORM-FIELD-INZ**/
	private void Svf_Int(Vrw32alp svf) {

		for (int j = 1; j < 31; j++) {
			svf.VrsOutn("HR_CLASS"		,j 	, "" );
			svf.VrsOutn("ATTENDNO"		,j 	, "" );
			svf.VrsOutn("NAME"			,j 	, "" );
			svf.VrsOutn("ABSENT"			,j 	, "" );
			svf.VrsOutn("TRANSFER"		,j 	, "" );//---NO026
		}

	}//Svf_Int()の括り



}//クラスの括り
