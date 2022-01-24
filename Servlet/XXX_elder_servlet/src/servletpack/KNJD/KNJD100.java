package servletpack.KNJD;

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
import servletpack.KNJD.detail.KNJ_Testname;
import servletpack.KNJZ.detail.KNJ_ClassCode;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Grade_Hrclass;
import servletpack.KNJZ.detail.KNJ_Semester;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ１００＞  中間個人成績表
 *
 *	2004/03/29 yamashiro・教科コード仕様の変更に伴う修正
 *						・RANK関数において０による除算による不具合を修正
 *						・１回の処理で複数生徒の処理へ変更（パフォーマンスの改善）
 *>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/

public class KNJD100 extends HttpServlet implements KNJ_ClassCode{
	Vrw32alp svf = new Vrw32alp(); 	//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB db2;						// Databaseクラスを継承したクラス
	PreparedStatement ps1;
	String kamoku_n[][]  = new String[20][2];    //科目名・科目コード
	String kamoku_s[][]  = new String[20][60];   //学級順位位置 科目毎の出席番号
	String kamoku_h[]    = new String[20];       //学級順位位置 科目毎の評定
	String schno[];					//出力対象学籍番号
	static int T_CNT = 15;			//１回当りの出力生徒数

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
	    String param[] = new String[7];
		//String schno[] = request.getParameterValues("category_selected");   	//学籍番号
		schno = request.getParameterValues("category_selected");   	//学籍番号

	// パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         					//年度
			param[1] = request.getParameter("GAKKI");   						//1-3:学期 4:学年末
			param[4] = request.getParameter("TEST").substring(0,2);  			//テスト種別
			param[5] = request.getParameter("TEST").substring(2); 	 			//テスト項目
			param[6] = request.getParameter("OUTPUT");							//学年データ出力有無フラグ

