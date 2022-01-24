/**
 *
 *	学校教育システム 賢者 [入試処理]  予備選考資料
 *
 *					＜ＫＮＪＬ３２０Ｈ_ＨＹＯＵＳＩ＞  予備選考資料
 *
 *	2007/11/13 RTS 作成日
 *
 */

package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJL320H_HYOUSI extends KNJL320H_BASE {

    private static final Log log = LogFactory.getLog(KNJL320H_HYOUSI.class);

    private StringBuffer stb;
	private ResultSet rs;
    boolean nonedata = false;
    int outcount;
    int absent_norecept;    //受付データがない欠席者数 ２科目
    //*-------------------------------------------------* 
    // 試験科目データテーブルの対象年度、入試制度に     *
    // 紐つく名称マスタデータ(L009)を格納。             *
    // 構成:キー⇒科目見出しエリア出力順,値=NAMECD2     *
    //*-------------------------------------------------* 
    HashMap hkamoku = new HashMap();


    KNJL320H_HYOUSI(DB2UDB db2, Vrw32alp svf, String param[]){
        super(db2, svf, param);
    }


	/**
     *  svf print 印刷処理 
     */
    void printSvf()	{

        try {
            if( ps4 == null ) ps4 = db2.prepareStatement( statementMeishou() );                 //名称マスター
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
        printSvfMain();	          //統計表・入学試験受験者成績結果表の印刷

        try {
            if( ps1 != null ) ps1.close();
            if( ps2 != null ) ps2.close();
            if( ps3 != null ) ps3.close();
            if( ps4 != null ) ps4.close();
            if( ps5 != null ) ps5.close();
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
    }


	/**
     *  svf print 印刷処理
     */
    void printSvfMain() {

        try {
            getHead();      //見出し項目
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }

        try {
            printSvfKekkahyoHead();        //名簿頁見出し印刷
            printSvfKekkahyo();            //名簿の印刷
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }

    }

	/**
     *  svf print 統計表 見出し出力
     */
    void printSvfKekkahyoHead()
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }

		try{
            ret = svf.VrSetForm("KNJL320H_1.frm", 1);
 			ret = svf.VrsOut("PRGID",          param[9] + "H_1" );
 			ret = svf.VrsOut("NENDO",          param[5] );
 			ret = svf.VrsOut("DATE",           param[6] );
			ret = svf.VrsOut("TESTDIV",        param[10] );
		} catch( Exception ex ){
			log.error("printSvfKekkahyoOut2 error!",ex);
		}

	}

	/**
     *  svf print 統計表の印刷
     */
    void printSvfKekkahyo() {

        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        for( int i=0 ; i<param.length ; i++ )
        	log.debug("param["+i+"]="+param[i]);

		try{
	        //入試科目テーブルより対象年度、対象入試制度の科目を取得する
			String retSql = getSubClass();
		    setSubClass(db2, svf, retSql);
			
	        //*----------------------------*
	        //*  各科目計欄の編集・出力    *
	        //*----------------------------*
			for(int j=0;j<hkamoku.size();j++){
				//『最高点、最低点、平均点、受験者数』を設定
				String sSubClass_Count = setkekkahyo(db2, (String)hkamoku.get(String.valueOf(j+1)), j+1 );
				//志願者データより欠席者数を取得し設定
				if(sSubClass_Count != null){
					setkekkahyo_absent(db2, (String)hkamoku.get(String.valueOf(j+1)), sSubClass_Count, j+1 );
				}
			}
			// 入試制度が高校一般の場合、加算点エリアの出力を行う
			if(param[1].equals("2")){
				int kamoku_cnt = hkamoku.size();
				String sKasan_Count = setkekkahyo_kaasn(db2, kamoku_cnt+1 );
				//志願者データより欠席者数を取得し設定
				if(sKasan_Count != null){
					setKasan_Absent(db2, sKasan_Count, kamoku_cnt+1 );
				}
			}
	        //*----------------------------*
	        //*  各合計欄の編集・出力      *
	        //*----------------------------*
			//『最高点、最低点、平均点、受験者数』を設定
			String sSubClass_Count = setkekkahyoTotal(db2);
			//志願者データより欠席者数を取得
			if(sSubClass_Count != null){
				setkekkahyoTotal_absent(db2, sSubClass_Count);
			}
		} catch( Exception ex ){
			log.error("printSvfKekkahyo error!",ex);
		}

		if( nonedata ){
			ret = svf.VrEndPage();
			ret = svf.VrPrint();
			super.nonedata = true;
		}
    }

	/**
     *  svf print 統計表 統計表各科目エリアデータ出力
     */
    private String setkekkahyo(DB2UDB db2, String sTestSubClassCd, int icol)
	{
    	String retSubClass_Cnt = null;
    	ResultSet rs = null;
		try{
	    	ps1 = db2.prepareStatement( getKekkahyo(sTestSubClassCd) );
			rs = ps1.executeQuery();
			while( rs.next() ){
				//統計表出力
	            svf.VrsOutn( "SUBCLASS" + icol,  1,	nvlT(rs.getString("MAXSCORE")) );		// 最高点
	            svf.VrsOutn( "SUBCLASS" + icol,  2,	nvlT(rs.getString("MINSCORE")) );		// 最低点
	            svf.VrsOutn( "SUBCLASS" + icol,  3,	nvlT(rs.getString("AVERAGE")) );		// 平均点
	            svf.VrsOutn( "SUBCLASS" + icol,  4,	nvlT(rs.getString("SUBCLASS_CNT")) );	// 受験者数
	            retSubClass_Cnt = nvlT(rs.getString("SUBCLASS_CNT"));
	            nonedata = true;
			}
		} catch( Exception ex ){
			log.error("setkekkahyo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
    		db2.commit();
		}
		return retSubClass_Cnt;
	}

	/**
     *  svf print 統計表 統計表各科目エリアデータ出力
     */
    private void setkekkahyo_absent(DB2UDB db2, String sTestSubClassCd, String sSubClass_Count, int icol)
	{
    	ResultSet rs = null;
    	int iAbsent_Cnt = 0;
    	int iDesire_Cnt = 0;
    	int iSubclass_Cnt = 0;
    	//*-----------------------------*
    	//* 『欠席者数』の取得・出力    *
    	//*-----------------------------*
		try{
			
			//志願者データより志願者取得
	    	ps2 = db2.prepareStatement( getKekkahyo_absent() );
			rs = ps2.executeQuery();
            if( rs.next() ){
            	iDesire_Cnt = rs.getInt("DESIRE_CNT");
                // 欠席者数を算出
                iSubclass_Cnt = Integer.valueOf(sSubClass_Count).intValue();
                // 欠席者数を算出する：志願者数?受験者数
                iAbsent_Cnt = iDesire_Cnt - iSubclass_Cnt;
    			//統計表出力
                svf.VrsOutn( "SUBCLASS" + icol,  5,	String.valueOf(iAbsent_Cnt) );	// 欠席者数
            }
			
		} catch( Exception ex ){
			log.error("setkekkahyo_absent error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
       		db2.commit();
		}
	}
    
	/**
     *  svf print 統計表 統計表各科目エリアデータ出力
     */
    private String setkekkahyoTotal(DB2UDB db2)
	{
    	String retTotal_Cnt = null;
    	ResultSet rs = null;
		try{
	    	ps3 = db2.prepareStatement( getKekkahyo_Total() );
			rs = ps3.executeQuery();
			while( rs.next() ){
				//統計表出力
	            svf.VrsOutn( "TOTAL",  1,	nvlT(rs.getString("MAXSCORE")) );		// 最高点
	            svf.VrsOutn( "TOTAL",  2,	nvlT(rs.getString("MINSCORE")) );		// 最低点
	            svf.VrsOutn( "TOTAL",  3,	nvlT(rs.getString("AVERAGE")) );		// 平均点
	            svf.VrsOutn( "TOTAL",  4,	nvlT(rs.getString("TOTAL_CNT")) );		// 受験者数
	            retTotal_Cnt = nvlT(rs.getString("TOTAL_CNT"));
	            nonedata = true;
			}
		} catch( Exception ex ){
			log.error("setkekkahyo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
    		db2.commit();
		}
		return retTotal_Cnt;
	}

	/**
     *  svf print 統計表 統計表各科目エリアデータ出力
     */
    private void setkekkahyoTotal_absent(DB2UDB db2,String sTotal_Count)
	{
    	ResultSet rs = null;
    	int iAbsent_Cnt = 0;
    	int iDesire_Cnt = 0;
    	int iSubclass_Cnt = 0;
    	//*-----------------------------*
    	//* 『欠席者数』の取得・出力    *
    	//*-----------------------------*
		try{
			
			//志願者データより志願者取得
			rs = ps2.executeQuery();
            if( rs.next() ){
            	iDesire_Cnt = rs.getInt("DESIRE_CNT");
                // 欠席者数を算出
                iSubclass_Cnt = Integer.valueOf(sTotal_Count).intValue();
                // 欠席者数を算出する：志願者数?受験者数
                iAbsent_Cnt = iDesire_Cnt - iSubclass_Cnt;
    			//統計表出力
                svf.VrsOutn( "TOTAL",  5,	String.valueOf(iAbsent_Cnt) );	// 欠席者数
            }
			
		} catch( Exception ex ){
			log.error("setkekkahyo_absent error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
       		db2.commit();
		}
	}

	/**
     *  svf print 統計表 統計表加算エリアデータ出力
     */
    private String setkekkahyo_kaasn(DB2UDB db2,int icol)
	{
    	String retKasan_Cnt = null;
    	ResultSet rs = null;
		try{
	    	ps5 = db2.prepareStatement( getKekkahyo_kasan() );
			rs = ps5.executeQuery();
			while( rs.next() ){
				//統計表出力
	            svf.VrsOutn( "SUBCLASS" + icol,  1,	nvlT(rs.getString("MAXSCORE")) );		// 最高点
	            svf.VrsOutn( "SUBCLASS" + icol,  2,	nvlT(rs.getString("MINSCORE")) );		// 最低点
	            svf.VrsOutn( "SUBCLASS" + icol,  3,	nvlT(rs.getString("AVERAGE")) );		// 平均点
	            svf.VrsOutn( "SUBCLASS" + icol,  4,	nvlT(rs.getString("KASAN_CNT")) );		// 受験者数
	            retKasan_Cnt = nvlT(rs.getString("KASAN_CNT"));
	            nonedata = true;
			}
		} catch( Exception ex ){
			log.error("setkekkahyo_kaasn error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
    		db2.commit();
		}
		return retKasan_Cnt;
	}

	/**
     *  svf print 統計表 統計表加算エリアデータ出力
     */
    private void setKasan_Absent(DB2UDB db2, String sKasan_Count, int icol)
	{
    	ResultSet rs = null;
    	int iAbsent_Cnt = 0;
    	int iDesire_Cnt = 0;
    	int iKasan_Cnt = 0;
    	//*-----------------------------*
    	//* 『欠席者数』の取得・出力    *
    	//*-----------------------------*
		try{
			
			//志願者データより志願者取得
	    	ps2 = db2.prepareStatement( getKekkahyo_absent() );
			rs = ps2.executeQuery();
            if( rs.next() ){
            	iDesire_Cnt = rs.getInt("DESIRE_CNT");
                // 欠席者数を算出する：志願者数?受験者数
                iKasan_Cnt = Integer.valueOf(sKasan_Count).intValue();
                iAbsent_Cnt = iDesire_Cnt - iKasan_Cnt;
    			//統計表出力
                svf.VrsOutn( "SUBCLASS" + icol,  5,	String.valueOf(iAbsent_Cnt) );	// 欠席者数
            }
			
		} catch( Exception ex ){
			log.error("setkekkahyo_absent error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
       		db2.commit();
		}
	}

	/**
     *  結果表 各科目欄取得
     *  (最高点・最低点・平均点・受験者数)
     */
    private String getKekkahyo(String sTestSubClassCd)
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT ");
            stb.append("COUNT(*) AS SUBCLASS_CNT, ");
            stb.append("MAX(SCORE) AS MAXSCORE, ");
            stb.append("MIN(SCORE) AS MINSCORE, ");
            stb.append("ROUND(AVG(FLOAT(SCORE))*10,0)/10 AS AVERAGE ");
            //テーブル
            stb.append("FROM  ENTEXAM_SCORE_DAT ");
            //抽出条件
            stb.append("WHERE ");
            stb.append("ENTEXAMYEAR = '" + param[0] + "'  AND ");		// 入試年度
            stb.append("APPLICANTDIV = '" + param[1] + "' AND ");		// 入試制度
            stb.append("TESTDIV = '" + param[2] + "' AND ");			// 入試区分
            stb.append("ATTEND_FLG = '1' AND ");						// 出欠フラグ(1:受験)
            stb.append("TESTSUBCLASSCD = '" + sTestSubClassCd + "'");	// 試験科目コード 

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}
    
	/**
     *  結果表 加算点欄取得
     *  (最高点・最低点・平均点・受験者数)
     */
    private String getKekkahyo_kasan()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT ");
            stb.append("COUNT(*) AS KASAN_CNT, ");
            stb.append("MAX(KASANTEN_ALL) AS MAXSCORE, ");
            stb.append("MIN(KASANTEN_ALL) AS MINSCORE, ");
            stb.append("ROUND(AVG(FLOAT(KASANTEN_ALL))*10,0)/10 AS AVERAGE ");
            //テーブル
            stb.append("FROM  ENTEXAM_RECEPT_DAT W1 ");
            stb.append(      "INNER JOIN ENTEXAM_APPLICANTCONFRPT_DAT W2 ON ");
            stb.append(      "W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(      "W2.EXAMNO = W1.EXAMNO ");
            //抽出条件
            stb.append("WHERE ");
            stb.append("W1.ENTEXAMYEAR = '" + param[0] + "'  AND ");	// 入試年度
            stb.append("W1.APPLICANTDIV = '" + param[1] + "' AND ");	// 入試制度
            stb.append("W1.TESTDIV = '" + param[2] + "' AND ");			// 入試区分
            stb.append("W1.TOTAL4 IS NOT NULL AND ");					// 4科目合計
            stb.append("W2.KASANTEN_ALL IS NOT NULL");					// 加算点

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

	/**
     *  結果表 合計欄取得
     *  (最高点・最低点・平均点・受験者数)
     */
    private String getKekkahyo_Total()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT ");
            stb.append("COUNT(*) AS TOTAL_CNT, ");
            stb.append("MAX(TOTAL4) AS MAXSCORE, ");
            stb.append("MIN(TOTAL4) AS MINSCORE, ");
            stb.append("ROUND(AVG(FLOAT(TOTAL4))*10,0)/10 AS AVERAGE ");
            //テーブル
            stb.append("FROM  ENTEXAM_RECEPT_DAT ");
            //抽出条件
            stb.append("WHERE ");
            stb.append("ENTEXAMYEAR = '" + param[0] + "'  AND ");	// 入試年度
            stb.append("APPLICANTDIV = '" + param[1] + "' AND ");	// 入試制度
            stb.append("TESTDIV = '" + param[2] + "' AND ");		// 入試区分
            stb.append("TOTAL4 IS NOT NULL ");						// 出欠フラグ(1:受験)

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

	/**
     *  結果表 合計欄取得
     *  (欠席数)
     */
    private String getKekkahyo_absent()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT ");
            stb.append("COUNT(*) AS DESIRE_CNT ");
            //テーブル
            stb.append("FROM  ENTEXAM_DESIRE_DAT ");
            //抽出条件
            stb.append("WHERE ");
            stb.append("ENTEXAMYEAR = '" + param[0] + "'  AND ");	// 入試年度
            stb.append("APPLICANTDIV = '" + param[1] + "' AND ");	// 入試制度
            stb.append("TESTDIV = '" + param[2] + "' AND ");		// 入試区分
            stb.append("APPLICANT_DIV = '1' ");						// 志願者区分(1:有り)

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

	/**
     *  入試科目テーブルより、入試年度・入試制度に紐つく
     *  入試科目コードを取得する
     * @return	stb
     */
    private String getSubClass()
	{
    	stb = new StringBuffer();
        
		try{
            stb.append("SELECT  T1.NAMECD2, T1.NAME1 ");
            stb.append(      "FROM    ENTEXAM_TESTSUBCLASSCD_DAT W1 ");
			stb.append(      "    LEFT JOIN NAME_MST T1 ON T1.NAMECD1='L009' ");
			stb.append(      "         AND T1.NAMECD2 = W1.TESTSUBCLASSCD  ");
            stb.append(      "WHERE   W1.ENTEXAMYEAR  = '" + param[0] + "' AND ");
            stb.append(      "        W1.APPLICANTDIV = '" + param[1] + "'");
			stb.append(" ORDER BY  ");
			stb.append("    W1.SHOWORDER ");


		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

	/**
     *  入試科目テーブルより、入試年度・入試制度に紐つく
     *  入試科目コードを取得する
     * @param rs	実行結果オブジェクト
     * @param svf	帳票オブジェクト
     */
    private void setSubClass(DB2UDB db2, Vrw32alp svf, String stb)
	{
		PreparedStatement ps = null;
    	ResultSet rs = null;
		try{
	    	int idx_kamoku = 1;
			// SQL発行
			ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
			while( rs.next() ){
			    svf.VrsOut("SUBCLASS_NAME" + idx_kamoku	,	rs.getString("NAME1"));
				hkamoku.put(String.valueOf(idx_kamoku)	,rs.getString("NAMECD2"));
				++idx_kamoku;
			}
		    // 試験制度が高校(一般)の場合、帳票の試験科目名称の最後に
		    // 『加算』を設定する
		    if(param[1].equals("2")){
		    	svf.VrsOut("SUBCLASS_NAME" + idx_kamoku	, "加算");
		    }

		} catch( Exception ex ){
            log.error("getSUBCLASS error!",ex);
    	} finally {
    		db2.commit();
    		DbUtils.closeQuietly(null, ps, rs);
    	}
	}
	/**
	 * NULL値を""として返す。
	 * <P>
	 * @param val 変換対象文字列
	 * @return 変換後文字列
	 */
	private String nvlT(String val) {

		if (val == null) {
			return "";
		} else {
			return val.trim();
		}
	}

}//クラスの括り
