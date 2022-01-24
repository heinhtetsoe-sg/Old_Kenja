// kanji=漢字
/**
 * $Id: faf57e89b5a27ef4ca11780972aa5d89eb25cb5d $
 *    学校教育システム 賢者 [成績管理] 成績一覧
 *
 */

package servletpack.KNJD;

import java.util.Map;

import java.sql.ResultSet;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


class KNJD063_INTER extends KNJD063_BASE {

    private static final Log log = LogFactory.getLog(KNJD063_INTER.class);


    KNJD063_INTER(DB2UDB db2, Vrw32alp svf, String param[]){
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
        ret = svf.VrsOut("TITLE" , param[12] + "  中間 成績一覧表");    //タイトル

        for( int i = 0 ; i < 19 ; i++ ){
            ret = svf.VrsOutn("ITEM1",  i + 1,   "素点" );
            ret = svf.VrsOutn("ITEM2",  i + 1,   "組順位" );
            ret = svf.VrsOutn("ITEM3",  i + 1,   "学年順位" );
        }

        fieldname  = "SEM" + param[1] + "_INTR_SCORE";
        fieldname2 = "SEM" + param[1] + "_INTR";
        if( param[13] == null )param[13] = param[1];
    }


    /** 
     *  明細印刷
     *  生徒の科目別素点・組順位・学年順位を出力する
     *  2005/08/22
     */
    void printSvfStdDetailOut( ResultSet rs, 
                               Map hmm
                             )
    {
        try {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            //学籍番号（生徒）に対応した行にデータをセットする。
            Integer int1 = (Integer)hmm.get(rs.getString("SCHREGNO"));
            if( int1==null )return;
            int linex = subclasslinecount + 1;

            if( rs.getString("SCORE")   != null )ret = svf.VrsOutn( "rate"        + int1.intValue(),  linex,  rs.getString("SCORE")   );
            if( rs.getString("GR_RANK") != null )ret = svf.VrsOutn( "GRADE_ORDER" + int1.intValue(),  linex,  rs.getString("GR_RANK") );
            if( rs.getString("HR_RANK") != null )ret = svf.VrsOutn( "CLASS_ORDER" + int1.intValue(),  linex,  rs.getString("HR_RANK") );

        } catch( Exception ex ){
            log.warn("attend... svf-out error!",ex);
        }
    }


}
