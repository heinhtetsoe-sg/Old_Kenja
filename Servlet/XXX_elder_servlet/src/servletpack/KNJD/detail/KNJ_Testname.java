// kanji=漢字
/*
 * $Id: bf6f7f22a995d829ece73af5b2f3cbc250302210 $
 *
 * 作成日: 2004/10/30
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
/**
 *
 *  テスト名称取得
 *
 *  2004/10/30 yamashiro・KNJ_Testname.TestName(DB2UDB,String,String,String)を追加
 *  2005/06/21 yamashiro Modify 共通化
 */

package servletpack.KNJD.detail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJ_Testname{
    
    private static Log log = LogFactory.getLog(KNJ_Testname.class);

    /** テストテーブルNEW */
    private static final String NEW_TABLE = "TESTITEM_MST_COUNTFLG_NEW";

	/**
	 *  テスト項目名称を設定
	 */
	public ReturnVal TestName(DB2UDB db2,String itemcd){

		String itemname = null;			//テスト項目名称

		try {
			String sql = null;
			sql = "SELECT "
					+ "TESTITEMNAME "
				+ "FROM "
					+ "TESTITEM_MST W1 "
				+ "WHERE "
				 	+ "W1.TESTKINDCD || W1.TESTITEMCD = '" + itemcd + "'";
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			if( rs.next() )
				itemname = rs.getString("TESTITEMNAME");
			rs.close();
		} catch( Exception e ){
			System.out.println("[KNJ_Testname]TestITEMname error!");
			System.out.println( e );
		}

		return (new ReturnVal(itemname,null,null));
	}


	/**
	 *  テスト種別名称を設定
	 */
	public ReturnVal TestNameK(DB2UDB db2,String kindcd){

		String kindname = null;			//テスト種別名称

		try {
			String sql = null;
			sql = "SELECT "
					+ "TESTKINDNAME "
				+ "FROM "
					+ "TESTKIND_MST W1 "
				+ "WHERE "
				 	+ "W1.TESTKINDCD = '" + kindcd + "'";
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			if( rs.next() )
				kindname = rs.getString("TESTKINDNAME");
			rs.close();
		} catch( Exception e ){
			log.error("[KNJ_Testname]TestKINDName error!", e );
		}

		return (new ReturnVal(kindname,null,null));
	}



	/**
	 *  テスト種別名称・項目名称・科目名称を設定
	 */
	public ReturnVal TestName(DB2UDB db2,String kindcd,String itemcd,String subclasscd,String year, String useCurriculumcd){

		String kindname = null;			//テスト種別名称
		String itemname = null;			//テスト項目名称
		String subclassname = null;		//科目名称
        String rssubclasscd = null;     //科目名称

		try {
			String sql = null;
			sql = "SELECT "
					+ "TESTKINDNAME,"
					+ "TESTITEMNAME,"
                    + ("1".equals(useCurriculumcd) ? " W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || " : "") 
                    + " W2.SUBCLASSCD AS SUBCLASSCD "
				+ "FROM "
					+ "TESTKIND_MST W1 "
					+ "INNER JOIN TESTITEM_MST W2 ON W2.TESTKINDCD = W1.TESTKINDCD "
					+ "INNER JOIN SUBCLASS_MST W3 ON "
                    + ("1".equals(useCurriculumcd) ? " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || " : "") 
					+ " W3.SUBCLASSCD = "
                    + ("1".equals(useCurriculumcd) ? " W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || " : "") 
					+ " W2.SUBCLASSCD "
				+ "WHERE "
				 		+ "W1.TESTKINDCD = '" + kindcd + "' "
					+ "AND W2.TESTITEMCD = '" + itemcd + "' "
					+ "AND W2.YEAR = '" + year + "' "
                    + "AND "
                    + ("1".equals(useCurriculumcd) ? " W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || " : "") 
					+ " W2.SUBCLASSCD = '" + subclasscd + "'";
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			if( rs.next() ){
				kindname = rs.getString("TESTKINDNAME");
				itemname = rs.getString("TESTITEMNAME");
				rssubclasscd = rs.getString("SUBCLASSCD");
			}
			rs.close();
		} catch( Exception e ){
			log.error("[KNJ_Testname]TestName error!", e );
		}
		
		if (null != rssubclasscd) {
		    subclassname = getSubclassname(db2, rssubclasscd, useCurriculumcd);
		}

		return (new ReturnVal(kindname,itemname,subclassname));
	}


	/**
	 *  テスト種別名称(固定)・科目名称を設定
	 */
	public ReturnVal TestName(DB2UDB db2,String kindcd,String subclasscd,String year, String useCurriculumcd){      // 04/10/30Add

		String kindname = null;			//テスト種別名称
		String itemname = "";			//テスト項目名称
		String subclassname = null;		//科目名称

        subclassname = getSubclassname(db2, subclasscd, useCurriculumcd);

        kindname = ( kindcd != null )? ( kindcd.equals("01") )? "中間テスト" : ( kindcd.equals("02") )? "期末テスト" : "" : "";
        if( subclassname == null ) subclassname = "";

		return (new ReturnVal(kindname,itemname,subclassname));
	}


    /**
     *  テスト種別名称(TESTITEM_MST_COUNTFLG)
     */
    public ReturnVal getTestNameCountFlg(
            final DB2UDB db2,
            final String kindcd,
            final String itemCd,
            final String subclasscd,
            final String year,
            final String useCurriculumcd
    ) {

        String kindname = "";         //テスト種別名称
        String subclassname = "";     //科目名称

        try {
            String testNameSql = "SELECT "
                        +"  TESTITEMNAME "
                        +"FROM "
                        +"  TESTITEM_MST_COUNTFLG "
                        +"WHERE "
                        +"  YEAR = '" + year + "' "
                        +"  AND TESTKINDCD = '" + kindcd + "'"
                        +"  AND TESTITEMCD = '" + itemCd + "'";
            db2.query(testNameSql);
            ResultSet rsTestName = db2.getResultSet();
            if( rsTestName.next() ){
                kindname = rsTestName.getString("TESTITEMNAME");
            }
            rsTestName.close();
            
            subclassname = getSubclassname(db2, subclasscd, useCurriculumcd);

        } catch( Exception e ){
            log.error("[KNJ_Testname]TestName error!", e);
        }

        return (new ReturnVal(kindname, null, subclassname));
    }

    private static String getSubclassname(final DB2UDB db2, final String subclasscd, final String useCurriculumcd) {
        String rtn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT SUBCLASSNAME ");
        stb.append(" FROM SUBCLASS_MST ");
        stb.append(" WHERE ");
        if ("1".equals(useCurriculumcd)) {
            stb.append(" CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("   SUBCLASSCD = '" + subclasscd + "' ");

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            if (rs.next()) {
                rtn = rs.getString("SUBCLASSNAME");
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }

    public static String getTestNameSql(
            final String countFlgTable,
            final String year,
            final String semester,
            final String testKindCd
    ) {
        final StringBuffer stb = new StringBuffer();
        if (NEW_TABLE.equals(countFlgTable)) {
            stb.append(" SELECT ");
            stb.append("    * ");
            stb.append(" FROM ");
            stb.append("    " + countFlgTable + " ");
            stb.append(" WHERE ");
            stb.append("    YEAR = '" + year + "' ");
            stb.append("    AND SEMESTER = '" + semester + "' ");
            stb.append("    AND TESTKINDCD || TESTITEMCD = '" + testKindCd + "' ");
        } else {
            stb.append(" SELECT ");
            stb.append("    * ");
            stb.append(" FROM ");
            stb.append("    " + countFlgTable + " ");
            stb.append(" WHERE ");
            stb.append("    YEAR = '" + year + "' ");
            stb.append("    AND TESTKINDCD || TESTITEMCD = '" + testKindCd + "' ");
        }
        return stb.toString();
    }

/**
 *  return値を返す内部クラス
 */
	public static class ReturnVal{

		public final String val1,val2,val3;

		public ReturnVal(String val1,String val2,String val3){
			this.val1 = val1;
			this.val2 = val2;
			this.val3 = val3;
		}
	}


}
