/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ３２０＞  欠点者一覧表（進級・卒業用）
 *
 *	2004/10/07 nakamoto 新規作成 11/04完了
 *					    フォームは１ページ当り３０行(生徒)１７列(科目)とする
 *
 *質問事項：進級の要件とされる科目とは？
 *
 *	2004/12/17 nakamoto 欠点科目は、類型の評定が１の生徒に修正
 *	2005/01/19 nakamoto 選択履修科目は、最後の列に表示。---NO001
 *	                    科目数の表示はXXX（YYY）で表示。XXX:欠点科目すべての数、YYY:必須科目数---NO001
 *	                    科目数の条件は必須科目数をみる。---NO002
 *	2005/01/21 nakamoto NO001にて、YYYは選択科目数に変更。選択科目数がなければ（YYY）は表示しない。NO003
 *	2005/03/07 nakamoto 異動情報の対象者は、対象外---NO004
 *	2005/03/09 nakamoto 欠点のカウントを「国語総合」で行うべきところを
 *	                    「現文」「古典」それぞれでカウントしていたのを修正。---NO005
 *	2005/03/10 nakamoto 総合的な学習(900100)は、対象外---NO006
 ***********************************************************************************************
 *  2005/10/20 nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO025
 **/

package servletpack.KNJD;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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


public class KNJD320K {

	/* ロギング */
	private static final Log log = LogFactory.getLog(KNJD320K.class);

	private String _useCurriculumcd = null;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[10];

