package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ２９０Ｋ＞  成績表（短冊）
 *
 *	2004/08/10 nakamoto 作成日
 *	2004/08/17 nakamoto 教科コード'90'未満を出力
 *	2004/09/06 nakamoto フォームＩＤを変更
 *
 *	2006/05/17 nakamoto 教科コード'90'以内を出力に仕様変更
 */

public class KNJA290K {

    private static final Log log = LogFactory.getLog(KNJA290K.class);
    
    private String _useCurriculumcd;
    private KNJDefineSchool _definecode;

	/**
	 * HTTP Get リクエストの処理
	 */
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[13];

	//	パラメータの取得
		String classcd[] = request.getParameterValues("CLASS_SELECTED");   			//学年・組
		try {
	        param[0] = request.getParameter("YEAR");         						//年度
			param[1] = request.getParameter("GAKKI");   							//学期
			param[2] = request.getParameter("KENSUU");   							//出力件数
			_useCurriculumcd = request.getParameter("useCurriculumcd");
		} catch( Exception ex ) {
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
		} catch( Exception ex ) {
			log.warn("DB2 open error!", ex);
		}
        _definecode = new KNJDefineSchool();
        _definecode.defineCode(db2, param[0]);

	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		PreparedStatement ps4 = null;
		PreparedStatement ps5 = null;
		PreparedStatement ps6 = null;
		PreparedStatement ps7 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		Set_Head(db2,svf,param);								//見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(Pre_Stat1(param));		//生徒preparestatement
			ps2 = db2.prepareStatement(Pre_Stat2(param));		//学級・担任preparestatement
			ps3 = db2.prepareStatement(Pre_Stat3(param));		//コースpreparestatement
			ps4 = db2.prepareStatement(Pre_Stat4(param));		//科目担任preparestatement
			ps5 = db2.prepareStatement(Pre_Stat5(param));		//単位数preparestatement
			ps6 = db2.prepareStatement(Pre_Stat6(param));		//科目名preparestatement
			ps7 = db2.prepareStatement(Pre_Stat7(param));		//生徒名preparestatement
		} catch( Exception ex ) {
			log.warn("DB2 open error!", ex);
		}
		//SVF出力
		for( int ia=0 ; ia<classcd.length ; ia++ ){
			Set_Detail_2(db2,svf,param,classcd[ia],ps2);							//学級・担任出力のメソッド
			Set_Detail_3(db2,svf,param,classcd[ia],ps3);							//コース取得のメソッド
			if( Set_Detail_1(db2,svf,param,classcd[ia],ps1,ps4,ps5,ps6,ps7) )nonedata = true;	//帳票出力のメソッド
		}
