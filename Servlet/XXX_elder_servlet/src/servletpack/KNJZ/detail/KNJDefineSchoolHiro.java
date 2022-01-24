/**
 *
 *  広島用学校値設定
 *    2005/06/27 yamashiro
 *      使用する際は 'KNJDefineCode.class'において実行
 *    2006/03/01 yamashiro 総ページ数算出の不具合のためsvfline2を変更
 *    2006/06/13 yamashiro SCHOOL_MSTのABSENT_COVが0以外でABSENT_COV_LATEがNULLまたは0の場合の不具合を修正
 */
package servletpack.KNJZ.detail;

import java.sql.ResultSet;
import java.sql.SQLException;
import nao_package.db.DB2UDB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJDefineSchoolHiro extends KNJDefineSchool implements KNJDefineSchool.DefineSchoolHolder {

    private static final Log log = LogFactory.getLog(KNJDefineSchoolHiro.class);

//	//public int school_Distinc;			//学校による設定区分 0:kenja(hiroshima) 1:kindai  04/07/28ADD
//	public String schoolmark = "HIRO";
//	public int svfline = 50;				//帳票のページ当り行数
//	public int svfline2 = 64;				//科目別出席簿の列数
//	//2006/03/01 public int svfline2 = 63;				//科目別出席簿の列数
//
//    public boolean usechairname;        	//科目名の代わりに講座名称を出力 04/12/31
//	public String schooldiv;				//学校区分 => 単位制/学年制
//	public int semesdiv;					//学期区分
//	public int absent_cov;					//欠課数換算       05/03/27
//	public int absent_cov_late;				//欠課数換算遅刻数 05/03/27
//
//	public boolean useschchrcountflg = true;	//時間割講座データ集計フラグを使用する
//	public boolean usefromtoperiod = true;		//課程マスタの開始終了校時を使用する
//	public boolean useabsencehigh = true;		//単位マスタの欠時数上限値を使用する
//
//	public int absencehighgrade[][] = null;

    private KNJDefineSchool _parent;

    private ResultSet rs;

	public KNJDefineSchoolHiro() {
        super(null);

        schoolmark = "HIRO";
        svfline = 50;
        svfline2 = 64;
        useschchrcountflg = true;
        usefromtoperiod = true;
        useabsencehigh = true;
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
     */
	public void setSchoolCode( DB2UDB db2, String year )
	{
		try{
			String sql = "SELECT  SEMESTERDIV, SCHOOLDIV, ABSENT_COV, ABSENT_COV_LATE FROM SCHOOL_MST WHERE YEAR = '" + year + "'";
			db2.query(sql);
			rs = db2.getResultSet();
			if( rs.next() ){
				_parent.schooldiv = schooldiv = rs.getString("SCHOOLDIV");
				if( rs.getString("SEMESTERDIV") != null ) _parent.semesdiv = semesdiv = Integer.parseInt( rs.getString("SEMESTERDIV") );
				if( rs.getString("ABSENT_COV") != null ) _parent.absent_cov = absent_cov = Integer.parseInt( rs.getString("ABSENT_COV") );
				if( rs.getString("ABSENT_COV_LATE") != null ) _parent.absent_cov_late = absent_cov_late = Integer.parseInt( rs.getString("ABSENT_COV_LATE") );
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
			if( semesdiv        == 0 ) _parent.semesdiv = semesdiv        = 2;
			if( schooldiv       == null ) _parent.schooldiv = schooldiv    = "1";
			//if( absent_cov      == 0 )absent_cov      = 0;
//			if( absent_cov_late == 0 )absent_cov_late = 1;
            if (0 == absent_cov_late) {   //06/06/13
                log.warn("SCHOOL_MSTのABSENT_COVが0以外でABSENT_COV_LATEがNULLまたは0です。");
                _parent.absent_cov = absent_cov = 0;
            }
		}
	}


}