	//	パラメータの取得
		String classcd[] = request.getParameterValues("GRADE");   			//学年
		try {
	        param[0] = request.getParameter("YEAR");         						//年度
			param[1] = request.getParameter("GAKKI");   							//学期
			param[2] = request.getParameter("SUBCLASS");   							//科目数
			param[3] = request.getParameter("OUTPUT");   							//帳票選択（1:以上,2:未満）
			param[8] = param[1];   													//学期の保管 04/12/17Add
			String idobi = request.getParameter("DATE");   					//異動対象日付---NO004
			param[7] = idobi.replace('/','-');
			_useCurriculumcd = request.getParameter("useCurriculumcd");
		} catch( Exception ex ) {
			log.warn("parameter error!");
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
		} catch( Exception ex ) {
			log.error("DB2 open error!", ex);
			return;
		}

	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		PreparedStatement ps4 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		Set_Head(db2,svf,param);								//見出し出力のメソッド
		if (log.isDebugEnabled())
			for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(Pre_Stat1(param));		//生徒（欠点者）
			ps2 = db2.prepareStatement(Pre_Stat2(param));		//科目名
			ps3 = db2.prepareStatement(Pre_Stat3(param));		//成績
			ps4 = db2.prepareStatement(Pre_Stat4(param));		//科目数
		} catch( Exception ex ) {
			log.warn("db2.prepareStatement error!");
		}
		//SVF出力
		for (int ia=0; ia<classcd.length; ia++) {
			SetTitle(svf,param,classcd[ia]);					//見出し出力タイトルのメソッド
			if( Set_Detail_1(db2,svf,param,classcd[ia],ps1,ps2,ps3,ps4) )nonedata = true;
		}

	//	該当データ無し
		if( !nonedata ){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		Pre_Stat_f(ps1,ps2,ps3,ps4);//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる

    }//doGetの括り



	/** 見出し項目（年度・作成日・プログラムＩＤ） **/
	private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		svf.VrSetForm("KNJD_KYOTU2.frm", 4);
		svf.VrsOut("NENDO1",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");	//年度
		svf.VrsOut("PRGID","KNJD320");//プログラムＩＤ

	//	ＳＶＦ属性変更--->改ページ
		svf.VrAttribute("GRADE","FF=1");

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));		//作成日
			if (param[8].equals("9")) param[1] = returnval.val2;	//学年末の場合、今学期をセット 04/12/17Add
		} catch( Exception e ){
			log.warn("ctrl_date get error!");
		}
	//	学期の取得
		try {
			returnval = getinfo.Semester(db2,param[0],param[8]);	// 04/12/17Modify param[1] ---> param[8]
			param[6] = returnval.val2;						//学期開始日 04/11/04Add
			//param[7] = returnval.val3;						//学期終了日 04/11/04Add---NO004
		} catch( Exception e ){
			System.out.println("[KNJD302K]Semester name get error!");
			System.out.println( e );
		}
		getinfo = null;
		returnval = null;

	//	各学期の成績項目名の取得	 04/12/17Add
		if (param[8].equals("1")) param[9] = "SEM1_REC";
		if (param[8].equals("2")) param[9] = "SEM2_REC";
		if (param[8].equals("3")) param[9] = "SEM3_TERM_REC";
		if (param[8].equals("9")) param[9] = "GRADE_RECORD";

	}//Set_Head()の括り



	/** 見出し項目（タイトル） **/
	private void SetTitle(Vrw32alp svf,String param[],String classcd) {

		try {
			String output = "";
			if (param[3].equals("1")) output = "科目以上の者";
			if (param[3].equals("2")) output = "科目未満の者";
			String youken = "";
			if (classcd.equals("03")) {
				youken = "卒業";
			} else {
				youken = "進級";
			}
			svf.VrsOut("TITLE1"	,youken + "の要件とされる科目で欠点科目が" + param[2] + output);//タイトル
			svf.VrsOut("SEMESTER1",	String.valueOf(Integer.parseInt(classcd)) + "学年");//学年
		} catch( Exception e ){
			log.warn("SetTitle error!");
		}

	}//SetTitle()の括り



	/** 該当学年の欠点者一覧を印刷する **/
	private boolean Set_Detail_1(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		String classcd,
		PreparedStatement ps1,
		PreparedStatement ps2,
		PreparedStatement ps3,
		PreparedStatement ps4
	) {
		boolean nonedata = false;

		try {
			int pp = 0;
			ps1.setString(++pp,classcd);	//学年
//log.debug("ps1 start!");
			ResultSet rs = ps1.executeQuery();
//log.debug("ps1 end!");
			svf.VrsOut("GRADE",	classcd);							//学年（改ページ用）

			Map hm1 = new HashMap();										//学籍番号と行番号の保管
			int schno = 0;
			while( rs.next() ){
				hm1.put(rs.getString("SCHREGNO"),new Integer(++schno));		//行番号に学籍番号を付ける
				Set_Detail_1_1(svf,rs,schno);								//生徒名等出力のメソッド
				if( schno==1 )param[4] = rs.getString("ATTENDNO2");			//開始生徒
				param[5] = rs.getString("ATTENDNO2");						//終了生徒
				if (schno == 30) {
					Set_Detail_2(db2,svf,param,hm1,ps2,ps3,ps4,classcd);		//科目出力のメソッド
					Svf_Int(svf);											//SVFフィールド初期化
					hm1.clear();											//行番号情報を削除
					schno = 0;
					param[4] = null;										//開始生徒
					param[5] = null;										//終了生徒
				}
				nonedata = true;
			}
			rs.close();
			db2.commit();
			if (schno > 0) {
				Set_Detail_2(db2,svf,param,hm1,ps2,ps3,ps4,classcd);		//科目出力のメソッド
				Svf_Int(svf);												//SVFフィールド初期化
			}
		} catch( Exception ex ) {
			log.warn("Set_Detail_1 error!", ex);
		}
		return nonedata;

	}//boolean Set_Detail_1()の括り



	/** 生徒名等出力 **/
	private void Set_Detail_1_1(Vrw32alp svf,ResultSet rs,int ia){

		try {
			svf.VrsOutn("HR_CLASS",ia ,rs.getString("HR_NAMEABBV"));			//組略称
			svf.VrsOutn("ATTENDNO",ia ,rs.getString("ATTENDNO"));				//出席番号
			svf.VrsOutn("NAME"    ,ia ,rs.getString("NAME"));					//生徒名
		} catch( Exception ex ){
			log.warn("Set_Detail_1_1 error!", ex);
		}

	}//Set_Detail_1_1()の括り



	/** 科目出力 **/
	private void Set_Detail_2(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		Map hm1,
		PreparedStatement ps2,
		PreparedStatement ps3,
		PreparedStatement ps4,
		String classcd
	) {
		try {
			int pp = 0;
			ps2.setString(++pp,classcd);			//学年
//log.debug("ps2 start!");
			ResultSet rs = ps2.executeQuery();		//科目表のレコードセット
//log.debug("ps2 end!");
			if (log.isDebugEnabled()) {
				log.debug("param[4]="+param[4]);
				log.debug("param[5]="+param[5]);
			}

			int lcount = 0;
			int numbercount = 0;	//科目数出力フラグ
			while( rs.next() ){
				//---NO001
				if ((numbercount == 0) && (rs.getInt("ELECTDIV") == 1)) {
					Set_Detail_4(db2,svf,param,hm1,ps4,classcd);		//欠点科目数出力のメソッド
					svf.VrEndRecord();
					lcount++;
					numbercount++;
				}
				Set_Detail_2_1(svf,rs);														//科目名等出力のメソッド
				Set_Detail_3(db2,svf,param,hm1,ps3,classcd,rs.getString("SUBCLASSCD"));		//得点出力のメソッド
				svf.VrEndRecord();
				lcount++;
			}
			rs.close();
			db2.commit();
			//空列の出力-->学年で改ページ
			if (lcount > 0) {
				//選択科目数データがない場合
				if (numbercount == 0) {
					Set_Detail_4(db2,svf,param,hm1,ps4,classcd);		//欠点科目数出力のメソッド
					svf.VrEndRecord();
					lcount++;
				}
				/*---NO001
				Set_Detail_4(db2,svf,param,hm1,ps4,classcd);		//欠点科目数出力のメソッド
				svf.VrEndRecord();
				lcount++;
				*/
				for( ; lcount%17>0 ; lcount++ ) {
				    svf.VrEndRecord();
				}
			}
		} catch( Exception ex ) {
			log.warn("Set_Detail_2 error!", ex);
		}

	}//Set_Detail_2()の括り



	/** 科目名出力 **/
	private void Set_Detail_2_1(Vrw32alp svf,ResultSet rs){

		try {
			int electdiv = rs.getInt("ELECTDIV");					//選択区分
			//科目マスタの選択区分＝１の時、科目名を網掛けにする。
			if (electdiv > 0) svf.VrAttribute("SUBCLASS1" 	,"Paint=(2,60,1),Bold=1"); 		//網掛け
							  svf.VrAttribute("SUBCLASS1"		,"Hensyu=3");//中央表示
							  svf.VrsOut("SUBCLASS1"			,rs.getString("SUBCLASSABBV"));	//科目名
			if (electdiv > 0) svf.VrAttribute("SUBCLASS1" 	,"Paint=(0,0,0),Bold=0");   	//網掛けクリア
		} catch( Exception ex ){
			log.warn("Set_Detail_2_1 error!", ex);
		}

	}//Set_Detail_2_1()の括り



	/** 得点出力 **/
	private void Set_Detail_3(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		Map hm1,
		PreparedStatement ps3,
		String classcd,
		String subclasscd
	) {
		try {
			int pp = 0;
			ps3.setString(++pp,classcd);			//学年
			ps3.setString(++pp,param[4]);			//開始生徒
			ps3.setString(++pp,param[5]);			//終了生徒
			ps3.setString(++pp,subclasscd);			//科目コード
			ResultSet rs = ps3.executeQuery();		//科目表のレコードセット

			while( rs.next() ){
				//学籍番号（生徒）に対応した行にデータをセットする。
				Integer int1 = (Integer)hm1.get(rs.getString("SCHREGNO"));
				if (int1 == null) continue;
				String tokuten = rs.getString("SCORE") + "(" + rs.getString("PATTERN") + ")";	// 04/12/17Modify
				svf.VrAttributen("POINT1",int1.intValue(),"Hensyu=1");//右寄せ表示
				svf.VrsOutn("POINT1", int1.intValue(), tokuten);//得点
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("Set_Detail_3 error!", ex);
		}

	}//Set_Detail_3()の括り



	/** 科目数出力 **/
	private void Set_Detail_4(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		Map hm1,
		PreparedStatement ps4,
		String classcd
	) {
		try {
			int pp = 0;
			ps4.setString(++pp,classcd);			//学年
			ps4.setString(++pp,param[4]);			//開始生徒
			ps4.setString(++pp,param[5]);			//終了生徒
//log.debug("ps4 start!");
			ResultSet rs = ps4.executeQuery();		//科目数のレコードセット
//log.debug("ps4 end!");

		    svf.VrsOut("SUBCLASS1"			,"科目数");	//科目数

			int sub_cnt = 0;	//全科目数-----NO003
			int sub_cnt1 = 0;	//必修科目数---NO003
			int sub_cnt2 = 0;	//選択科目数---NO003
			while( rs.next() ){
				//学籍番号（生徒）に対応した行にデータをセットする。
				Integer int1 = (Integer)hm1.get(rs.getString("SCHREGNO"));
				if (int1 == null) continue;

				svf.VrAttributen("POINT1",int1.intValue(),"Hensyu=3");//中央表示
				//YYYは選択科目数に変更。選択科目数がなければ（YYY）は表示しない。NO003
				sub_cnt 	= rs.getInt("SUB_CNT");
				sub_cnt1 	= rs.getInt("SUB_CNT1");
				sub_cnt2 	= sub_cnt - sub_cnt1;
				if (sub_cnt2 > 0) {
					svf.VrsOutn("POINT1", int1.intValue(), String.valueOf(sub_cnt)+"("+String.valueOf(sub_cnt2)+")");
				} else {
					svf.VrsOutn("POINT1", int1.intValue(), String.valueOf(sub_cnt));
				}
				//svf.VrsOutn("POINT1", int1.intValue(), rs.getString("SUB_CNT")+"("+rs.getString("SUB_CNT1")+")");//科目数 NO001
				//svf.VrsOutn("POINT1", int1.intValue(), rs.getString("SUB_CNT"));//科目数
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("Set_Detail_4 error!", ex);
		}

	}//Set_Detail_4()の括り



	/* 生徒（欠点者）データ
	 * 抽出条件：指定学年の生徒。類型評定が１の科目が指定科目以上・未満の生徒。
	 */
	private String Pre_Stat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	学年をパラメータとする
		try {
            //NO025Modify----------↓----------
			stb.append("WITH SCHNO AS(");
            stb.append(    "SELECT  W1.YEAR,W1.SEMESTER,SCHREGNO,GRADE,HR_CLASS,ATTENDNO ");
	        stb.append(	   "FROM    SCHREG_REGD_DAT W1,SEMESTER_MST T1 ");
	        stb.append(	   "WHERE   W1.YEAR = '" + param[0] + "' AND ");
	        stb.append(	  	       "W1.SEMESTER = '" + param[1] + "' AND ");
	        stb.append(            "W1.GRADE = ? AND ");
	        stb.append(	  	       "W1.YEAR = T1.YEAR AND ");
	        stb.append(	  	       "W1.SEMESTER = T1.SEMESTER AND ");
	        stb.append(            "NOT EXISTS( SELECT  'X' FROM SCHREG_BASE_MST S1 ");
	        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
	        stb.append(                              "((S1.GRD_DIV IN ('2','3') AND ");
	        stb.append(                                "S1.GRD_DATE < CASE WHEN T1.EDATE < '"+param[7]+"' THEN T1.EDATE ELSE '"+param[7]+"' END) OR ");
	        stb.append(                               "(S1.ENT_DIV IN ('4','5') AND ");
	        stb.append(                                "S1.ENT_DATE > CASE WHEN T1.EDATE < '"+param[7]+"' THEN T1.EDATE ELSE '"+param[7]+"' END)) ) AND ");
	        stb.append(            "NOT EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
	        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
	        stb.append(                               "(S1.TRANSFERCD IN ('1','2') AND ");
	        stb.append(                                "CASE WHEN T1.EDATE < '"+param[7]+"' THEN T1.EDATE ELSE '"+param[7]+"' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ");
			stb.append(    "), ");
//            /***** NO025
//            stb.append("WITH SCHNO AS( ");
//            stb.append(   "SELECT YEAR,SEMESTER,SCHREGNO,GRADE,HR_CLASS,ATTENDNO ");
//            stb.append(   "FROM   SCHREG_REGD_DAT ");
//            stb.append(   "WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND GRADE =? ), ");
//						//異動情報（転学・退学・留学・休学・停学・編入）04/11/04---NO004
//			stb.append("IDOU1 AS ( ");
//			stb.append("    SELECT SCHREGNO,GRD_DIV AS IDOU_CD,GRD_DATE AS IDOU_DATE ");
//			stb.append("    FROM   SCHREG_BASE_MST ");
//			stb.append("    WHERE  GRD_DIV IN ('2','3') AND GRD_DATE < '"+param[7]+"' ), ");//---NO011
//			stb.append("IDOU2 AS ( ");
//			stb.append("    SELECT SCHREGNO,TRANSFERCD AS IDOU_CD,TRANSFER_SDATE AS IDOU_DATE ");
//			stb.append("    FROM   SCHREG_TRANSFER_DAT ");
//			stb.append("    WHERE  ((TRANSFERCD IN ('1','2') AND  ");
//			stb.append("            '"+param[7]+"' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) OR ");
//			stb.append("            (TRANSFERCD IN ('4') AND '"+param[7]+"' < TRANSFER_SDATE)) AND ");
//			stb.append("           SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) ), ");
//            *****/
            //NO025Modify----------↑----------

		//	欠点科目 04/12/17Modify------------------
			stb.append("TYPE_GROUP AS ( ");
			stb.append("    SELECT W2.GRADE, W2.HR_CLASS, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||  ");
            }
			stb.append("           SUBCLASSCD AS SUBCLASSCD, "+param[9]+"_TYPE_ASSES_CD  ");
			stb.append("    FROM   TYPE_GROUP_MST W1, TYPE_GROUP_HR_DAT W2 ");
			stb.append("    WHERE  W1.YEAR='"+param[0]+"' AND W1.YEAR=W2.YEAR AND ");
			stb.append("           W1.TYPE_GROUP_CD=W2.TYPE_GROUP_CD ),  ");

			stb.append("TYPE_ASSES AS ( ");
			stb.append("    SELECT TYPE_ASSES_CD,TYPE_ASSES_HIGH  ");
			stb.append("    FROM   TYPE_ASSES_MST ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND TYPE_ASSES_LEVEL='1' ),  ");

			stb.append("SUB_REP AS(  ");//---NO005
			stb.append("    SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' ||  ");
            }
			stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD ");
			stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT  ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND REPLACECD='1' ), ");
			stb.append("KIN_REC AS ( ");
			stb.append("    SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||  ");
            }
			stb.append("           SUBCLASSCD AS SUBCLASSCD,SCHREGNO,"+param[9]+"   ");
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND "+param[9]+" IS NOT NULL  ");
			stb.append("           AND SUBCLASSCD NOT IN ('900100') ");//---NO006
            stb.append(        " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
			stb.append("           AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||  ");
            }
			stb.append("              SUBCLASSCD NOT IN (SELECT SUBCLASSCD FROM SUB_REP) ) ");//---NO005
		//	欠点科目 04/12/17Modify------------------

						//メイン
			stb.append("SELECT t2.schregno, t2.grade, t2.hr_class, t2.attendno, t2.hr_nameabbv, t2.name, ");
			stb.append("    t2.grade || t2.hr_class || t2.attendno attendno2 ");
			stb.append("FROM ");
						//欠点者情報 04/12/17Modify
			stb.append("   ( ");
			stb.append(		"SELECT COUNT(K1.SUBCLASSCD) SUB_CNT, K1.SCHREGNO ");
			stb.append(		"       ,SUM(CASE WHEN VALUE(K5.ELECTDIV,'0') = '0' THEN 1 ELSE 0 END) SUB_CNT1 ");//---NO002
			stb.append(		"FROM   KIN_REC K1, SCHNO K2, TYPE_GROUP K3, TYPE_ASSES K4 ");
			stb.append(		"       ,SUBCLASS_MST K5 ");//---NO002
			stb.append(		"WHERE  K1.SCHREGNO=K2.SCHREGNO AND K1.SUBCLASSCD=K3.SUBCLASSCD AND ");
			stb.append(		"       K2.GRADE=K3.GRADE AND K2.HR_CLASS=K3.HR_CLASS AND ");
			stb.append(		"       K1.SCHREGNO=K2.SCHREGNO AND K3."+param[9]+"_TYPE_ASSES_CD=K4.TYPE_ASSES_CD AND ");
			stb.append(		"       K1."+param[9]+" <= K4.TYPE_ASSES_HIGH ");
			stb.append(		"       AND ");//---NO002
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          K5.CLASSCD || '-' || K5.SCHOOL_KIND || '-' || K5.CURRICULUM_CD || '-' ||  ");
            }
            stb.append(     "           K5.SUBCLASSCD = K1.SUBCLASSCD ");//---NO002
			stb.append(		"GROUP BY K1.SCHREGNO ");
			stb.append(    ") t1, ");
