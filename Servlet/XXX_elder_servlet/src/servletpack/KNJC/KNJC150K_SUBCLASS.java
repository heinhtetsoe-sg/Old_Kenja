/**
 *
 *	学校教育システム 賢者 [出欠管理] 欠席・欠課の要注意者リスト
 *
 *	2005/03/05 yamashiro
 *
 */

package servletpack.KNJC;

import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJC150K_SUBCLASS extends KNJC140K_SUBCLASS {

    private static final Log log = LogFactory.getLog(KNJC150K_SUBCLASS.class);


    /**
     *  コンストラクター
     */
    KNJC150K_SUBCLASS( DB2UDB db2, Vrw32alp svf, String param[], String pselect[] ){
        super(db2, svf, param, pselect);
    }


	/** 
     *  タイトル出力
     *	2005/03/05
     */
	String getPaperTitle()
	{
		return "欠課時数要注意者リスト";
	}


	/** 
     *  PrepareStatement作成-->欠課時数超過者の条件部分
     *     2005/03/05
     */
	String prestatementCondition(){

		return

		"INT(W4.CREDITS*(CASE WHEN W2.GRADE='03' THEN 8 ELSE 10 END) * " + param[1] 
		+ "/(CASE WHEN W2.GRADE='03' THEN 2 ELSE 3 END) + 1 ) <= VALUE(W1.ABSENT_CNT,0)";

	}


}//クラスの括り
