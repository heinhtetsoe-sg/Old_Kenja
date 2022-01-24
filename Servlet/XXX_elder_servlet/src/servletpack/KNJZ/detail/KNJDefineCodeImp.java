/**
 *
 *  学校値設定
 *    2005/04/26 yamashiro
 *      各学校用の学校値設定クラスにおいて実装
 *      使用する際は 'KNJDefineCode.class'において実行
 *
 */

package servletpack.KNJZ.detail;

import nao_package.db.DB2UDB;

interface KNJDefineCodeImp{

	String subject_D = "01";			//教科コード
	String subject_U = "89";			//教科コード
	String subject_T = "90";			//総合的な学習の時間
	String subject_E = "93";			//行事
	String subject_S = "92";			//ＳＨＲ
	String subject_S_A = "920100";		//午前ＳＨＲ
	String subject_L = "94";			//ＬＨＲ

	void Get_ClassCode( DB2UDB db2 );

	void setSchoolCode( DB2UDB db2, String year );

}