//			/* 04/12/17Modify
//			stb.append("    (SELECT schregno, count(*) sub_cnt ");
//			stb.append("     FROM   kin_record_dat ");
//			stb.append("     WHERE  year='"+param[0]+"' AND ");
//			stb.append("          ((judge_pattern='A' AND a_pattern_assess='1') or ");
//			stb.append("           (judge_pattern='B' AND b_pattern_assess='1') or ");
//			stb.append("           (judge_pattern='C' AND c_pattern_assess='1')) ");
//			stb.append("     GROUP BY schregno) t1, ");
//			*/
						//学籍情報
			stb.append("    (SELECT w2.schregno, w2.grade, w2.hr_class, w2.attendno, w1.hr_nameabbv, w3.name ");
			stb.append("     FROM   schreg_regd_hdat w1, SCHNO w2, schreg_base_mst w3 ");
			stb.append("     WHERE  w2.year=w1.year AND w2.semester=w1.semester AND w2.grade=w1.grade AND ");
			stb.append("            w2.hr_class=w1.hr_class AND w3.schregno=w2.schregno) t2 ");
			stb.append("WHERE t1.schregno=t2.schregno  ");
						//指定科目数
			if (param[3].equals("1")) stb.append("    AND "+param[2]+" <= t1.sub_cnt1 ");//以上---NO002 sub_cnt→sub_cnt1
			if (param[3].equals("2")) stb.append("    AND t1.sub_cnt1 < "+param[2]+"  ");//未満---NO002 sub_cnt→sub_cnt1
						//異動情報の対象者は、対象外---NO004
