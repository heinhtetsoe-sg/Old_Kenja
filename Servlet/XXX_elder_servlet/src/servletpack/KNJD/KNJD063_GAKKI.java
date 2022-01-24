// kanji=漢字
/**
 * $Id: 083e478f76d74ff8075051c4646224e9bf2feb2b $
 *    学校教育システム 賢者 [成績管理] 成績一覧
 *
 */

package servletpack.KNJD;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJD063_GAKKI extends KNJD063_INTER {

    private static final Log log = LogFactory.getLog(KNJD063_GAKKI.class);


    KNJD063_GAKKI(DB2UDB db2, Vrw32alp svf, String param[]){
        super(db2, svf, param);
    }


    /**
     *  ページ見出し
     *  2005/08/22
     */
    void set_head2()
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrsOut("TITLE" , param[12] + "成績一覧表");     //タイトル

        for( int i = 0 ; i < 19 ; i++ ){
            ret = svf.VrsOutn("ITEM1",  i + 1,   "評価" );
            ret = svf.VrsOutn("ITEM2",  i + 1,   "組順位" );
            ret = svf.VrsOutn("ITEM3",  i + 1,   "学年順位" );
        }

        fieldname = "SEM" + param[1] + "_VALUE";
        if( param[13] == null )param[13] = param[1];
    }


}
