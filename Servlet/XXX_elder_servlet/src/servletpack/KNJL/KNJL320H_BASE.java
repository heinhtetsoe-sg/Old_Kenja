/**
 *
 *	学校教育システム 賢者 [入試処理]  予備選考資料
 *
 *					＜ＫＮＪＬ３２０Ｈ_ＢＡＳＥ＞  予備選考資料
 *
 *	2007/11/13 RTS 作成日
 *
 *
 */

package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJL320H_BASE {

    private static final Log log = LogFactory.getLog(KNJL320H_BASE.class);

	Vrw32alp svf; 	                //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB db2;						//Databaseクラスを継承したクラス
	String param[];
	boolean nonedata;				//該当データなしフラグ
	PreparedStatement ps1,ps2,ps3,ps4,ps5,ps6;
    DecimalFormat df = new DecimalFormat("0.0");
    private StringBuffer stb;;
	private ResultSet rs;
    int totalnum[];


    /**
      *  コンストラクター
      *
      **/
    KNJL320H_BASE(DB2UDB db2, Vrw32alp svf, String param[]){
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
    		String sNENDO = convZenkakuToHankaku(param[0]);
 			param[5] = sNENDO + "年度";
        } catch( Exception ex ){
            log.error("printSvfMeiboHead error!",ex);
        }

	    //作成日(現在処理日)
		try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();		//各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;		//各情報を返すためのクラス
			returnval = getinfo.Control(db2);
			param[6] = fomatSakuseiDate(returnval.val3);
		} catch( Exception ex ){
			log.warn("ymd1 svf-out error!",ex);
		}

        //入試区分
		try{
			if (param[1].equals("1")){
				ps4.setString( 1, "L004" );
				ps4.setString( 2, param[2] );
			} else {
				ps4.setString( 1, "L003" );
				ps4.setString( 2, param[1] );
			}
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
     *  preparedstatement 名称マスターから名称を取得
     */
	String statementMeishou()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
			if (param[1].equals("1")){
	            stb.append("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = ? AND NAMECD2 = ? ");
			} else {
	            stb.append("SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = ? AND NAMECD2 = ? ");
			}
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}
	
    /**
     * 半角数字を全角数字に変換する
     * @param s
     * @return
     */
    private String convZenkakuToHankaku(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c >= '0' && c <= '9') {
            sb.setCharAt(i, (char) (c - '0' + 0xff10));
          }
        }
        return sb.toString();
    }

    /**
     * 日付をフォーマットYYYY年MM月DD日に設定する
     * @param s
     * @return
     */
    private String fomatSakuseiDate(String cnvDate) {

    	String retDate = "";
    	try {
			DateFormat foramt = new SimpleDateFormat("yyyy-MM-dd"); 
			//文字列よりDate型へ変換
			Date date1 = foramt.parse(cnvDate); 
			// 年月日のフォーマットを指定
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
			// Date型より文字列へ変換
			retDate = sdf1.format(date1);
		} catch( Exception e ){
			log.error("setHeader set error!");
		}
		return retDate;
    }



}//クラスの括り