//            /***** NO025
//			stb.append("      AND t1.SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) ");
//			stb.append("      AND t1.SCHREGNO NOT IN (SELECT DISTINCT SCHREGNO FROM IDOU2) ");
//            *****/
			stb.append("ORDER BY t2.grade, t2.hr_class, t2.attendno ");
		} catch( Exception e ){
			log.warn("Pre_Stat1 error!");
		}
		return stb.toString();

	}//Pre_Stat1()の括り


	/* 欠点科目
	 * 抽出条件 指定学年の生徒で、類型評定が１の欠点科目。
	 * electdiv 選択科目フラグ（１：選択）。
	 */
	String Pre_Stat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//学年をパラメータとする
		try {
						//在籍
            stb.append("WITH SCHNO AS( ");
            stb.append(   "SELECT SCHREGNO, GRADE, HR_CLASS ");// 04/12/17Modify
            stb.append(   "FROM   SCHREG_REGD_DAT ");
            stb.append(   "WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND GRADE =? ), ");

		//	欠点科目 04/12/17Modify------------------
			stb.append("TYPE_GROUP AS ( ");
			stb.append("    SELECT W2.GRADE, W2.HR_CLASS, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||  ");
            }
			stb.append("           SUBCLASSCD AS SUBCLASSCD, "+param[9]+"_TYPE_ASSES_CD  ");
			stb.append("    FROM   TYPE_GROUP_MST W1, TYPE_GROUP_HR_DAT W2 ");
			stb.append("    WHERE  W1.YEAR='"+param[0]+"' AND W1.YEAR=W2.YEAR AND ");
			stb.append("           W1.TYPE_GROUP_CD=W2.TYPE_GROUP_CD ),  ");

			stb.append("TYPE_ASSES AS ( ");
			stb.append("    SELECT TYPE_ASSES_CD,TYPE_ASSES_HIGH  ");
			stb.append("    FROM   TYPE_ASSES_MST ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND TYPE_ASSES_LEVEL='1' ),  ");

			stb.append("SUB_REP AS(  ");//---NO005
			stb.append("    SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' ||  ");
            }
			stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD ");
			stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT  ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND REPLACECD='1' ), ");
			stb.append("KIN_REC AS ( ");
			stb.append("    SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||  ");
            }
			stb.append("           SUBCLASSCD AS SUBCLASSCD,SCHREGNO,"+param[9]+"   ");
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND "+param[9]+" IS NOT NULL  ");
			stb.append("           AND SUBCLASSCD NOT IN ('900100') ");//---NO006
            stb.append(        " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
            stb.append("           AND ");//---NO005
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||  ");
            }
			stb.append("              SUBCLASSCD NOT IN (SELECT SUBCLASSCD FROM SUB_REP) ), ");//---NO005

            stb.append("KIN_MAIN AS( ");
			stb.append(		"SELECT K1.SUBCLASSCD, K1.SCHREGNO ");
			stb.append(		"FROM   KIN_REC K1, SCHNO K2, TYPE_GROUP K3, TYPE_ASSES K4 ");
			stb.append(		"WHERE  K1.SCHREGNO=K2.SCHREGNO AND K1.SUBCLASSCD=K3.SUBCLASSCD AND ");
			stb.append(		"       K2.GRADE=K3.GRADE AND K2.HR_CLASS=K3.HR_CLASS AND ");
			stb.append(		"       K1.SCHREGNO=K2.SCHREGNO AND K3."+param[9]+"_TYPE_ASSES_CD=K4.TYPE_ASSES_CD AND ");
			stb.append(		"       K1."+param[9]+" <= K4.TYPE_ASSES_HIGH ), ");

						//科目数
            stb.append("SUBCNT AS( ");
			stb.append(		"SELECT COUNT(K1.SUBCLASSCD) SUB_CNT, K1.SCHREGNO ");
			stb.append(		"       ,SUM(CASE WHEN VALUE(K5.ELECTDIV,'0') = '0' THEN 1 ELSE 0 END) SUB_CNT1 ");//---NO002
			stb.append(		"FROM   KIN_REC K1, SCHNO K2, TYPE_GROUP K3, TYPE_ASSES K4 ");
			stb.append(		"       ,SUBCLASS_MST K5 ");//---NO002
			stb.append(		"WHERE  K1.SCHREGNO=K2.SCHREGNO AND K1.SUBCLASSCD=K3.SUBCLASSCD AND ");
			stb.append(		"       K2.GRADE=K3.GRADE AND K2.HR_CLASS=K3.HR_CLASS AND ");
			stb.append(		"       K1.SCHREGNO=K2.SCHREGNO AND K3."+param[9]+"_TYPE_ASSES_CD=K4.TYPE_ASSES_CD AND ");
			stb.append(		"       K1."+param[9]+" <= K4.TYPE_ASSES_HIGH ");
            stb.append(     "       AND ");//---NO002
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          K5.CLASSCD || '-' || K5.SCHOOL_KIND || '-' || K5.CURRICULUM_CD || '-' ||  ");
            }
			stb.append(		"         K5.SUBCLASSCD = K1.SUBCLASSCD ");//---NO002
			stb.append(		"GROUP BY K1.SCHREGNO ), ");
						//欠点者
            stb.append("SCHNO_SUBCNT AS(");
			stb.append("    SELECT schregno ");
			stb.append("    FROM   SUBCNT ");
			if (param[3].equals("1")) stb.append("    WHERE  "+param[2]+" <= sub_cnt1 ) ");//指定科目数以上---NO002 sub_cnt→sub_cnt1
			if (param[3].equals("2")) stb.append("    WHERE   sub_cnt1 < "+param[2]+" ) ");//指定科目数未満---NO002 sub_cnt→sub_cnt1
						//メイン
			stb.append("SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          t2.CLASSCD || '-' || t2.SCHOOL_KIND || '-' || t2.CURRICULUM_CD || '-' ||  ");
            }
			stb.append("     t2.subclasscd as subclasscd, t2.subclassabbv, value(t2.electdiv,'0') electdiv ");
			stb.append("FROM ");
			stb.append("    (SELECT subclasscd ");
			stb.append("     FROM   KIN_MAIN ");
			stb.append("     WHERE  schregno in (SELECT schregno FROM SCHNO_SUBCNT ) ");
			stb.append("    GROUP BY subclasscd) t1  ");
			stb.append("    inner join subclass_mst t2 on t1.subclasscd=");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          t2.CLASSCD || '-' || t2.SCHOOL_KIND || '-' || t2.CURRICULUM_CD || '-' ||  ");
            }
			stb.append("      t2.subclasscd ");
			stb.append("ORDER BY value(t2.electdiv,'0'), ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          t2.CLASSCD || '-' || t2.SCHOOL_KIND || '-' || t2.CURRICULUM_CD || '-' ||  ");
            }
			stb.append("      t2.subclasscd ");//---NO001
			//stb.append("ORDER BY t2.subclasscd ");