log.debug("nonedata = " + nonedata);
	//	該当データ無し
		if( !nonedata ){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		Pre_Stat_f(ps1,ps2,ps3,ps4,ps5,ps6,ps7);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** SVF-FORM **/
	private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

		svf.VrSetForm("KNJA290.frm", 1);	//04/09/06	KIN27
		param[12] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";

	//	ＳＶＦ属性変更--->出力形式がクラス別の場合クラス毎に改ページ
		//ret = svf.VrAttribute("SUBCLASS1","FF=1");

	}//Set_Head()の括り



	/**SVF-FORM**/
	private boolean Set_Detail_1(DB2UDB db2,Vrw32alp svf,String param[],String hrclasscd,PreparedStatement ps1
						,PreparedStatement ps4,PreparedStatement ps5,PreparedStatement ps6,PreparedStatement ps7)
	{
		boolean nonedata = false;

		try {
			int pp = 0;
			ps1.setString(++pp,hrclasscd);	//学年・組
			if ("KIN".equals(_definecode.schoolmark) && "03".equals(hrclasscd.substring(0, 2))) {
//	            ps1.setString(++pp,"90");  //学年・組
			    ps1.setString(++pp,"89");  //学年・組
			} else {
			    ps1.setString(++pp,"89");  //学年・組
			}
			ResultSet rs = ps1.executeQuery();
log.debug("gr_cl = " + hrclasscd);

			while( rs.next() ){
			//	出力件数
                if ("1".equals(_useCurriculumcd)) {
                    log.debug("sub = " + rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD") + ", chair = " + rs.getString("CHAIRCD"));
                } else {
                    log.debug("sub = " + rs.getString("SUBCLASSCD") + ", chair = " + rs.getString("CHAIRCD"));
                }
				for( int ib=0 ; ib<Integer.parseInt(param[2]) ; ib++ ){
				//	各種メソッド
					//科目担任
					Set_Detail_4(db2,svf,param,hrclasscd,ps4,rs.getString("CHAIRCD"));
					String classcd = null, schoolkind = null, curriculumcd = null;
					if ("1".equals(_useCurriculumcd)) {
					    classcd = rs.getString("CLASSCD");
					    schoolkind = rs.getString("SCHOOL_KIND");
					    curriculumcd = rs.getString("CURRICULUM_CD");
					}
                    //単位数
                    Set_Detail_5(db2,svf,param,hrclasscd,ps5,classcd, schoolkind, curriculumcd, rs.getString("SUBCLASSCD"));
                    //科目名
                    Set_Detail_6(db2,svf,param,hrclasscd,ps6,classcd, schoolkind, curriculumcd, rs.getString("SUBCLASSCD"));
					//生徒名
					Set_Detail_7(db2,svf,param,hrclasscd,ps7,rs.getString("CHAIRCD"));
				}

				nonedata = true;
			}
			rs.close();
		} catch( Exception ex ) {
			log.warn("Set_Detail_1 read error!", ex);
		}
		return nonedata;

	}//Set_Detail_1()の括り



	/**SVF-FORM**/
	private void Set_Detail_2(DB2UDB db2,Vrw32alp svf,String param[],String classcd,PreparedStatement ps2)
	{
		try {
			int pp = 0;
			ps2.setString(++pp,classcd);	//年組
			ResultSet rs = ps2.executeQuery();

			if( rs.next() ){
				//ret = svf.VrsOut("HR_NAME" 		, rs.getString("HR_NAMEABBV") );	//組略称
				//ret = svf.VrsOut("STAFFNAME1" 	, rs.getString("STAFFNAME") );		//学級担任
				param[7] = rs.getString("HR_NAMEABBV");
				param[8] = rs.getString("STAFFNAME");
			} else {
				//ret = svf.VrsOut("HR_NAME" 		, "" );
				//ret = svf.VrsOut("STAFFNAME1" 	, "" );
				param[7] = "";
				param[8] = "";
			}
			rs.close();
		} catch( Exception ex ) {
			log.warn("Set_Detail_2 read error!", ex);
		}

	}//Set_Detail_2()の括り



	/**SVF-FORM**/
	private void Set_Detail_3(DB2UDB db2,Vrw32alp svf,String param[],String classcd,PreparedStatement ps3)
	{
		try {
			int pp = 0;
			ps3.setString(++pp,classcd);	//年組
			ResultSet rs = ps3.executeQuery();

			if( rs.next() ){
				param[3] = rs.getString("COURSECD");
				param[4] = rs.getString("MAJORCD");
				param[5] = rs.getString("COURSECODE");
				param[6] = rs.getString("GRADE");
			}
			rs.close();
		} catch( Exception ex ) {
			log.warn("Set_Detail_3 read error!", ex);
		}

	}//Set_Detail_3()の括り



	/**SVF-FORM**/
	private void Set_Detail_4(DB2UDB db2,Vrw32alp svf,String param[],String classcd
										,PreparedStatement ps4,String chaircd)
	{
		try {
			int pp = 0;
			ps4.setString(++pp,chaircd);	//講座コード
			ResultSet rs = ps4.executeQuery();

			if( rs.next() ){
				//ret = svf.VrsOut("STAFFNAME2" 	, rs.getString("STAFFNAME") );		//科目担任
				param[9] = rs.getString("STAFFNAME");
			} else {
				//ret = svf.VrsOut("STAFFNAME2" 	, "" );
				param[9] = "";
			}
			rs.close();
		} catch( Exception ex ) {
			log.warn("Set_Detail_4 read error!", ex);
		}

	}//Set_Detail_4()の括り



	/**SVF-FORM**/
	private void Set_Detail_5(DB2UDB db2,Vrw32alp svf,String param[],String hrclasscd
										,PreparedStatement ps5,String classcd, String schoolkind, String curriculumcd, String subclasscd)
	{
		try {
			int pp = 0;
			ps5.setString(++pp,param[3]);	//coursecd
			ps5.setString(++pp,param[4]);	//majorcd
			ps5.setString(++pp,param[6]);	//grade
			ps5.setString(++pp,param[5]);	//coursecode
			if ("1".equals(_useCurriculumcd)) {
                ps5.setString(++pp,classcd);  //教科コード
                ps5.setString(++pp,schoolkind);
                ps5.setString(++pp,curriculumcd);
			} else {
			    ps5.setString(++pp,subclasscd.substring(0,2));  //教科コード
			}
			ps5.setString(++pp,subclasscd);	//科目コード
			ResultSet rs = ps5.executeQuery();

			if( rs.next() ){
				//ret = svf.VrsOut("CREDIT" 	, rs.getString("CREDITS") );		//単位数
				param[10] = rs.getString("CREDITS");
			} else {
				//ret = svf.VrsOut("CREDIT" 	, "" );
				param[10] = "";
			}
			rs.close();
		} catch( Exception ex ) {
			log.warn("Set_Detail_5 read error!", ex);
		}

	}//Set_Detail_5()の括り



	/**SVF-FORM**/
	private void Set_Detail_6(DB2UDB db2,Vrw32alp svf,String param[],String hrclasscd
										,PreparedStatement ps6,String classcd, String schoolKind, String curriculumCd, String subclasscd)
	{
		try {
			int pp = 0;
			if ("1".equals(_useCurriculumcd)) {
	            ps6.setString(++pp,classcd);
	            ps6.setString(++pp,schoolKind);
	            ps6.setString(++pp,curriculumCd);
			}
			ps6.setString(++pp,subclasscd);	//科目コード
			ResultSet rs = ps6.executeQuery();

			if( rs.next() ){
				//ret = svf.VrsOut("SUBCLASS" 	, rs.getString("SUBCLASSABBV") );		//科目名
				param[11] = rs.getString("SUBCLASSABBV");
			} else {
				//ret = svf.VrsOut("SUBCLASS" 	, "" );
				param[11] = "";
			}
			rs.close();
		} catch( Exception ex ) {
			log.warn("Set_Detail_6 read error!", ex);
		}

	}//Set_Detail_6()の括り



	/**SVF-FORM**/
	private void Set_Detail_7(DB2UDB db2,Vrw32alp svf,String param[],String classcd
													,PreparedStatement ps7,String chaircd)
	{
		try {
			int pp = 0;
			ps7.setString(++pp,classcd);	//年組コード
			ps7.setString(++pp,chaircd);	//講座コード
			ResultSet rs = ps7.executeQuery();

			boolean nonedata = false;
			String strx;
			String stry;
			String strz;
			int x;					//姓の文字数
			int y;					//名の文字数
			int z;					//空白文字の位置
			int gyo = 0;			//行数
			while( rs.next() ){
				gyo = rs.getInt("ATTENDNO");		//出席番号
				if ( gyo > 50 ){
					if ( !nonedata ){
						Svf_Int(svf,param);							//ヘッダ出力
						svf.VrEndPage();
					}
					gyo = gyo - 50;
					nonedata = true;
				}
			//	生徒漢字・規則に従って出力
				strz = rs.getString("NAME");
				z = strz.indexOf("　");
				if ( z < 0 ){
					svf.VrsOutn("NAME" 	,gyo	, strz );			//空白がない
				} else {
					strx = strz.substring(0,z);
					stry = strz.substring(z+1);
					x = strx.length();
					y = stry.length();
					if ( x == 1 ){
						svf.VrsOutn("LNAME2" 	,gyo	, strx );		//姓１文字
					} else {
						svf.VrsOutn("LNAME1" 	,gyo	, strx );		//姓２文字以上
					}
					if ( y == 1 ){
						svf.VrsOutn("FNAME2" 	,gyo	, stry );		//名１文字
					} else {
						svf.VrsOutn("FNAME1" 	,gyo	, stry );		//名２文字以上
					}
				}
			}
			rs.close();
			if ( gyo > 0 ){
				Svf_Int(svf,param);							//ヘッダ出力
				svf.VrEndPage();
			}
		} catch( Exception ex ) {
			log.warn("Set_Detail_7 read error!", ex);
		}

	}//Set_Detail_7()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
            if ("1".equals(_useCurriculumcd)) {
                stb.append("SELECT t1.chaircd,t2.classcd,t2.school_kind,t2.curriculum_cd,t2.subclasscd FROM ");
            } else {
                stb.append("SELECT t1.chaircd,t2.subclasscd FROM ");
            }
			stb.append("(SELECT w1.chaircd FROM chair_std_dat w1,schreg_regd_dat w2 WHERE ");
			stb.append("w2.year='"+param[0]+"' AND w2.semester='"+param[1]+"' AND w2.grade || w2.hr_class =? AND ");
			stb.append("w1.year=w2.year AND w1.semester=w2.semester AND w1.schregno=w2.schregno ");
			stb.append("GROUP BY w1.chaircd) t1, ");
			if ("1".equals(_useCurriculumcd)) {
                stb.append("(SELECT chaircd,classcd,school_kind,curriculum_cd,subclasscd FROM chair_dat ");
			} else {
			    stb.append("(SELECT chaircd,subclasscd FROM chair_dat ");
			}
			stb.append("WHERE year='"+param[0]+"' AND semester='"+param[1]+"') t2 ");
//			stb.append("WHERE t1.chaircd=t2.chaircd AND substr(t2.subclasscd,1,2)<'90' ORDER BY t2.subclasscd ");//2004/08/17
//			stb.append("WHERE t1.chaircd=t2.chaircd AND substr(t2.subclasscd,1,2) <= '90' ORDER BY ");//2006/05/17
            stb.append("WHERE t1.chaircd=t2.chaircd AND substr(t2.subclasscd,1,2) <= ? ORDER BY ");
			if ("1".equals(_useCurriculumcd)) {
                stb.append(" t2.classcd, t2.school_kind, t2.curriculum_cd, t2.subclasscd ");
			} else {
			    stb.append(" t2.subclasscd ");
			}
		} catch( Exception e ){
			log.warn("Pre_Stat1 error!", e);
		}
		return stb.toString();

	}//Pre_Stat1()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT hr_nameabbv,staffname FROM ");
			stb.append("(SELECT hr_nameabbv,tr_cd1 FROM schreg_regd_hdat ");
			stb.append("WHERE year='"+param[0]+"' AND semester='"+param[1]+"' AND grade || hr_class =? ");
			stb.append(") w1 left join staff_mst w2 on w1.tr_cd1=w2.staffcd ");
		} catch( Exception e ){
			log.warn("Pre_Stat2 error!", e);
		}
		return stb.toString();

	}//Pre_Stat2()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat3(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT value(grade,'') grade, value(coursecd,'') coursecd, ");
			stb.append("value(majorcd,'') majorcd, value(coursecode,'') coursecode, min(attendno) ");
			stb.append("FROM schreg_regd_dat ");
			stb.append("WHERE year='"+param[0]+"' AND semester='"+param[1]+"' AND grade || hr_class =? ");
			stb.append("GROUP BY grade,coursecd,majorcd,coursecode ");
		} catch( Exception e ){
			log.warn("Pre_Stat3 error!", e);
		}
		return stb.toString();

	}//Pre_Stat3()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat4(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT staffname FROM ");
			stb.append("(SELECT chaircd,min(staffcd) staffcd FROM chair_stf_dat ");
			stb.append("WHERE year='"+param[0]+"' AND semester='"+param[1]+"' AND int(chargediv)=1 AND chaircd=? ");
			stb.append("GROUP BY chaircd) w1,staff_mst w2 WHERE w1.staffcd=w2.staffcd ");
		} catch( Exception e ){
			log.warn("Pre_Stat4 error!", e);
		}
		return stb.toString();

	}//Pre_Stat4()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat5(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT credits FROM credit_mst ");
			stb.append("WHERE year='"+param[0]+"' AND coursecd=? AND majorcd=? AND ");
			stb.append("grade=? AND coursecode=? AND classcd=? AND ");
			if ("1".equals(_useCurriculumcd)) {
                stb.append("  school_kind=? AND ");
                stb.append("  curriculum_cd=? AND ");
			}
			stb.append(" subclasscd=? ");
		} catch( Exception e ){
			log.warn("Pre_Stat5 error!", e);
		}
		return stb.toString();

	}//Pre_Stat5()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat6(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT subclassabbv FROM subclass_mst ");
			stb.append("WHERE ");
			if ("1".equals(_useCurriculumcd)) {
	            stb.append("  classcd=? and ");
	            stb.append("  school_kind=? and ");
	            stb.append("  curriculum_cd=? and ");
			}
			stb.append("  subclasscd=? ");
		} catch( Exception e ){
			log.warn("Pre_Stat6 error!", e);
		}
		return stb.toString();

	}//Pre_Stat6()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat7(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT DISTINCT w2.schregno,int(w2.attendno) attendno,w3.name ");
			stb.append("FROM chair_std_dat w1,schreg_regd_dat w2,schreg_base_mst w3 ");
			stb.append("WHERE w2.year='"+param[0]+"' AND w2.semester='"+param[1]+"' AND ");
			stb.append("w2.grade || w2.hr_class =? AND w1.year=w2.year AND w1.semester=w2.semester AND ");
			stb.append("w1.chaircd=? AND w1.schregno=w2.schregno AND w3.schregno=w2.schregno ");
			stb.append("ORDER BY attendno ");
		} catch( Exception e ){
			log.warn("Pre_Stat7 error!", e);
		}
		return stb.toString();

	}//Pre_Stat7()の括り



	/**PrepareStatement close**/
	private void Pre_Stat_f(PreparedStatement ps1,PreparedStatement ps2,PreparedStatement ps3,
							PreparedStatement ps4,PreparedStatement ps5,PreparedStatement ps6,PreparedStatement ps7)
	{
		try {
			ps1.close();
			ps2.close();
			ps3.close();
			ps4.close();
			ps5.close();
			ps6.close();
			ps7.close();
		} catch( Exception e ){
			log.warn("Pre_Stat_f error!", e);
		}
	}//Pre_Stat_f()の括り



	/**SVF-FORM-FIELD-INZ**/
	private void Svf_Int(Vrw32alp svf,String param[]){

		svf.VrsOut("NENDO" 		, param[12] );
		svf.VrsOut("SUBCLASS" 	, param[11] );
		svf.VrsOut("CREDIT" 		, param[10] );
		svf.VrsOut("STAFFNAME2" 	, param[9] );
		svf.VrsOut("STAFFNAME1" 	, param[8] );
		svf.VrsOut("HR_NAME" 		, param[7] );

	}//Svf_Int()の括り



}//クラスの括り
