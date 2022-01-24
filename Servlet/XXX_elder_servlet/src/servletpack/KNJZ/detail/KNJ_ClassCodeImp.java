/**
 *
 *  教科コード等定数設定
 *      2004/12/31 yamashiro  void Get_ClassCode(DB2UDB db2)を変更
 */
package servletpack.KNJZ.detail;

import nao_package.db.*;
import java.sql.*;


public class KNJ_ClassCodeImp implements KNJ_ClassCode,KNJ_ClassCode_2 {

	public int school_Distinc;			//学校による設定区分 0:kenja(hiroshima) 1:kindai  04/07/28ADD

	public String subject_D;			//教科コード
	public String subject_U;			//教科コード
	public String subject_T;			//総合的な学習の時間
	public String subject_E;			//行事
	public String subject_S;			//ＳＨＲ
	public String subject_S_A;			//午前ＳＨＲ
	public String subject_L;			//ＬＨＲ

	public int svfline;					//帳票のページ当り行数
	public int svfline2;				//科目別出席簿の列数

    public boolean usechairname;        //科目名の代わりに講座名称を出力 04/12/31

	public KNJ_ClassCodeImp(DB2UDB db2){
		Get_ClassCode(db2);
	}

    /**
     *  設定値を取得
     *      NAME_MST  NAMECD1 = 'Z010' AND NAMECD2 = '00' => 学校
     *      NAME_YDAT NAMECD1 = 'C000' AND NAMECD2 = '2'  => 講座名表示   2004/12/31 2005/01/07Modify
     */
	private void Get_ClassCode(DB2UDB db2){

		StringBuffer stb = new StringBuffer();
		ResultSet rs;
		String bdiv = null;
		try {
			//stb.append("SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00'");
			stb.append("SELECT  NAMECD1, NAMECD2, NAME1 ");
            stb.append("FROM    NAME_MST ");
            stb.append("WHERE   NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
			stb.append("UNION SELECT  NAMECD1, NAMECD2, '' AS NAME1 ");
            stb.append("FROM    NAME_YDAT ");
            stb.append("WHERE   NAMECD1 = 'C000' AND NAMECD2 = '2' ");
		} catch( Exception e ){
			System.out.println("[KNJ_ClassCodeImp]Get_ClassCode(DB2UDB) sqlstatement error!");
			System.out.println( e );
		}
		try {
			db2.query(stb.toString());
			rs = db2.getResultSet();
			while ( rs.next() ){
                if( rs.getString("NAMECD1").equals("Z010") )
                    bdiv = rs.getString("NAME1");
                else if( rs.getString("NAMECD1").equals("C000") )
                    usechairname = true;
            }
			db2.commit();
		} catch( Exception e ){
			System.out.println("[KNJ_ClassCodeImp]Get_ClassCode(DB2UDB) ResultSet error!");
			System.out.println( e );
		}

		//以下、共通の設定
		this.subject_D = KNJ_ClassCode.subject_D;			//教科コード
		this.subject_U = KNJ_ClassCode.subject_U;			//教科コード
		this.subject_T = KNJ_ClassCode.subject_T;			//総合的な学習の時間
		this.subject_E = KNJ_ClassCode.subject_E;			//行事
		this.subject_S = KNJ_ClassCode.subject_S;			//ＳＨＲ
		this.subject_S_A = KNJ_ClassCode.subject_S_A;		//午前ＳＨＲ
		this.subject_L = KNJ_ClassCode.subject_L;			//ＬＨＲ
		this.svfline = KNJ_ClassCode.svfline;				//帳票のページ当り行数
		this.svfline2 = KNJ_ClassCode.svfline;				//科目別出席簿の列数

		//以下、学校により設定が異なる
		if( bdiv != null && bdiv.equals("kindai") ){
			this.svfline = KNJ_ClassCode_2.svfline;			//帳票のページ当り行数
			this.svfline2 = KNJ_ClassCode_2.svfline2;		//科目別出席簿の列数
			this.school_Distinc = 1;						//学校による設定区分 04/07/28ADD
		}


	}


}//class KNJ_ClassCodeImpの括り