//		//	欠点科目 04/12/17Modify------------------
//			/* 04/12/17Modify
//            stb.append("SUBCNT AS( ");
//			stb.append("     SELECT schregno, count(*) sub_cnt ");
//			stb.append("     FROM   kin_record_dat ");
//			stb.append("     WHERE  year='"+param[0]+"' AND  ");
//			stb.append("          ((judge_pattern='A' AND a_pattern_assess='1') or ");
//			stb.append("           (judge_pattern='B' AND b_pattern_assess='1') or ");
//			stb.append("           (judge_pattern='C' AND c_pattern_assess='1')) ");
//			stb.append("     GROUP BY schregno ), ");
//						//欠点者
//            stb.append("SCHNO_SUBCNT AS(");
//			stb.append("    SELECT t1.schregno ");
//			stb.append("    FROM   SUBCNT t1, SCHNO t2 ");
//			stb.append("    WHERE  t1.schregno=t2.schregno  ");
//			if (param[3].equals("1")) stb.append("    AND "+param[2]+" <= t1.sub_cnt ) ");//指定科目数以上
//			if (param[3].equals("2")) stb.append("    AND t1.sub_cnt < "+param[2]+" ) ");//指定科目数未満
//						//メイン
//			stb.append("SELECT t2.subclasscd, t2.subclassabbv, value(t2.electdiv,'0') electdiv ");
//			stb.append("FROM ");
//			stb.append("    (SELECT subclasscd ");
//			stb.append("     FROM   kin_record_dat ");
//			stb.append("     WHERE  year='"+param[0]+"' AND ");
//			stb.append("          ((judge_pattern='A' AND a_pattern_assess='1') or ");
//			stb.append("           (judge_pattern='B' AND b_pattern_assess='1') or ");
//			stb.append("           (judge_pattern='C' AND c_pattern_assess='1')) AND ");
//			stb.append("            schregno in (SELECT schregno FROM SCHNO_SUBCNT ) ");
//			stb.append("    GROUP BY subclasscd) t1  ");
//			stb.append("    inner join subclass_mst t2 on t1.subclasscd=t2.subclasscd ");
//			stb.append("ORDER BY t2.subclasscd ");
//			*/
		} catch( Exception e ){
			log.warn("Pre_Stat2 error!");
		}
		return stb.toString();

	}//Pre_Stat2()の括り


	/* 科目毎の得点
	 * 抽出条件 出力範囲の生徒で、類型評定が１。科目毎。
	 */
	String Pre_Stat3(String param[])
	{
		//欠点科目、学年、出力生徒範囲をパラメータとする
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO, GRADE, HR_CLASS ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND GRADE =? AND  ");
			stb.append("           GRADE||HR_CLASS||ATTENDNO >=? AND  ");
			stb.append("           GRADE||HR_CLASS||ATTENDNO <=? ),  ");

			stb.append("TYPE_GROUP AS ( ");
			stb.append("    SELECT W2.GRADE, W2.HR_CLASS, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||  ");
            }
			stb.append("           SUBCLASSCD AS SUBCLASSCD, "+param[9]+"_TYPE_ASSES_CD  ");
			stb.append("    FROM   TYPE_GROUP_MST W1, TYPE_GROUP_HR_DAT W2 ");
			stb.append("    WHERE  W1.YEAR='"+param[0]+"' AND W1.YEAR=W2.YEAR AND ");
			stb.append("           W1.TYPE_GROUP_CD=W2.TYPE_GROUP_CD ),  ");

			stb.append("TYPE_ASSES AS ( ");
			stb.append("    SELECT TYPE_ASSES_CD,TYPE_ASSES_HIGH  ");
			stb.append("    FROM   TYPE_ASSES_MST ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND TYPE_ASSES_LEVEL='1' ),  ");

			stb.append("KIN_REC AS ( ");
			stb.append("    SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||  ");
            }
			stb.append("           SUBCLASSCD AS SUBCLASSCD,SCHREGNO,"+param[9]+" ");
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND "+param[9]+" IS NOT NULL )  ");

			stb.append("SELECT K1.SCHREGNO, K1."+param[9]+" SCORE, K4.TYPE_ASSES_CD PATTERN  ");
			stb.append("FROM   KIN_REC K1, SCHNO K2, TYPE_GROUP K3, TYPE_ASSES K4  ");
			stb.append("WHERE  K1.SCHREGNO=K2.SCHREGNO AND K1.SUBCLASSCD=K3.SUBCLASSCD AND  ");
			stb.append("       K2.GRADE=K3.GRADE AND K2.HR_CLASS=K3.HR_CLASS AND  ");
			stb.append("       K1.SCHREGNO=K2.SCHREGNO AND K3."+param[9]+"_TYPE_ASSES_CD=K4.TYPE_ASSES_CD AND  ");
			stb.append("       K1."+param[9]+" <= K4.TYPE_ASSES_HIGH AND K1.SUBCLASSCD=? ");
