/**
 *
 *  学校値設定（各学校共通）
 *    2005/04/26 yamashiro
 *      使用する際は 各学校値設定クラス('KNJDefine*')を継承する
 *
 */
package servletpack.KNJZ.detail;

import nao_package.db.DB2UDB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJDefineCode extends KNJDefineSchool {

    private static final Log log = LogFactory.getLog(KNJDefineCode.class);


    /**
     *  各学校における定数等設定を実行
     *  @deprecated 上位のクラス(KNJDefineSchool)に引っ越しました。<br>
     *  このクラスは将来(KNJDefineCode)、廃止の意向です。
     */
	public void defineCode( DB2UDB db2, String year )
	{
		Get_ClassCode( db2 );			//学校区分および科目表示の設定値を取得
		setSchoolCode( db2, year );		//学校設定値を取得
	}

}
