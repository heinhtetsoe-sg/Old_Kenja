/**
 *
 *	学校教育システム 賢者 [入試処理]  本選考資料４科目成績一覧表
 *
 *					＜ＫＮＪＬ３２０Ｈ_ＫＡＭＯＫＵ４＞  予備選考資料４科目成績一覧表
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


public class KNJL320H_KAMOKU4 extends KNJL320H_BASE {

    private static final Log log = LogFactory.getLog(KNJL320H_KAMOKU4.class);

    private StringBuffer stb;
	private ResultSet rs;
    int pageno;
    int totalpageno;
    int outcount;
    //*-------------------------------------------------* 
    // 試験科目データテーブルの対象年度、入試制度に     *
    // 紐つく名称マスタデータ(L009)を格納。             *
    // 構成:キー⇒科目見出しエリア出力順,値=NAMECD2     *
    //*-------------------------------------------------* 
	HashMap hkamoku = new HashMap();


    KNJL320H_KAMOKU4(DB2UDB db2, Vrw32alp svf, String param[]){
        super(db2, svf, param);
    }


	/**
     *  svf print 印刷処理 
     */
    void printSvf()	{

		int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        try {
            if( ps4 == null ) ps4 = db2.prepareStatement( statementMeishou() );                 //名称マスター

            ps5 = db2.prepareStatement( statementMeiboMeisai() );       //受験者名簿
            ps6 = db2.prepareStatement( statementMeiboTotalPage() );    //総頁数
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
        printSvfMain();	            //統計表・名簿の印刷

        try {
            if( ps5 != null ) ps5.close();
            if( ps6 != null ) ps6.close();
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
            printSvfMeiboHead();        //名簿頁見出し印刷
            printSvfMeibo();            //名簿の印刷
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }

    }


	/**
     *  svf print 名簿の印刷
     */
    void printSvfMeibo() {

        //名簿印刷
		ResultSet rs = null;
		try{
            int p = 0;
			ps5.setString( ++p, param[2] );
			rs = ps5.executeQuery();
			int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            String examno = null;
            totalnum = new int[3];
            for( int i = 0 ; i < totalnum.length ; i++ ) totalnum[i] = 0;

			while( rs.next() ){
                //受験番号のブレイク
                if( examno == null  ||  ! examno.equals( rs.getString("EXAMNO") ) ){
                    if( examno != null ){
                        ret = svf.VrEndRecord();
                        nonedata = true;
                    }
                    printSvfMeiboOut1(rs);         //受験生別出力
                    examno = rs.getString("EXAMNO");
                    //統計
                    if( rs.getString("SEX").equals("1") ) totalnum[0]++;
                    if( rs.getString("SEX").equals("2") ) totalnum[1]++;
                    totalnum[2]++;
                }
                printSvfMeiboOut2(svf, rs);   //科目別出力
			}
            if( examno != null ){
                printTotalNum();		                //人数セット＆出力
                ret = svf.VrEndRecord();
                ret = svf.VrPrint();
                nonedata = true;
            }
		} catch( Exception ex ){
			log.error("printSvfMeibo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
		}
    }


	/**
     *  svf print 名簿 見出印刷
     */
    void printSvfMeiboHead()
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		ResultSet rs = null;

        try {
            ret = svf.VrSetForm("KNJL320H_2.frm", 4);
 			ret = svf.VrsOut("PRGID",          param[9] + "H_2" );
 			ret = svf.VrsOut("NENDO",          param[5] );
 			ret = svf.VrsOut("DATE",           param[6] );
 			ret = svf.VrsOut("TESTDIV",        param[10] );
		    //指示画面より指定された試験科目の取得
        	String retsql = getSubClass();
		    //指示画面より指定された試験科目の設定
        	setSubClass(db2, svf, retsql);
 			
 			for( int i=0 ; i<param.length ; i++ )log.debug("param["+i+"]="+param[i]);
        } catch( Exception ex ){
            log.error("printSvfMeiboHead error!",ex);
        }

        printSvfMeiboHead2( svf );

        //総ページ数
		try{
            int p = 0;
			ps6.setString( ++p, param[2] );
			rs = ps6.executeQuery();
            if( rs.next() ) totalpageno = rs.getInt("COUNT");
		} catch( Exception ex ){
			log.error("printSvfMeiboHead error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
            pageno = 0;
            outcount = 0;
		}

        printSvfTestdiv();
   }


	/**
     *  svf print 名簿 見出印刷 帳票別
     */
    void printSvfMeiboHead2(Vrw32alp svf)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
		    ret = svf.VrsOut("BORDERLINE",  "４科目計" + param[3] + "点以上");
        } catch( Exception ex ){
            log.error("printSvfMeiboHead2 error!",ex);
        }
    }


	/**
     *  svf print 名簿 明細 受験生別出力
     */
    void printSvfMeiboOut1(ResultSet rs)
	{
		int ret = 0;
		String retSql = "";
		
        if (false && 0 != ret) { ret = 0; }
		try{
			// 順位
	 		ret = svf.VrsOut( "RANK",      ( rs.getString("TOTAL_RANK4")    != null )? rs.getString("TOTAL_RANK4")  : ""  );
	 		// 受験番号
	 		ret = svf.VrsOut( "EXAMNO",    rs.getString("EXAMNO")  );                                               //受験番号
			// 氏名
	 		if(param[7].equals("1")){
		 		ret = svf.VrsOut(setformatArea("NAME", 10, rs.getString("NAME"))
						,rs.getString("NAME"));
	 		}
            // 性別
	 		ret = svf.VrsOut( "SEX",       ( rs.getString("SEXNAME")   != null )? rs.getString("SEXNAME") : ""  );  //性別
	 	    // 合計
	 		ret = svf.VrsOut( "TOTAL",    ( rs.getString("TOTAL4")    != null )? rs.getString("TOTAL4")  : ""  );  //４科目計

		    //*------------------------------------------------------------------*
		    //* <判定の設定>                                                     *
		    //* ・合格者			⇒受付データの合否区分＝1(合格)              *
		    //* ・不合格者			⇒受付データの合否区分＝2(不合格)            *
		    //* ・繰上合格者		⇒受付データの合否区分＝3(繰上合格候補)      *
		    //*						  且つ 基礎データの合否判定＝1(合格)         *
		    //*						  且つ 基礎データの特別措置区分＝1(繰上合格) *
		    //* ・繰上合格候補者	⇒受付データの合否区分＝3(繰上合格候補)      *
		    //*------------------------------------------------------------------*
            if( rs.getString("JUDGEDIV") != null ){
                if( rs.getString("JUDGEDIV").equals("1") ){
        			// 合格
                	retSql = createNameMstSql("L013", "1");
                } else {
                	// 不合格
                    if( rs.getString("JUDGEDIV").equals("2") ){
            			retSql = createNameMstSql("L013", "2");
        			}
                    if(rs.getString("JUDGEDIV").equals("3")){
            			// 繰上合格
        				if(nvlT(rs.getString("JUDGEMENT")).equals("1") && nvlT(rs.getString("SPECIAL_MEASURES")).equals("1")) {
                			retSql = createNameMstSql("L010", "1");
                    	} else {
                        	// 繰上合格候補者
                			retSql = createNameMstSql("L013", "3");
                    	}
                    }
                }
			    svf.VrsOut("JUDGEMENT",	getNameMast(db2, retSql));
            } else if(nvlT(rs.getString("EXAMINEE_DIV")).equals("2")){
	 		    //未受験を設定
            	ret = svf.VrsOut( "JUDGEMENT",  "未受験" );
            }
            
            // 改ページ判定
            if( 50 == outcount ){
                outcount = 0;
                ret = svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );       //ページ
            }
            outcount++;
		} catch( Exception ex ){
			log.error("printSvfMeiboOut1 error!",ex);
		}
	}


	/**
     *  svf print 名簿 明細 科目別出力
     */
    void printSvfMeiboOut2(Vrw32alp svf, ResultSet rs)
	{
		int ret = 0;
    	int idx_kamoku = 1;
        if (false && 0 != ret) { ret = 0; }
		try{
		    // 入試制度が高校一般または高校推薦の場合
			if(param[1].equals("2") || param[1].equals("3")){
				svf.VrsOut( "POINT" + idx_kamoku,	rs.getString("TOTAL_ALL") ); //内申点
				++idx_kamoku;
		    }
            if( rs.getString("SCORE") != null ){
            	// 志願者得点データより試験科目コードを取得
                String sTestKamoku = rs.getString("TESTSUBCLASSCD");
				// 試験科目が名称マスタの試験科目(L009)に存在する場合、設定
				for(int j=0;j<hkamoku.size();j++){
	                if(hkamoku.get(String.valueOf(idx_kamoku)).equals(sTestKamoku)){
						ret = svf.VrsOut( "POINT" + idx_kamoku,	rs.getString("SCORE") ); //各教科得点
	                }
	                ++idx_kamoku;
				}
            }
		    if(param[1].equals("2")){
				svf.VrsOut( "POINT" + idx_kamoku,	rs.getString("KASANTEN_ALL") ); //加算点
		    }
		} catch( Exception ex ){
			log.error("printSvfMeiboOut2 error!",ex);
		}
	}


	/**
     *  svf print 入試区分出力
     */
    void printSvfTestdiv()
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try{
	 		ret = svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );       //ページ
	 		ret = svf.VrsOut( "TOTAL_PAGE",  String.valueOf( totalpageno )  );    //総ページ
		} catch( Exception ex ){
			log.error("printSvfTestdiv error!",ex);
		}
	}

	/**
     *  preparedstatement 受験者名簿
     */
	String statementMeiboMeisai()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT  W1.EXAMNO, ");
            stb.append(        "W1.JUDGEDIV, ");
            stb.append(        "W3.NAME, ");
            stb.append(        "W2.EXAMINEE_DIV, ");
            stb.append(        "W4.TESTSUBCLASSCD, ");
            stb.append(        "W4.SCORE, ");
            stb.append(        "W5.TOTAL_ALL, ");
            stb.append(        "W5.KASANTEN_ALL, ");
            stb.append(        "W1.TOTAL4, ");
            stb.append(        "W1.TOTAL_RANK4, ");
            stb.append(        "W3.SEX, ");
            stb.append(        "MEISYOU_GET(W3.SEX,'Z002',1) AS SEXNAME, ");
            stb.append(        "W3.JUDGEMENT, ");
            stb.append(        "W3.SPECIAL_MEASURES, ");
            stb.append(        "W1.TESTDIV ");

            stb.append("FROM    ENTEXAM_DESIRE_DAT W2 ");
            stb.append(        "INNER JOIN ENTEXAM_RECEPT_DAT W1 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                            "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                            "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                            "W2.EXAMNO = W1.EXAMNO ");
            stb.append(        "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                   "W3.EXAMNO = W1.EXAMNO ");
            stb.append(        "LEFT JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                          "W4.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                          "W4.EXAM_TYPE = W1.EXAM_TYPE AND ");
            stb.append(                                          "W4.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                          "W4.RECEPTNO = W1.RECEPTNO ");
            stb.append(        "LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT W5 ON W5.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                   "   W5.EXAMNO = W1.EXAMNO ");
            stb.append("WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(        "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(        "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(        "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.受験者区分
            stb.append(        "W1.TOTAL4 IS NOT NULL AND ");                 //志願者受付データ.４科目合計
            stb.append(    param[3] + " <= VALUE(W1.TOTAL4, 0) AND ");
            stb.append(        "W1.EXAM_TYPE = '1' AND ");                    //志願者受付データ.受験型

            stb.append(        "W1.TESTDIV = ? ");

            if(param[4].equals("1")){
                stb.append("ORDER BY W1.TOTAL4 DESC, W1.EXAMNO, W4.TESTSUBCLASSCD ");
            } else {
                stb.append("ORDER BY W1.EXAMNO, W4.TESTSUBCLASSCD ");
            }
            
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 総ページ数
     */
	String statementMeiboTotalPage()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append(      "SELECT  CASE WHEN 0 < MOD(COUNT(DISTINCT W1.EXAMNO),50) THEN COUNT(DISTINCT W1.EXAMNO)/50 + 1 ELSE COUNT(DISTINCT W1.EXAMNO)/50 END AS COUNT ");

            stb.append(      "FROM    ENTEXAM_DESIRE_DAT W2 ");
            stb.append(              "INNER JOIN ENTEXAM_RECEPT_DAT W1 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(              "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");
            stb.append(              "LEFT JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                "W4.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                "W4.EXAM_TYPE = W1.EXAM_TYPE AND ");
            stb.append(                                                "W4.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                "W4.RECEPTNO = W1.RECEPTNO ");
            stb.append(              "LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT W5 ON W5.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "   W5.EXAMNO = W1.EXAMNO ");
            stb.append(      "WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(              "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(              "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(              "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.受験者区分
            stb.append(              "W1.TOTAL4 IS NOT NULL AND ");                 //志願者受付データ.４科目合計
            stb.append(          param[3] + " <= VALUE(W1.TOTAL4, 0) AND ");
            stb.append(              "W1.EXAM_TYPE = '1' AND ");                    //志願者受付データ.受験型
            stb.append(              "W1.TESTDIV = ? ");                            //受付データ.入試区分
            
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}
	
	/**
     *  名称マスタより、引数より取得した名称区分、名称コードを元に
     *  SQLモジュールを作成する。
     * @param sNamecd1		名称区分
     * @param sNamecd2		名称コード
     * @return	stb
     */
    private String createNameMstSql(String sNamecd1, String sNamecd2)
	{
    	stb = new StringBuffer();
        
		try{
            stb.append("SELECT  NAME1 ");
            stb.append(      "FROM    NAME_MST ");
            stb.append(      "WHERE   NAMECD1  = '" + sNamecd1 + "' AND ");
            stb.append(      "        NAMECD2  = '" + sNamecd2 + "'");

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

    /**
     *  引数より取得したＳＱＬを発行し
     *  名称マスタより名称１を取得する。
     * @param rs		実行結果オブジェクト
     * @return	retName	名称マスタより取得した名称
     */
    private String getNameMast(DB2UDB db2, String stb)
	{
    	String retName = "";
		PreparedStatement ps = null;
    	ResultSet rs = null;
		try{
			// SQL発行
			ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            
   			if ( rs.next()  &&  rs.getString(1) != null ){
   				retName = rs.getString("NAME1");
   			}
		} catch( Exception ex ){
            log.error("setJudgementSql error!",ex);
    	} finally {
    		db2.commit();
    		DbUtils.closeQuietly(null, ps, rs);
    	}
		return retName;
	}
    
    /**
     * 帳票に設定する文字数が制限文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name	帳票出力エリア
     * @param area_len		制限文字数
     * @param sval			値
     * @return
     */
    private String setformatArea(String area_name, int area_len, String sval) {

    	String retAreaName = "";
		// 値がnullの場合はnullが返される
    	if (sval == null) {
			return null;
		}
    	// 設定値が制限文字超の場合、帳票設定エリアの変更を行う
    	if(area_len >= sval.length()){
   			retAreaName = area_name + "1";
    	} else {
   			retAreaName = area_name + "2";
    	}
        return retAreaName;
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
		    // 試験制度が高校(一般)の場合、帳票の試験科目名称を
		    // 『内申』より設定する
		    if(param[1].equals("2") || param[1].equals("3")){
		    	svf.VrsOut("SUBCLASS_NAME" + idx_kamoku	,	"内申");
				++idx_kamoku;
		    }
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
	 */
	private String nvlT(String val) {

		if (val == null) {
			return "";
		} else {
			return val;
		}
	}

}//クラスの括り
