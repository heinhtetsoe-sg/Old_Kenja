/**
 *
 *	学校教育システム 賢者 [入試処理]  本選考資料
 *
 *	2004/12/20
 *  2005/01/05 科目コードを社会:3 理科:4に変更
 *             入試区分の履歴の表記において、受付データの合否区分=1の時は◎を出力、を追加
 *             結果表の集計仕様を変更
 *  2005/01/08 出願コース名は出力しない
 *             入試制度名は出力しない
 *  2005/01/12 db2.commit()を随所に入れる
 *
 */

package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_EditDate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJL321BASE {

    private static final Log log = LogFactory.getLog(KNJL321BASE.class);

	Vrw32alp svf; 	                //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB db2;						//Databaseクラスを継承したクラス
	String param[];
	boolean nonedata;				//該当データなしフラグ
	PreparedStatement ps1,ps2,ps3,ps4,psTestDiv;
    DecimalFormat df = new DecimalFormat("0.0");   // 05/01/10Modify
    private StringBuffer stb;;
	private ResultSet rs;
    int totalnum[];
    int fsAreaRuikei[];


    /**
      *  コンストラクター
      *
      **/
    KNJL321BASE(DB2UDB db2, Vrw32alp svf, String param[]){
        this.db2 = db2;
        this.svf = svf;
        this.param = param;
    }


	/**
     *  svf print 印刷処理 
     */
    void printSvf()	{

    }


    /**
     *  svf print 印刷処理
     */
    void printSvfMain() {
    }


	/**
     *  svf print 最終人数出力
     */
    void printTotalNum()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append( "男" ).append( totalnum[0] ).append( "名, " );
            stb.append( "女" ).append( totalnum[1] ).append( "名, " );
            stb.append( "合計" ).append( totalnum[2] ).append( "名" );
	 		svf.VrsOut( "NOTE",  stb.toString() );
		} catch( Exception ex ){
			log.error("printTotalNum error!",ex);
		}
	}


	/**
     *  svf print 見出項目取得
     */
    void getHead()
	{
        ResultSet rs = null;

        try {
 			param[5] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception ex ){
            log.error("printSvfMeiboHead error!",ex);
        }

	    //作成日(現在処理日)
		try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();		//各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;		//各情報を返すためのクラス
			returnval = getinfo.Control(db2);
			param[6] = KNJ_EditDate.h_format_JP(returnval.val3);
		} catch( Exception ex ){
			log.warn("ymd1 svf-out error!",ex);
		}

        //入試区分
		try{
			ps4.setString( 1, param[0] );
			ps4.setString( 2, param[2] );
			rs = ps4.executeQuery();
			if ( rs.next()  &&  rs.getString(1) != null )
 			    param[10] = rs.getString(1);
        } catch( Exception ex ){
            log.error("getTestdiv error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
        }
	}


	/**
     *  preparedstatement 入試区分マスターから名称を取得
     */
	String statementMeishou()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT NAME AS NAME1 FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = ? AND TESTDIV = ? ");
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


}//クラスの括り
