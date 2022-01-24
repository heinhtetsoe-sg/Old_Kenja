/**
 *
 *  近大付属用学校値設定
 *    2005/04/26 yamashiro
 *      使用する際は 'KNJDefineCode.class'において実行
 *    2006/03/01 yamashiro 総ページ数算出の不具合のためsvfline2を変更
 *
 */
package servletpack.KNJZ.detail;

import java.sql.ResultSet;
import java.sql.SQLException;
import nao_package.db.DB2UDB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJDefineSchoolKindai extends KNJDefineSchool implements KNJDefineSchool.DefineSchoolHolder {

    private static final Log log = LogFactory.getLog(KNJDefineSchoolKindai.class);

//	//public int school_Distinc = 1;		//学校による設定区分 0:kenja(hiroshima) 1:kindai  04/07/28ADD
//	public String schoolmark = "KIN";
//	public int svfline = 50;				//帳票のページ当り行数
//	public int svfline2 = 64;				//科目別出席簿の列数
//	//06/03/01 public int svfline2 = 47;				//科目別出席簿の列数
//
//    public boolean usechairname;        	//科目名の代わりに講座名称を出力 04/12/31
//	public String schooldiv;				//学校区分 => 単位制/学年制
//	public int semesdiv;					//学期区分
//	public int absent_cov = 1;				//欠課数換算を学期ごとで行う
//	public int absent_cov_late = 3;			//欠課数換算の遅刻・早退基数を３回とする
//
//	public boolean useschchrcountflg = true;	//時間割講座データ集計フラグを使用する
//	public boolean usefromtoperiod;				//課程マスタの開始終了校時を使用する
//	public boolean useabsencehigh;				//単位マスタの欠時数上限値を使用する
//	public int absencehighgrade[][] = { {10,3},{10,3},{8,2} };
											//欠時数{上限値,学期}を設定{ {1年},{2年},{3年} }

    private KNJDefineSchool _parent;

    private ResultSet rs;

    public KNJDefineSchoolKindai() {
        super(null);

        schoolmark = "KIN";
        svfline = 50;
        svfline2 = 64;
        absent_cov = 1;
        absent_cov_late = 3;
        useschchrcountflg = true;
        absencehighgrade = new int[][] { {10,3},{10,3},{8,2} };
    }

    /**
     * {@inheritDoc}
     */
    public void setParent(final KNJDefineSchool kds) {
        _parent = kds;
    }

    /**
     *  学校区分および科目表示の設定値を取得
     *      NAME_MST  NAMECD1 = 'Z010' AND NAMECD2 = '00' => 学校
     *      NAME_YDAT NAMECD1 = 'C000' AND NAMECD2 = '2'  => 講座名表示   2004/12/31 2005/01/07Modify
     */
	public void Get_ClassCode( DB2UDB db2 ){

		StringBuffer stb = new StringBuffer();
		String bdiv = null;
        if (false && null != bdiv) { bdiv = null; }
		try {
			stb.append("SELECT  NAMECD1, NAMECD2, NAME1 ");
            stb.append("FROM    NAME_MST ");
            stb.append("WHERE   NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
			stb.append("UNION ");
			stb.append("SELECT  NAMECD1, NAMECD2, '' AS NAME1 ");
            stb.append("FROM    NAME_YDAT ");
            stb.append("WHERE   NAMECD1 = 'C000' AND NAMECD2 = '2' ");
		} catch( Exception e ){
			log.error("Get_ClassCode(DB2UDB) sqlstatement error! ", e );
		}
		try {
			db2.query(stb.toString());
			rs = db2.getResultSet();
			while ( rs.next() ){
                if( rs.getString("NAMECD1").equals("Z010") )
                    bdiv = rs.getString("NAME1");
                else if( rs.getString("NAMECD1").equals("C000") )
                    _parent.usechairname = usechairname = true;
            }
			db2.commit();
		} catch( Exception e ){
			log.error("Get_ClassCode(DB2UDB) ResultSet error! ", e );
		}

	}

    /**
     *  学校設定値を取得
     *    2005/05/12 Modify SCHOOLNAME1から高／中を区分
     */
	public void setSchoolCode( DB2UDB db2, String year )
	{
		try{
			String sql = "SELECT  SEMESTERDIV, SCHOOLDIV, SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "'";
			db2.query(sql);
			rs = db2.getResultSet();
			if( rs.next() ){
				_parent.schooldiv = schooldiv = rs.getString("SCHOOLDIV");
				if( rs.getString("SEMESTERDIV") != null ) _parent.semesdiv = semesdiv = Integer.parseInt( rs.getString("SEMESTERDIV") );
				if( rs.getString("SCHOOLNAME1") != null  &&  
					0 <= rs.getString("SCHOOLNAME1").indexOf("中学") ) _parent.schoolmark = schoolmark = "KINJUNIOR";  //05/05/12
			}
		} catch( Exception ex ){
			log.error("setSchoolCode error! ", ex );
		} finally {
			try {
				if( rs != null )rs.close();
                db2.commit();
			} catch( SQLException ex ) {
				log.warn("ResultSet-close error!",ex);
			}
			if( semesdiv == 0 )     _parent.semesdiv = semesdiv  = 3;		//３学期制に設定する
			if( schooldiv == null ) _parent.schooldiv = schooldiv = "0";	//学校区分を学年制に設定する
		}
	}


}