		//	'学年＋組'パラメータを分解
			String strx = request.getParameter("GRADE_HR_CLASS");				//学年＋組
			KNJ_Grade_Hrclass gradehrclass = new KNJ_Grade_Hrclass();			//クラスのインスタンス作成
			KNJ_Grade_Hrclass.ReturnVal returnval = gradehrclass.Grade_Hrclass(strx);
			param[2] = returnval.val1;											//学年
			param[3] = returnval.val2;											//組

		} catch( Exception ex ) {
			System.out.println("[KNJD100]parameter get error!");
			System.out.println(ex);
		}

	//	print設定
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	//	svf設定
		svf.VrInit();					   //クラスの初期化
		svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			System.out.println("[KNJD100]DB2 open error!");
			System.out.println(ex);
		}

        for(int ia=0 ; ia<param.length ; ia++) System.out.println("[KNJD100]param[" + ia + "]=" + param[ia]);

	//	ＳＶＦ作成処理
		Pre_Stat_1(param);									//preparestatement
		Set_Head(param);									//見出し出力
		boolean nonedata = false;
		for( int ia=0 ; ia<schno.length ; ia+=T_CNT ){
			if( Set_Detail(ia,Integer.parseInt(param[6])) )nonedata = true;
			if( ia>100 )	break;
		}

	//	該当データ無し
		if( !nonedata ){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		Pre_Stat_F();		//preparestatement
		db2.commit();
		db2.close();		//DBを閉じる
		outstrm.close();	//ストリームを閉じる 

    }//doGetの括り



	/** SVF-FORM **/
	private void Set_Head(String param[]){

		svf.VrSetForm("KNJD100.frm", 4);
		svf.VrsOut("YEAR"       ,nao_package.KenjaProperties.gengou
													(Integer.parseInt(param[0])) + "年度");		//年度

	//	ＳＶＦ属性変更--->出力形式がクラス別の場合クラス毎に改ページ
		//if( param[12].equals("1") )	ret = svf.VrAttribute("ATTENDCLASSCD","FF=1");

	//	作成日(現在処理日)の取得
		try {
			KNJ_Control control = new KNJ_Control();
			KNJ_Control.ReturnVal returnval = control.Control(db2);
			svf.VrsOut("TODAY"	,KNJ_EditDate.h_format_JP(returnval.val3));				//作成日
		} catch( Exception e ){
			System.out.println("[KNJD100]ctrl_date get error!");
			System.out.println( e );
		}
	//	学期名称の取得
		try {
			KNJ_Semester semester = new KNJ_Semester();
			KNJ_Semester.ReturnVal returnval = 
						semester.Semester(db2,param[0],param[1]);
			svf.VrsOut("term2"	,returnval.val1);				//学期名称
		} catch( Exception e ){
			System.out.println("[KNJD100]Semester name get error!");
			System.out.println( e );
		}
	//	担任名の取得
		try {
			KNJ_Grade_Hrclass hrclass = new KNJ_Grade_Hrclass();
			KNJ_Grade_Hrclass.ReturnVal returnval = 
						hrclass.Hrclass_Staff(db2,param[0],param[1],param[2],param[3]);
			svf.VrsOut("HR_NAME"		,returnval.val1);			//組名称
			svf.VrsOut("CLASSSTAFF"	,returnval.val3);			//担任名
		} catch( Exception e ){
			System.out.println("[KNJD100]test name get error!");
			System.out.println( e );
		}
	//	テスト名称の取得
		try {
			KNJ_Testname test = new KNJ_Testname();
			KNJ_Testname.ReturnVal returnval = test.TestName(db2,param[4]+""+param[5]);
			svf.VrsOut("term"		,returnval.val1);			//テスト名称
		} catch( Exception e ){
			System.out.println("[KNJD100]test name get error!");
			System.out.println( e );
		}
	// ＤＢ検索（科目名・評定学級順位の取得）
		try {
			db2.query( Pre_Stat_2(param) );
			ResultSet rs = db2.getResultSet();
			String f_kamoku = "0";  			//前レコードの科目コード
			int ia = 0;  						//科目の配列要素
			int ib = 0;  						//席次順位の配列要素
			for(int ic = 0 ; ic < 20 ; ic++) kamoku_h[ic] = "";
			String strx = null;
			while( rs.next() ){
				if( !f_kamoku.equals(rs.getString("SUBCLASSCD")) ){
					kamoku_n[ia][0] = rs.getString("SUBCLASSCD");    //科目コード
					kamoku_n[ia][1] = rs.getString("SUBCLASSNAME");  //科目名称
					f_kamoku = rs.getString("SUBCLASSCD");
					ia++;
					if(ia > kamoku_n.length) break;
					ib = 0;
				}
				if(ib > kamoku_s[ia-1].length) continue;
				kamoku_s[ia-1][ib] = rs.getString("SCHREGNO");  //学籍番号
				strx = "    " + rs.getString("SCORE");
				int intx = strx.length();
				kamoku_h[ia-1] = kamoku_h[ia-1] +strx.substring(intx-4,intx);  //評価
				ib++;
			}
		//	学級順位位置表ラベル
			StringBuffer bfr = new StringBuffer();
			for(int id=1 ; id<61 ; id++){
				if(id<10){
					bfr.append("   ");
					bfr.append(id);
				} else{
					bfr.append("  ");
					bfr.append(id);
				}
			}
			svf.VrsOut("positionlabel" , bfr.toString());
			bfr = null;
		} catch( Exception e ){
			System.out.println("[KNJD100]kamoku set error!");
			System.out.println( e );
		}

	}//Set_Headの括り



	/**SVF-FORM**/
	//private void Set_Detail(String schregno,String param[]){
	private boolean Set_Detail(int acnt,int par6){

		boolean nonedata = false;

		try {
			//for( int ia=1 ; ia<9 ; ia++ )	ps1.setString(ia,schregno);	//学籍番号
			for( int pp=1 ; pp<=T_CNT ; pp++,acnt++ ){
				if( acnt<schno.length )	ps1.setString(pp,schno[acnt]);
				else					ps1.setString(pp,null);
			}
			ResultSet rs = ps1.executeQuery();
			//System.out.println("[KNJD100]Set_Detail sql ok!");
			//System.out.println("[KNJD100]Set_Detail sql ps1="+String.valueOf(ps1));

			int ia = 0;		                    //科目列カウント
			StringBuffer stb = new StringBuffer();
			String inoutname = new String();
			String schregno = "000000";
			while( rs.next() ){
			//	生徒番号の変わり目
				if( !rs.getString("SCHREGNO").equals(schregno) ){
					if( !schregno.equals("000000") ){
						if( ia==0 ){
							svf.VrsOut("class_rank" , "  /  " );
							svf.VrsOut("year_rank" , "  /  " );
							svf.VrEndRecord();
							nonedata = true;
						}
					}
					Svf_Field_Cle();	//SVF-FIELDS初期化
					ia=0;		//科目列カウント
					schregno = rs.getString("SCHREGNO");
				}
			//	１回目の処理
				if( ia==0 ){
					svf.VrsOut("ATTENDNO" ,rs.getString("ATTENDNO"));	//出席番号
					svf.VrsOut("student"  ,rs.getString("S_NAME"));	//生徒名
					inoutname = String.valueOf(rs.getInt("ANNUAL")) + "年" ;
					if( rs.getString("INOUTCD").equals("1") )	inoutname = inoutname + "外生";
					else										inoutname = inoutname + "内生";
					svf.VrsOut("FIELD1"	,inoutname);				//内生・外生
					if( rs.getString("SUBCLASSCD") == null )	continue;
				//	総合平均レコードの処理
					if( rs.getString("SUBCLASSCD").equals("0") ){
						svf.VrsOut("grand_point"          , rs.getString("SCORE"));
						svf.VrsOut("grand_class_average"  , rs.getString("HR_AVG"));
						svf.VrsOut("grand_class_deviation", rs.getString("HR_HENSA"));
						if(rs.getInt("HR_SEKIJI") != 0)
							svf.VrsOut("grand_class_rank" , rs.getString("HR_SEKIJI") + "/" + rs.getString("HR_SEITOSU"));
						else
							svf.VrsOut("grand_class_rank" , "  /  " );
						if( par6==1 ){
							svf.VrsOut("grand_year_average"   , rs.getString("SC_AVG"));
							svf.VrsOut("grand_year_deviation" , rs.getString("SC_HENSA"));
							svf.VrsOut("grand_year_rank"  , "   /   " );
							if(rs.getInt("SC_SEKIJI") != 0)
								svf.VrsOut("grand_year_rank"  , rs.getString("SC_SEKIJI") + "/" + rs.getString("SC_SEITOSU"));
						}
						continue;
					}
				}
			//	科目の変わり目の処理 2004/08/01本人が受けている科目のみ出力へ変更
				for( ; ia<kamoku_n.length ; ia++)
					if( kamoku_n[ia][0].equals(rs.getString("SUBCLASSCD")) )break;
/* ************	while( !kamoku_n[ia][0].equals(rs.getString("SUBCLASSCD")) ){
					ret = svf.VrsOut("CLASSNAME"   ,  kamoku_n[ia][1]);
					ret = svf.VrsOut("CLASSNAME_2" ,  kamoku_n[ia][1]);
					ret = svf.VrEndRecord();
					nonedata = true ; //該当データなしフラグ
					ia++;
				}********************************* */
				if( !kamoku_n[ia][0].equals(rs.getString("SUBCLASSCD")) )continue;
			//	成績の記録明細の出力
				svf.VrsOut("CREDITS"         , String.valueOf(rs.getInt("CREDITS")));
				svf.VrsOut("class_time"      , String.valueOf(rs.getInt("JISU")));
				if( rs.getString("ATTEND_FLG").equals("1") ){
					svf.VrsOut("point2"          , rs.getString("SCORE"));
					svf.VrsOut("class_deviation" , rs.getString("HR_HENSA"));
					if( par6==1 )
						svf.VrsOut("yesr_deviation"  , rs.getString("SC_HENSA"));
				} else
					svf.VrsOut("point"           , "未受験");
				svf.VrsOut("class_average"   , rs.getString("HR_AVG"));
				if( rs.getInt("HR_SEKIJI") != 0 )
					svf.VrsOut("class_rank"      , rs.getString("HR_SEKIJI") + "/" + rs.getString("HR_SEITOSU"));
				else
					svf.VrsOut("class_rank"      , "  /  " );
				if( par6==1 ){
					svf.VrsOut("year_average"    , rs.getString("SC_AVG"));
					svf.VrsOut("year_rank"       , "   /   ");
					if(rs.getInt("SC_SEKIJI") != 0)
						svf.VrsOut("year_rank"       , rs.getString("SC_SEKIJI") + "/" + rs.getString("SC_SEITOSU"));
				}

			//	学級順位位置表
				stb.append(kamoku_h[ia]);
				for(int id=0 ; id<kamoku_s[ia].length ; id++){
					if(rs.getString("SCHREGNO").equals(kamoku_s[ia][id])){
						if(stb.length() > id*4)		stb.setCharAt(id * 4,'(');
						if(stb.length() > id*4+4)	stb.setCharAt(id * 4 + 4,')');
						else						stb.append(')');
						break;
					}
				}
				svf.VrsOut("position" , stb.toString());
				stb.delete(0,stb.length());
				if(par6==1){
				//	学年偏差値グラフ
					for(int id = 50 ; id <= rs.getInt("SC_HENSA") ; id++)		stb.append("*");
					svf.VrsOut("graph" , stb.toString()  );
					stb.delete(0,stb.length());
				}
			//	科目名
				svf.VrsOut("CLASSNAME"   ,  kamoku_n[ia][1]);
				svf.VrsOut("CLASSNAME_2" ,  kamoku_n[ia][1]);
				svf.VrEndRecord();
				nonedata = true ; //該当データなしフラグ
				ia++;
			}// whileの括り
			rs.close();
			//if( !nonedata ){
			if( ia==0 ){
				svf.VrsOut("class_rank" , "  /  " );
				svf.VrsOut("year_rank" , "  /  " );
				svf.VrEndRecord();
				nonedata = true;
			}
			Svf_Field_Cle();
			stb = null;
			inoutname = null;
			//System.out.println("[KNJD100]Set_Detail read ok!");
		} catch( Exception e ){
			System.out.println("[KNJD100]Set_Detail read error!");
			System.out.println( e );
		}

		return nonedata;

	}//Set_Detailの括り



	/**PrepareStatement作成**/
	void Pre_Stat_1(String param[]){

		try {
			StringBuffer stb = new StringBuffer();

			//一度に処理する生徒の表
			stb.append("WITH T_SCHREGNO AS(SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND GRADE='"+param[2]+"' AND SCHREGNO IN (");
			for( int ia=0 ; ia<T_CNT ; ia++ ){
				if( ia>0 ) stb.append(",");
				stb.append("?");
			}
			stb.append(")),");

			//コース内情報
			stb.append("COURSE AS("
						+ "SELECT VALUE(W1.SUBCLASSCD,'0') AS SUBCLASSCD,"
							   + "COUNT(DISTINCT W1.SCHREGNO) AS SEITOSU,"
							   + "ROUND(AVG(FLOAT(W1.SCORE))*100,0)/100 AS HEIKIN,"
							   + "ROUND(STDDEV(FLOAT(W1.SCORE))*100,0)/100 AS H_HENSA "
						+ "FROM ( SELECT SUBCLASSCD,SCHREGNO,SCORE "
							   + "FROM   TESTSCORE_DAT "
							   + "WHERE  ATTEND_FLG='1' AND SUBSTR(SUBCLASSCD,1,2)<='"+subject_U+"' AND "
									  + "YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND "
									  + "TESTKINDCD='"+param[4]+"' AND TESTITEMCD='"+param[5]+"')W1 "
							+ "INNER JOIN ("
							   + "SELECT SCHREGNO "
							   + "FROM   SCHREG_REGD_DAT W2 "
							   + "WHERE  W2.YEAR = '" +  param[0] + "' AND W2.SEMESTER = '" +  param[1] + "' AND "
									  + "EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W3 "
											  + "WHERE  W3.GRADE = W2.GRADE AND W3.COURSECD = W2.COURSECD AND "
													 + "W3.MAJORCD = W2.MAJORCD AND "
													 + "VALUE(W3.COURSECODE,'0000')=VALUE(W2.COURSECODE,'0000') AND "
													 + "W3.SCHREGNO IN(SELECT SCHREGNO FROM T_SCHREGNO)AND "
													 + "W3.YEAR='"+param[0]+"' AND W3.SEMESTER='"+param[1]+"')"
							+ ")W2 ON W1.SCHREGNO=W2.SCHREGNO "
					    + "GROUP BY GROUPING SETS (SUBCLASSCD,())),");
			//学級内情報
			stb.append("HR_CLASS AS("
						+ "SELECT VALUE(W1.SUBCLASSCD,'0') AS SUBCLASSCD,"
							   + "COUNT(DISTINCT W1.SCHREGNO) AS SEITOSU,"
							   + "ROUND(AVG(FLOAT(W1.SCORE))*100,0)/100 AS HEIKIN,"
							   + "ROUND(STDDEV(FLOAT(W1.SCORE))*100,0)/100 AS H_HENSA "
						+ "FROM ( SELECT SUBCLASSCD,SCHREGNO,SCORE "
							   + "FROM   TESTSCORE_DAT "
							   + "WHERE  ATTEND_FLG = '1' AND SUBSTR(SUBCLASSCD,1,2)<='"+subject_U+"' AND "
									  + "YEAR = '" +  param[0] + "' AND SEMESTER = '" +  param[1] + "' AND "
									  + "TESTKINDCD = '" +  param[4] + "' AND TESTITEMCD = '" +  param[5] + "' )W1 "
							+ "INNER JOIN ("
							   + "SELECT SCHREGNO "
							   + "FROM   SCHREG_REGD_DAT W2 "
							   + "WHERE  W2.YEAR = '" +  param[0] + "' AND W2.SEMESTER = '" +  param[1] + "' AND "
									  + "EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W3 "
											  + "WHERE  W3.GRADE = W2.GRADE AND W3.HR_CLASS = W2.HR_CLASS AND "
													 + "W3.SCHREGNO IN (SELECT SCHREGNO FROM T_SCHREGNO)AND "
													 + "W3.YEAR='"+param[0]+"' AND W3.SEMESTER='"+param[1]+"')"
							+ ")W2 ON W1.SCHREGNO=W2.SCHREGNO "
					    + "GROUP BY GROUPING SETS (SUBCLASSCD,()))");
			
			//メインの表
			stb.append("SELECT T2.SCHREGNO,T2.ATTENDNO,T2.NAME AS S_NAME,T2.ANNUAL,"
						    + "VALUE(T2.INOUTCD,'0') AS INOUTCD,T1.SUBCLASSCD,T1.ATTEND_FLG,"
						    + "T1.SCORE,T4.CREDITS,T4.JISU,"
						    + "DECIMAL(ROUND(T5.HEIKIN*10,0)/10,5,1) AS HR_AVG,"
						    + "DECIMAL(ROUND(T6.HEIKIN*10,0)/10,5,1) AS SC_AVG,"
						    + "T8.HENSA AS HR_HENSA,T9.HENSA AS SC_HENSA,"
						    + "T8.SEKIJI AS HR_SEKIJI,T9.SEKIJI AS SC_SEKIJI,"
						    + "T5.SEITOSU AS HR_SEITOSU,T6.SEITOSU AS SC_SEITOSU ");
			//個人情報
			stb.append("FROM ( SELECT W1.SCHREGNO,ATTENDNO,ANNUAL,INOUTCD,NAME "
							+ "FROM   SCHREG_REGD_DAT W1,SCHREG_BASE_MST W2 "
							+ "WHERE  W1.SCHREGNO IN(SELECT SCHREGNO FROM T_SCHREGNO)AND "
								   + "W1.YEAR='"+param[0]+"' AND W1.SEMESTER='"+param[1]+"' AND "
								   + "W1.SCHREGNO=W2.SCHREGNO )T2 ");
			//テスト情報
			stb.append(	"LEFT JOIN("
						    + "SELECT SCHREGNO,VALUE(SUBCLASSCD,'0') AS SUBCLASSCD,"
								   + "DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS SCORE,"
								   + "MAX(ATTEND_FLG) AS ATTEND_FLG "
							+ "FROM   TESTSCORE_DAT "
							+ "WHERE  SCHREGNO IN (SELECT SCHREGNO FROM T_SCHREGNO) AND "
								   + "YEAR = '" +  param[0] + "' AND SEMESTER = '" +  param[1] + "' AND "
								   + "TESTKINDCD = '" +  param[4] + "' AND TESTITEMCD = '" +  param[5] + "' AND "
								   + "SUBSTR(SUBCLASSCD,1,2)<='"+subject_U+"' "
							+ "GROUP BY GROUPING SETS (SCHREGNO,(SCHREGNO,SUBCLASSCD))"
						+ ")T1  ON T1.SCHREGNO = T2.SCHREGNO ");
			//偏差値
			stb.append("LEFT JOIN HR_CLASS T5 ON T1.SUBCLASSCD = T5.SUBCLASSCD ");
			stb.append("LEFT JOIN COURSE T6 ON T1.SUBCLASSCD = T6.SUBCLASSCD ");
			//科目単位・授業時数 <T4>
			stb.append("LEFT JOIN ("
							+ "SELECT DISTINCT W1.SCHREGNO,W3.SUBCLASSCD,W3.CREDITS,"
								   + "VALUE(W3.CREDITS,0)*VALUE(W2.CLASSWEEKS,0)-VALUE(W4.SUSPEND,0)"
										+ "-VALUE(W4.MOURNING,0) AS JISU "
							+ "FROM ( SELECT SCHREGNO,GRADE,HR_CLASS,COURSECD,MAJORCD,COURSECODE "
								   + "FROM   SCHREG_REGD_DAT "
								   + "WHERE  SCHREGNO IN (SELECT SCHREGNO FROM T_SCHREGNO) AND "
										  + "YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' )W1 "
								+ "INNER JOIN SCHREG_REGD_HDAT W2 ON W2.YEAR='"+param[0]+"' AND W2.SEMESTER='"+param[1]+"' AND "
													+ "W1.GRADE=W2.GRADE AND W1.HR_CLASS=W2.HR_CLASS "
								+ "INNER JOIN CREDIT_MST W3 ON W3.YEAR='"+param[0]+"' AND W1.GRADE=W3.GRADE AND "
													+ "W1.COURSECD=W3.COURSECD AND W1.MAJORCD=W3.MAJORCD AND "
													+ "VALUE(W1.COURSECODE,'0000') = VALUE(W3.COURSECODE,'0000') "
								+ "LEFT JOIN( "
									+ "SELECT SCHREGNO,SUBCLASSCD,SUM(SUSPEND) AS SUSPEND,SUM(MOURNING) AS MOURNING "
									+ "FROM   ATTEND_SUBCLASS_DAT "
									+ "WHERE  SCHREGNO IN (SELECT SCHREGNO FROM T_SCHREGNO) AND "
										   + "YEAR = '" +  param[0] + "' AND SEMESTER = '" +  param[1] + "' AND "
										   + "CLASSCD<='"+subject_U+"' "
									+ "GROUP BY SCHREGNO,SUBCLASSCD "
								+ ")W4 ON W4.SCHREGNO = W1.SCHREGNO AND W4.SUBCLASSCD = W3.SUBCLASSCD "
					 + ")T4 ON T1.SUBCLASSCD = T4.SUBCLASSCD AND T1.SCHREGNO = T4.SCHREGNO ");
				
			//学級単位の生徒数・席次・平均・偏差値の列 <T8> 
			stb.append("LEFT  JOIN ("
						+ "SELECT DISTINCT S1.SCHREGNO,S1.SUBCLASSCD,"
							   + "CASE S2.H_HENSA WHEN 0 THEN 0 "
									+ "ELSE ROUND((S1.SCORE-S2.HEIKIN)/S2.H_HENSA*10*10,0)/10+50 END AS HENSA,"
							   + "CASE S2.H_HENSA WHEN 0 THEN 0 "
									+ "ELSE RANK() OVER(PARTITION BY S1.SUBCLASSCD "
									+ "ORDER BY ROUND((S1.SCORE-S2.HEIKIN)/case S2.H_HENSA when 0 then 1 else S2.H_HENSA end *10*10,0)/10+50 DESC) END AS SEKIJI "
						+ "FROM ( SELECT W1.SCHREGNO,VALUE(W1.SUBCLASSCD,'0') AS SUBCLASSCD,"
									  + "ROUND(AVG(FLOAT(SCORE))*100,0)/100 AS SCORE "
							   + "FROM ( SELECT SUBCLASSCD,SCHREGNO,SCORE "
									  + "FROM   TESTSCORE_DAT "
									  + "WHERE  ATTEND_FLG = '1' AND SUBSTR(SUBCLASSCD,1,2)<='"+subject_U+"' AND "
											 + "YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND "
											 + "TESTKINDCD='"+param[4]+"' AND TESTITEMCD='"+param[5]+"' )W1 "
									+ "INNER JOIN ("
									  + "SELECT SCHREGNO "
								  	  + "FROM   SCHREG_REGD_DAT W2 "
									  + "WHERE  W2.YEAR = '" +  param[0] + "' AND W2.SEMESTER = '" +  param[1] + "' AND "
											 + "EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W3 "
													 + "WHERE  W3.GRADE = W2.GRADE AND W3.HR_CLASS = W2.HR_CLASS AND "
															+ "W3.SCHREGNO IN (SELECT SCHREGNO FROM T_SCHREGNO) AND "
															+ "W3.YEAR = '" +  param[0] + "' AND W3.SEMESTER = '" +  param[1] + "')"
									+ ")W2 ON W1.SCHREGNO = W2.SCHREGNO "
							   + "GROUP BY GROUPING SETS (W1.SCHREGNO,(W1.SCHREGNO,W1.SUBCLASSCD)))S1,"
						+ "HR_CLASS S2 "
						+ "WHERE S1.SUBCLASSCD = S2.SUBCLASSCD "
					+ ")T8 ON T1.SCHREGNO = T8.SCHREGNO AND T1.SUBCLASSCD = T8.SUBCLASSCD ");
					
			//校内の生徒数・席次・平均・偏差値の列  <T9> 
			stb.append("LEFT JOIN ("
						+ "SELECT DISTINCT S1.SCHREGNO,S1.SUBCLASSCD,"
							   + "CASE S2.H_HENSA WHEN 0 THEN 0 "
									+ "ELSE ROUND((S1.SCORE-S2.HEIKIN)/S2.H_HENSA*10*10,0)/10+50 END AS HENSA,"
							   + "CASE S2.H_HENSA WHEN 0 THEN 0 "
									+ "ELSE RANK() OVER(PARTITION BY S1.SUBCLASSCD "
									+ "ORDER BY ROUND((S1.SCORE-S2.HEIKIN)/case S2.H_HENSA when 0 then 1 else S2.H_HENSA end *10*10,0)/10+50 DESC) END AS SEKIJI "
						+ "FROM ( SELECT W1.SCHREGNO,VALUE(W1.SUBCLASSCD,'0') AS SUBCLASSCD,"
									  + "ROUND(AVG(FLOAT(SCORE))*100,0)/100 AS SCORE "
							   + "FROM ( SELECT SUBCLASSCD,SCHREGNO,SCORE "
									  + "FROM   TESTSCORE_DAT "
									  + "WHERE  ATTEND_FLG='1' AND SUBSTR(SUBCLASSCD,1,2)<='"+subject_U+"' AND "
											 + "YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND "
											 + "TESTKINDCD='"+param[4]+"' AND TESTITEMCD='"+param[5]+"')W1 "
								  + "INNER JOIN ("
									  + "SELECT SCHREGNO "
									  + "FROM   SCHREG_REGD_DAT W2 "
									  + "WHERE  W2.YEAR='"+param[0]+"' AND W2.SEMESTER='"+param[1]+"' AND "
											 + "EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W3 "
													 + "WHERE  W3.GRADE=W2.GRADE AND W3.COURSECD=W2.COURSECD AND "
															+ "W3.MAJORCD=W2.MAJORCD AND "
															+ "VALUE(W3.COURSECODE,'0000')=VALUE(W2.COURSECODE,'0000')AND "
															+ "W3.SCHREGNO IN(SELECT SCHREGNO FROM T_SCHREGNO)AND "
															+ "W3.YEAR='"+param[0]+"' AND W3.SEMESTER='"+param[1]+"')"
								  + ")W2 ON W1.SCHREGNO = W2.SCHREGNO "
							   + "GROUP BY GROUPING SETS (W1.SCHREGNO,(W1.SCHREGNO,W1.SUBCLASSCD)))S1,"
							   + "COURSE S2 "
						+ "WHERE  S1.SUBCLASSCD=S2.SUBCLASSCD "
					 + ")T9 ON T1.SCHREGNO=T9.SCHREGNO AND T1.SUBCLASSCD=T9.SUBCLASSCD ");
			stb.append("ORDER BY T2.ATTENDNO,SUBCLASSCD");

			ps1 = db2.prepareStatement(stb.toString());
		} catch( Exception e ){
			System.out.println("[KNJD100]Pre_Stat_1 error!");
			System.out.println( e );
		}

	}//Pre_Stat_1の括り



	/**PrepareStatement作成**/
	String Pre_Stat_2(String param[]){

		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT T1.SUBCLASSCD,T3.SUBCLASSNAME,T1.SCHREGNO,T1.SCORE ");
			stb.append("FROM   TESTSCORE_DAT T1 ");
			stb.append("INNER JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO=T2.SCHREGNO AND T1.SEMESTER=T2.SEMESTER AND T1.YEAR=T2.YEAR ");
			stb.append("LEFT  JOIN SUBCLASS_MST T3 ON T1.SUBCLASSCD=T3.SUBCLASSCD ");
			stb.append("WHERE  T2.YEAR='"+param[0]+"' AND T2.SEMESTER='"+param[1]+"' AND ");
			stb.append(		  "T2.GRADE='"+param[2]+"' AND T2.HR_CLASS='"+param[3]+"' AND ");
			stb.append(		  "T1.TESTKINDCD='"+param[4]+"' AND T1.TESTITEMCD='"+param[5]+"' AND ");
			stb.append(		  "SUBSTR(T1.SUBCLASSCD,1,2)<='"+subject_U+"' AND T1.ATTEND_FLG='1' ");
			stb.append("ORDER BY T1.SUBCLASSCD,T1.SCORE DESC");
		} catch( Exception e ){
			System.out.println("[KNJD100]String Pre_Stat_2() error!");
			System.out.println( e );
		}
		return stb.toString();

	}//String Pre_Stat_2()の括り



	/**PrepareStatement close**/
	private void Pre_Stat_F(){

		try {
			ps1.close();
		} catch( Exception e ){
			System.out.println("[KNJD100]Pre_Stat_F error!");
			System.out.println( e );
		}
	}//Pre_Stat_Fの括り



	/**SVF-FIELD INZ**/
	private void Svf_Field_Cle(){

		svf.VrsOut("ATTENDNO"   			,"");
		svf.VrsOut("student"    			,"");
		svf.VrsOut("grand_point"  		,"");
		svf.VrsOut("grand_class_average"  ,"");
		svf.VrsOut("grand_class_deviation","");
		svf.VrsOut("grand_class_rank" 	, "  /  " );
		svf.VrsOut("grand_year_average"   ,"");
		svf.VrsOut("grand_year_deviation" ,"");
		svf.VrsOut("grand_year_rank"  	, "   /   " );
	}//Svf_Field_Cleの括り



}//クラスの括り
