package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_ClassCodeImp;
import servletpack.KNJZ.detail.KNJ_EditDate;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [出欠管理]
 *
 *					＜ＫＮＪＣ０５１＞  出席簿（科目別）
 *
 *	2004/07/07・新様式としてKNJC050とは別途作成
 *	2004/09/14 yamashiro・SQL文の不具合を修正->ペナルティ欠課選択の場合SQLの不具合が起きる
 *	2004/09/28 yamashiro・単位がnullの場合の不具合を修正
 *						・校時名称が'SHR'の場合は校時を出力しない-->10/06:数字以外は出力しないへ変更
 *	2004/09/29 yamashiro・SHRやLHRの場合総ページ数が０になる不具合を修正
 *						・遅刻コードに'7'を追加
 *	2004/10/06 yamashiro・出欠データ出力において時間割データにないデータは出力しない
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJC052K extends KNJC051K {

    private static final Log log = LogFactory.getLog(KNJC052K.class);

	PreparedStatement ps11,ps12;
    private static int count;

	/* svf print 印刷処理 */
    void printSvf(DB2UDB db2, Vrw32alp svf)
	{
		ccimp = new KNJ_ClassCodeImp(db2);						//教科コード・ページ行数・列数等定数設定のインスタンス
        setPrepareStatement(db2);
		//SQL作成
		try {
			ps11 = db2.prepareStatement(Pre_Stat_6());		//全講座取得
			ps12 = db2.prepareStatement(Pre_Stat_7());		//講座別日付範囲取得
		} catch (Exception ex) {
			log.error(" error!", ex);
		}

		ResultSet rs11 = null;
		try {
			rs11 = ps11.executeQuery();
            while (rs11.next()) {
                log.debug("CHAIRCD="+rs11.getString("CHAIRCD")+"  count="+ ++count );
                setScheduleValues(db2, rs11);
		        Set_Head(db2,svf);	                                //見出し出力のメソッド
		        //for( int i=0 ; i<param.length ; i++ )log.debug("param["+i+"]="+param[i]);
		        Set_Detail_1(db2,svf);			//出力処理のメソッド
            }
		} catch( Exception ex ){
			log.warn("error!",ex);
		} finally{
			db2.commit();
			DbUtils.closeQuietly(rs11);
		}
        Pre_Stat_f();
        Pre_Stat_f2();

    }


	/* svf print 印刷処理 */
    void setScheduleValues(DB2UDB db2,ResultSet rs11)
	{
		ResultSet rs12 = null;
		try {
			ps12.setString(1, rs11.getString("CHAIRCD"));	//講座コード
			rs12 = ps12.executeQuery();
            if (rs12.next()) {
                param[2] = rs11.getString("CHAIRCD");        //受講コード
                param[3] = rs11.getString("SUBCLASSCD").substring(0,2);   	//教科コード
                param[4] = rs11.getString("SUBCLASSCD");   	//科目コード
                param[5] = rs11.getString("GROUPCD");   	    //群コード
                //param[13] = rs12.getString("DATE1");	        //印刷範囲開始
                //param[14] = rs12.getString("DATE2");	        //印刷範囲終了
            }
		} catch (Exception ex) {
			log.warn("error!",ex);
		} finally{
			db2.commit();
			DbUtils.closeQuietly(rs12);
		}
    }


	/**PrepareStatement作成**/
	String Pre_Stat_6() {

	//	全講座取得
		StringBuffer stb = new StringBuffer();
		try {
            stb.append("SELECT W1.CHAIRCD,GROUPCD,SUBCLASSCD ");
            stb.append("FROM   CHAIR_DAT W1,CHAIR_STF_DAT W2 ");
            stb.append("WHERE  W1.YEAR='"+param[0]+"' AND W1.SEMESTER='"+param[1]+"' AND ");
            stb.append(       "W2.YEAR='"+param[0]+"' AND W2.SEMESTER='"+param[1]+"' AND ");
            stb.append(       "W1.CHAIRCD=W2.CHAIRCD ");
            //stb.append(       "AND substr(SUBCLASSCD,1,2)='05' ");
            stb.append("GROUP BY W2.STAFFCD,W1.CHAIRCD,GROUPCD,SUBCLASSCD ");
            stb.append("ORDER BY W2.STAFFCD,W1.CHAIRCD");
		} catch (Exception ex) {
			log.error(" error!", ex);
		}
		return stb.toString();

	}//Pre_Stat_6()の括り


	/**PrepareStatement作成**/
	String Pre_Stat_7() {

	//	講座別日付範囲取得
		StringBuffer stb = new StringBuffer();
		try {
            stb.append("SELECT CHAIRCD,MIN(EXECUTEDATE)AS DATE1,MAX(EXECUTEDATE)AS DATE2 ");
            stb.append("FROM   SCH_CHR_DAT W1 ");
            stb.append("WHERE  W1.YEAR='"+param[0]+"' AND W1.SEMESTER='"+param[1]+"' AND W1.CHAIRCD=? ");
            stb.append("GROUP BY CHAIRCD");
		} catch (Exception ex) {
			log.error(" error!", ex);
		}
		return stb.toString();

	}//Pre_Stat_7()の括り


	/* get parameter doGet()パラメータ受け取り */
    void getParam(HttpServletRequest request) {
	    param = new String[18];
		try {
	        param[0] = request.getParameter("YEAR");         	//年度
			param[1] = request.getParameter("GAKKI");   		//学期
			if( request.getParameter("OUTPUT3")!=null )param[15] = "on";		//遅刻を欠課に換算 null:無
		//	日付型を変換
			KNJ_EditDate editdate = new KNJ_EditDate();							//クラスのインスタンス作成
			param[13] = editdate.h_format_sec(request.getParameter("DATE"));	//印刷範囲開始
			param[14] = editdate.h_format_sec(request.getParameter("DATE2"));	//印刷範囲終了
            editdate = null;
		} catch (Exception ex) {
			log.error("get parameter error!", ex);
		}
		for( int i=0 ; i<param.length ; i++ )log.debug("KNJC051K  param["+i+"]="+param[i]);
    }


	/**PrepareStatement close**/
	void Pre_Stat_f2()
	{
		try {
			if( ps11 != null )ps11.close();
			if( ps12 != null )ps12.close();
		} catch( Exception ex ){
			log.warn("Preparedstatement-close error!", ex);
		}
	}//Pre_Stat_f2の括り


}//クラスの括り
