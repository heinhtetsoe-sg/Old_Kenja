// kanji=漢字
/**
 * $Id: 7e4d2b684a0cc09140c9fcf0a9b22f9b03402f78 $
 *    学校教育システム 賢者 [成績管理] 成績一覧
 *
 */

package servletpack.KNJD;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJD063_GRADE extends KNJD063_GAKKI {

    private static final Log log = LogFactory.getLog(KNJD063_GRADE.class);


    KNJD063_GRADE(DB2UDB db2, Vrw32alp svf, String param[]){
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
        ret = svf.VrsOut("TITLE",  "  成績一覧表（評定）");

        for( int i = 0 ; i < 19 ; i++ ){
            ret = svf.VrsOutn("ITEM1",  i + 1,   "評定" );
            ret = svf.VrsOutn("ITEM2",  i + 1,   "組順位" );
            ret = svf.VrsOutn("ITEM3",  i + 1,   "学年順位" );
        }

        fieldname = "GRAD_VALUE";
        if( param[13] == null )param[13] = param[1];
    }


}