//			/*
//			stb.append("SELECT schregno, grade_record, judge_pattern ");
//			stb.append("FROM kin_record_dat ");
//			stb.append("WHERE year='"+param[0]+"' AND subclasscd=? AND ");
//			stb.append("    ((judge_pattern='A' AND a_pattern_assess='1') or ");
//			stb.append("     (judge_pattern='B' AND b_pattern_assess='1') or ");
//			stb.append("     (judge_pattern='C' AND c_pattern_assess='1')) AND ");
//			stb.append("    schregno in (SELECT schregno ");
//			stb.append("                 FROM   schreg_regd_dat ");
//			stb.append("                 WHERE  year='"+param[0]+"' AND semester='"+param[1]+"' AND grade=? AND ");
//			stb.append("                        grade||hr_class||attendno >=? AND ");
//			stb.append("                        grade||hr_class||attendno <=? ) ");
//			*/
		} catch( Exception e ){
			log.warn("Pre_Stat3 error!", e);
		}
		return stb.toString();

	}//Pre_Stat3()の括り



	/* 生徒毎の欠点科目数
	 * 抽出条件 出力範囲の生徒で、類型評定が１。生徒毎。
	 */
	String Pre_Stat4(String param[])
	{
		//学年、出力生徒範囲をパラメータとする
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO, GRADE, HR_CLASS ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND GRADE =? AND  ");
			stb.append("           GRADE||HR_CLASS||ATTENDNO >=? AND  ");
			stb.append("           GRADE||HR_CLASS||ATTENDNO <=? ),  ");
			stb.append("TYPE_GROUP AS ( ");
			stb.append("    SELECT W2.GRADE, W2.HR_CLASS, ");
			if ("1".equals(_useCurriculumcd)) {
	            stb.append("          CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||  ");
			}
			stb.append("          SUBCLASSCD AS SUBCLASSCD, "+param[9]+"_TYPE_ASSES_CD  ");
			stb.append("    FROM   TYPE_GROUP_MST W1, TYPE_GROUP_HR_DAT W2 ");
			stb.append("    WHERE  W1.YEAR='"+param[0]+"' AND W1.YEAR=W2.YEAR AND ");
			stb.append("           W1.TYPE_GROUP_CD=W2.TYPE_GROUP_CD ),  ");
			stb.append("TYPE_ASSES AS ( ");
			stb.append("    SELECT TYPE_ASSES_CD,TYPE_ASSES_HIGH  ");
			stb.append("    FROM   TYPE_ASSES_MST ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND TYPE_ASSES_LEVEL='1' ),  ");
			stb.append("KIN_REC AS ( ");
			stb.append("    SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||  ");
            }
			stb.append("           SUBCLASSCD AS SUBCLASSCD,SCHREGNO,"+param[9]+" ");
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND "+param[9]+" IS NOT NULL ");
			stb.append("           AND SUBCLASSCD NOT IN ('900100') ");//---NO006
            stb.append(        " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
			stb.append(" ) ,SUB_REP AS(  ");//---NO005
			stb.append("    SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("          ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' ||  ");
            }
			stb.append("           ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD");
			stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT  ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND REPLACECD='1' ) ");
			stb.append(",KIN_MAIN AS(   ");//---NO005
			stb.append("	SELECT K1.SUBCLASSCD, K1.SCHREGNO, K5.ELECTDIV ");
			stb.append("	FROM   KIN_REC K1, SCHNO K2, TYPE_GROUP K3, TYPE_ASSES K4  ");
			stb.append("	       ,SUBCLASS_MST K5  ");
			stb.append("	WHERE  K1.SCHREGNO=K2.SCHREGNO AND K1.SUBCLASSCD=K3.SUBCLASSCD AND ");
			stb.append("	       K2.GRADE=K3.GRADE AND K2.HR_CLASS=K3.HR_CLASS AND ");
			stb.append("	       K1.SCHREGNO=K2.SCHREGNO AND K3."+param[9]+"_TYPE_ASSES_CD=K4.TYPE_ASSES_CD AND ");
			stb.append("	       K1."+param[9]+" <= K4.TYPE_ASSES_HIGH  ");
            stb.append("           AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("           K5.CLASSCD || '-' || K5.SCHOOL_KIND || '-' || K5.CURRICULUM_CD || '-' || ");
            }
			stb.append("	           K5.SUBCLASSCD = K1.SUBCLASSCD ) ");
						//メイン
			stb.append("SELECT t1.schregno, t1.sub_cnt ");
			stb.append("       ,t1.SUB_CNT1 ");//---NO001
			stb.append("FROM ");
						//欠点者情報 04/12/17Modify---NO005
			stb.append("   ( ");
			stb.append("    SELECT COUNT(T1.SUBCLASSCD) SUB_CNT, T1.SCHREGNO  ");
			stb.append("           ,SUM(CASE WHEN VALUE(T1.ELECTDIV,'0') = '0' THEN 1 ELSE 0 END) SUB_CNT1  ");
			stb.append("    FROM   KIN_MAIN T1 ");
			stb.append("    WHERE  NOT EXISTS(SELECT 'X' FROM SUB_REP T2 WHERE T2.ATTEND_SUBCLASSCD=T1.SUBCLASSCD) ");
			stb.append("    GROUP BY T1.SCHREGNO ");
