/**
 *
 *	学校教育システム 賢者 [出欠管理] 欠席・欠課の要注意者・超過者リスト
 *
 *	2004/12/16 yamashiro・
 *
 */

package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJSchoolMst;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJC140K_BASE {

    private static final Log log = LogFactory.getLog(KNJC140K_BASE.class);

	Vrw32alp svf; 	                //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB db2;						//Databaseクラスを継承したクラス
	String param[];
    String pselect[];
	boolean nonedata;				//該当データなしフラグ
	PreparedStatement ps1, ps2, ps3;
	int pline;					    //出力行数カウント
	DecimalFormat dmf1 = new DecimalFormat("00");
	KNJSchoolMst knjSchoolMst;

    /**
     *  コンストラクター
     */
    KNJC140K_BASE(DB2UDB db2, Vrw32alp svf, String param[], String pselect[]){
        this.db2 = db2;
        this.svf = svf;
        this.param = param;
        this.pselect = pselect;
        
        loadSchoolMst(db2, param[0]);
    }


	/**
	 *  HTTP Get リクエストの処理
	 */
	void printSvf() {
		setHead();						//見出し出力のメソッド
		printSvfMain();
    }


	/** 
     *  見出し項目等 
     */
	void setHead(){

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		int ret = 0;
        if (false || ret == 0) ret = 0;
		ret = svf.VrSetForm("KNJC140.frm", 4);				//共通フォーム
		ret = svf.VrsOut("NENDO",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");//年度
		ret = svf.VrsOut("PRGID","KNJC140");

	//	ＳＶＦ属性変更--->改ページ
		ret = svf.VrAttribute("GRADE","FF=1");

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			ret = svf.VrsOut("ymd",KNJ_EditDate.h_format_JP(returnval.val3));		//作成日
		} catch( Exception ex ){
            log.error("error! " + ex );
		}
	//	学期名称・期間の取得
		try {
			returnval = getinfo.Semester(db2,param[0],param[1]);
			ret = svf.VrsOut("SEMESTER"	,returnval.val1);	//今学期名称
			param[4] = returnval.val3;						//今学期終了日の保存
		} catch( Exception ex ){
            log.error("error! " + ex );
		}
		try {
			returnval = getinfo.Semester(db2,param[0],"1");
			param[3] = returnval.val2;						//今年度開始日の保存
		} catch( Exception ex ){
            log.error("error! " + ex );
		}
		if( param[3]==null )param[3] = param[0]+"-04-01";
		if( param[4]==null )param[4] = String.valueOf((Integer.parseInt(param[0])+1))+"-03-31";
		getinfo = null;
		returnval = null;

	}//Set_Head()の括り


	/** 
     *  印刷処理 
     */
	void printSvfMain(){ }


	/** 
     *  PrepareStatement作成-->該当学年の学級取得 
	 *    学年別学級の表  ホスト変数-->学年、組
     */
	String prestatementHrclass(){

		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT GRADE,HR_CLASS,HR_NAME ");
			stb.append("FROM   SCHREG_REGD_HDAT W1 ");
			stb.append("WHERE  W1.YEAR = '" + param[0] + "' AND ");
            stb.append(       "W1.SEMESTER = '" + param[1] + "' AND ");
			stb.append(		  "W1.GRADE = ? ");
			stb.append("ORDER BY W1.HR_CLASS ");
		} catch( Exception ex ){
            log.error("error! " + ex );
		}
		return stb.toString();
	}


	/**PrepareStatement close**/
	void prestatementClose(){
		try {
			if( ps1 != null ) ps1.close();
			if( ps2 != null ) ps2.close();
			if( ps3 != null ) ps3.close();
		} catch( Exception ex ){
            log.error("error! " + ex );
		}
	}//prestatementClose()の括り

    /**
     * 学校マスタをロードする
     * @param db2 DB2
     * @param year 年度
     */
    public void loadSchoolMst(DB2UDB db2, String year) {
        try {
            knjSchoolMst = new KNJSchoolMst(db2, year);
        } catch (SQLException ex) {
            log.error("loadSchoolMst exception!", ex);
        }
    }

}//クラスの括り
