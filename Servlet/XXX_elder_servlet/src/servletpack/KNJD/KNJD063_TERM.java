// kanji=漢字
/**
 * $Id: 43327db167305d6c1309cc88ac45150025f1e9f2 $
 *    学校教育システム 賢者 [成績管理] 成績一覧
 *
 *  2005/02/10 yamashiro KIN_RECORD_DATの試験成績、学期成績、学年成績の取得仕様を変更
 *  2005/05/30 yamashiro 一括出力対応
 */

package servletpack.KNJD;

import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJD063_TERM extends KNJD063_INTER {

    private static final Log log = LogFactory.getLog(KNJD063_TERM.class);


    KNJD063_TERM(DB2UDB db2, Vrw32alp svf, String param[]){
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
        ret = svf.VrsOut("TITLE" , param[12] + "  期末 成績一覧表");    //タイトル

        for( int i = 0 ; i < 19 ; i++ ){
            ret = svf.VrsOutn("ITEM1",  i + 1,   "素点" );
            ret = svf.VrsOutn("ITEM2",  i + 1,   "組順位" );
            ret = svf.VrsOutn("ITEM3",  i + 1,   "学年順位" );
        }

        fieldname  = "SEM" + param[1] + "_TERM_SCORE";
        fieldname2 = "SEM" + param[1] + "_TERM";
        if( param[13] == null )param[13] = param[1];
    }


}