//			/*---NO005
//			stb.append(		"SELECT COUNT(K1.SUBCLASSCD) SUB_CNT, K1.SCHREGNO ");
//			stb.append(		"       ,SUM(CASE WHEN VALUE(K5.ELECTDIV,'0') = '0' THEN 1 ELSE 0 END) SUB_CNT1 ");//---NO001
//			stb.append(		"FROM   KIN_REC K1, SCHNO K2, TYPE_GROUP K3, TYPE_ASSES K4 ");
//			stb.append(		"       ,SUBCLASS_MST K5 ");//---NO001
//			stb.append(		"WHERE  K1.SCHREGNO=K2.SCHREGNO AND K1.SUBCLASSCD=K3.SUBCLASSCD AND ");
//			stb.append(		"       K2.GRADE=K3.GRADE AND K2.HR_CLASS=K3.HR_CLASS AND ");
//			stb.append(		"       K1.SCHREGNO=K2.SCHREGNO AND K3."+param[9]+"_TYPE_ASSES_CD=K4.TYPE_ASSES_CD AND ");
//			stb.append(		"       K1."+param[9]+" <= K4.TYPE_ASSES_HIGH ");
//			stb.append(		"       AND K5.SUBCLASSCD = K1.SUBCLASSCD ");//---NO001
//			stb.append(		"GROUP BY K1.SCHREGNO ");
//			*/
			stb.append(    ") t1 ");
						//指定科目数
			if (param[3].equals("1")) stb.append("    WHERE "+param[2]+" <= t1.sub_cnt1 ");//以上---NO002 sub_cnt→sub_cnt1
			if (param[3].equals("2")) stb.append("    WHERE t1.sub_cnt1 < "+param[2]+"  ");//未満---NO002 sub_cnt→sub_cnt1
//			/*
//			stb.append("    (SELECT schregno, count(*) sub_cnt ");
//			stb.append("     FROM   kin_record_dat ");
//			stb.append("     WHERE  year='"+param[0]+"' AND  ");
//			stb.append("          ((judge_pattern='A' AND a_pattern_assess='1') or ");
//			stb.append("           (judge_pattern='B' AND b_pattern_assess='1') or ");
//			stb.append("           (judge_pattern='C' AND c_pattern_assess='1')) ");
//			stb.append("     GROUP BY schregno) t1, ");
//			stb.append("    (SELECT schregno ");
//			stb.append("     FROM   schreg_regd_dat ");
//			stb.append("     WHERE  year='"+param[0]+"' AND semester='"+param[1]+"' AND grade=? AND ");
//			stb.append("            grade||hr_class||attendno >=? AND ");
//			stb.append("            grade||hr_class||attendno <=?) t2 ");
//			stb.append("WHERE t1.schregno=t2.schregno  ");
//			*/
		} catch( Exception e ){
			log.warn("Pre_Stat4 error!", e);
		}
		return stb.toString();

	}//Pre_Stat4()の括り



	/**PrepareStatement close**/
	private void Pre_Stat_f(PreparedStatement ps1,PreparedStatement ps2,PreparedStatement ps3,PreparedStatement ps4)
	{
		try {
			ps1.close();
			ps2.close();
			ps3.close();
			ps4.close();
		} catch( Exception e ){
			log.warn("Preparedstatement-close error!");
		}
	}//Pre_Stat_f()の括り



	/**SVF-FORM-FIELD-INZ**/
	private void Svf_Int(Vrw32alp svf){

		for (int j=1; j<31; j++){
			svf.VrsOutn("HR_CLASS"		,j 	, "" );
			svf.VrsOutn("ATTENDNO"		,j 	, "" );
			svf.VrsOutn("NAME"			,j 	, "" );
		}

	}//Svf_Int()の括り



}//クラスの括り
