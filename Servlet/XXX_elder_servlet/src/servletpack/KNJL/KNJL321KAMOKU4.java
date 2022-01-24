/**
 *
 *	学校教育システム 賢者 [入試処理]  本選考資料４科目成績一覧表
 *
 *	2004/12/20
 *  2005/01/05 科目コードを社会:3 理科:4に変更
 *             入試区分の履歴の表記において、受付データの合否区分=1の時は◎を出力、を追加
 *             結果表の集計仕様を変更
 *  2005/01/08 出願コース名は出力しない
 *             入試制度名は出力しない
 *  2005/01/12 db2.commit()を随所に入れる
 *  2005/01/14 入試区分の履歴の表記において、受付データの合否区分=1,特待区分=1の時は※を出力、を追加 NO001
 *
 */

package servletpack.KNJL;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJL321KAMOKU4 extends KNJL321BASE {

    private static final Log log = LogFactory.getLog(KNJL321KAMOKU4.class);

    private StringBuffer stb;
	private ResultSet rs;
	//boolean nonedata;
    int pageno;
    int totalpageno;
    int outcount;
    int absent_norecept;    //受付データがない欠席者数 ２科目

    KNJL321KAMOKU4(DB2UDB db2, Vrw32alp svf, String param[]){
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

            ps2 = db2.prepareStatement( statementMeiboMeisai() );       //受験者名簿
            ps3 = db2.prepareStatement( statementMeiboTotalPage() );    //総頁数
            psTestDiv = db2.prepareStatement(preTestDivMst());
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
        printSvfMain();	            //統計表・名簿の印刷

        try {
            if( ps2 != null ) ps2.close();
            if( ps3 != null ) ps3.close();
            if( psTestDiv != null ) psTestDiv.close();
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

        //名簿頁見出し印刷
        //printSvfMeiboHead();

        //名簿印刷
		ResultSet rs = null;
		try{
            int p = 0;
			ps2.setString( ++p, param[2] );
            if ("KNJL320".equals(param[9]) || "KNJL321".equals(param[9])) {
                ps2.setString( ++p, param[2] );
            }
			rs = ps2.executeQuery();
			int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            String examno = null;
            int total2 = 0;
            int total4 = 0;
            int rank = 0;
            int rankcount = 0;
            totalnum = new int[3];
            for( int i = 0 ; i < totalnum.length ; i++ ) totalnum[i] = 0;
            fsAreaRuikei = new int[3];
            for( int i = 0 ; i < fsAreaRuikei.length ; i++ ) fsAreaRuikei[i] = 0;

			while( rs.next() ){
                //受験番号のブレイク
                if( examno == null  ||  ! examno.equals( rs.getString("EXAMNO") ) ){
                    if( examno != null ){
                        ret = svf.VrEndRecord();
                        nonedata = true;
                    }
                    rankcount++;
					if( judgeRank(rs, total2, total4) ){	// 04/12/30Modify by nakamoto
                        rank = rankcount;
                        total2 = rs.getInt("TOTAL2");
                        total4 = rs.getInt("TOTAL4");
                    }
                    //地区累計 01:埼玉,02:東京,07:神奈川
                    if ("01".equals(rs.getString("FS_AREA_CD"))) fsAreaRuikei[0]++;
                    if ("02".equals(rs.getString("FS_AREA_CD"))) fsAreaRuikei[1]++;
                    if ("07".equals(rs.getString("FS_AREA_CD"))) fsAreaRuikei[2]++;
                    printSvfMeiboOut1(rs, rank);         //受験生別出力
                    examno = rs.getString("EXAMNO");
                    //統計
                    if( rs.getString("SEX").equals("1") ) totalnum[0]++;
                    if( rs.getString("SEX").equals("2") ) totalnum[1]++;
                    totalnum[2]++;
                }
                printSvfMeiboOut2(rs);                  //科目別出力
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
     *  順位判定フラグ
     *  ２科計のみで順位を判定する	(04/12/30Add by nakamoto)
     */
	boolean judgeRank(ResultSet rs, int total2, int total4)
	{
        try {
            if( rs.getInt("TOTAL4") != total4 ) return true;
        } catch( Exception ex ){
            log.error("judgeRank error!",ex);
        }
		return false;
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
            ret = svf.VrSetForm("KNJL320_2.frm", 4);
 			ret = svf.VrsOut("PRGID",          param[9] + "_2" );
 			ret = svf.VrsOut("NENDO",          param[5] );
 			ret = svf.VrsOut("DATE",           param[6] );
 			//ret = svf.VrsOut("EXAM_TYPE",      "（４科目）" );
 			ret = svf.VrsOut("EXAM_TYPE",      ("KNJL320".equals(param[9]) || "KNJL321".equals(param[9])) ? "（本選考資料：合計選考用）" : "" );  // 05/01/29Modify
 			ret = svf.VrsOut("TESTDIV",        param[10] );
for( int i=0 ; i<param.length ; i++ )log.debug("param["+i+"]="+param[i]);
        } catch( Exception ex ){
            log.error("printSvfMeiboHead error!",ex);
        }

        if ("KNJL320".equals(param[9]) || "KNJL321".equals(param[9])) {
            printSvfMeiboHead2( svf );
        }

        //総ページ数
		try{
            int p = 0;
			ps3.setString( ++p, param[2] );
			rs = ps3.executeQuery();
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
            if( param[12] != null )
 			    ret = svf.VrsOut("BORDERLINE",  "２科目計" + param[12] + "未満で、４科目計" + param[11] + "点以上");
            else
 			    ret = svf.VrsOut("BORDERLINE",  "合計" + param[11] + "点以上");
        } catch( Exception ex ){
            log.error("printSvfMeiboHead2 error!",ex);
        }
    }

    private String getMark(String judgediv, String judgeclass, String honordiv) {
        String mark="";
        if (judgediv == null) mark= "";
        else if ("1".equals(judgediv) && "1".equals(honordiv)) mark= "☆";
        else if ("1".equals(judgediv) && "3".equals(judgeclass)) mark= "◎";
        else if ("1".equals(judgediv) && "4".equals(judgeclass)) mark= "○";
        else if ("1".equals(judgediv) && "2".equals(judgeclass)) mark= "◎";
        else if ("1".equals(judgediv) && "1".equals(judgeclass)) mark= "○";
        else if ("1".equals(judgediv) && "6".equals(judgeclass)) mark= "○";
        else if ("2".equals(judgediv)) mark= "×";
        return mark;
    }

	/**
     *  svf print 名簿 明細 受験生別出力
     *      2005/01/05 ４科目計を出力しない
     *      2005/01/05 条件欄に合否判定'◎'を追加
     */
    void printSvfMeiboOut1(ResultSet rs, int rank)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rsTestDiv = null;
		try{
	 		ret = svf.VrsOut( "RANK",      String.valueOf( rank )  );                                               //順位
	 		ret = svf.VrsOut( "EXAMNO",    rs.getString("EXAMNO")  );                                               //受験番号
	 		ret = svf.VrsOut( "NAME",      ( rs.getString("NAME")      != null )? rs.getString("NAME")    : ""  );  //氏名
            ret = svf.VrsOut( "SEX",       ( rs.getString("SEXNAME")   != null )? rs.getString("SEXNAME") : ""  );  //性別
	 		ret = svf.VrsOut( "2TOTAL",    ( rs.getString("TOTAL2")    != null )? rs.getString("TOTAL2")  : ""  );  //２科目計
	 	    ret = svf.VrsOut( "4TOTAL",    ( rs.getString("TOTAL4")    != null )? rs.getString("TOTAL4")  : ""  );  //４科目計
            ret = svf.VrsOut( "KATEN",     ( rs.getString("KATEN")     != null )? rs.getString("KATEN")   : ""  );  //加点

            if( rs.getString("REMARK1")  !=  null || rs.getString("ENTDIV")  !=  null) {
                String remark = "";
                if (rs.getString("REMARK1") != null) { 
                    remark += rs.getString("REMARK1");
                }
                if (rs.getString("ENTDIV") != null && "1".equals(rs.getString("ENTDIV"))) {
                    remark += ("".equals(remark) ? "" : " ") + "入学手続済み";
                }
                ret = svf.VrsOut( ( 10 >= remark.length() )? "NOTE1_1" : "NOTE1_2",  remark );  //備考
            }

//            ret = svf.VrsOut( "ABSENCE",   ( rs.getString("ABSENCE_DAYS")     != null )? rs.getString("ABSENCE_DAYS") : ""  );  //欠席数

//            String examno = rs.getString("EXAMNO");
//            ret = svf.VrsOut( "CHECK1", getMark(examno, rs.getString("TEST2"), rs.getString("HONORDIV2")));
//            ret = svf.VrsOut( "CHECK2", getMark(examno, rs.getString("TEST3"), rs.getString("HONORDIV3")));
//            ret = svf.VrsOut( "CHECK3", getMark(examno, rs.getString("TEST4"), rs.getString("HONORDIV4")));
//            ret = svf.VrsOut( "CHECK4", getMark(examno, rs.getString("TEST5"), rs.getString("HONORDIV5")));
//            ret = svf.VrsOut( "CHECK5", getMark(examno, rs.getString("TEST6"), rs.getString("HONORDIV6")));
            int num = 0;
            rsTestDiv = psTestDiv.executeQuery();
            while (rsTestDiv.next()) {
                num++;
                String testdiv = rsTestDiv.getString("TESTDIV");

                String mark = getMark(rs.getString("JUDGEDIV" + testdiv), rs.getString("JUDGECLASS" + testdiv), rs.getString("HONORDIV" + testdiv));
                //重複受検
                if ("KNJL321".equals(param[9])) {
                    if ("".equals(mark)) mark = getMark(rs.getString("RECOM1_JUDGEDIV" + testdiv), rs.getString("RECOM1_JUDGECLASS" + testdiv), rs.getString("RECOM1_HONORDIV" + testdiv));
                    if ("".equals(mark)) mark = getMark(rs.getString("RECOM2_JUDGEDIV" + testdiv), rs.getString("RECOM2_JUDGECLASS" + testdiv), rs.getString("RECOM2_HONORDIV" + testdiv));
                    if ("".equals(mark)) mark = getMark(rs.getString("RECOM3_JUDGEDIV" + testdiv), rs.getString("RECOM3_JUDGECLASS" + testdiv), rs.getString("RECOM3_HONORDIV" + testdiv));
                }
                ret = svf.VrsOut("CHECK" + num, mark);
            }
            rsTestDiv.close();
 
            //地区累計 01:埼玉,02:東京,07:神奈川
            ret = svf.VrsOut( "AREACD", ( rs.getString("FS_AREA_CD") != null )? rs.getString("FS_AREA_CD") : ""  );  //地区コード
            ret = svf.VrsOut( "AREA1",  String.valueOf(fsAreaRuikei[0])  );  //埼玉累計
            ret = svf.VrsOut( "AREA2",  String.valueOf(fsAreaRuikei[1])  );  //東京累計
            ret = svf.VrsOut( "AREA3",  String.valueOf(fsAreaRuikei[2])  );  //神奈川累計

            if( 60 == outcount ){
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
     *            ENTEXAM_SCORE_DATのTESTSUBCLASSCDはNOT NULL
     *      2005/01/05 社会・理科は出力しない
     */
    void printSvfMeiboOut2(ResultSet rs)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try{
            if( rs.getString("SCORE") != null ){
                if( rs.getString("TESTSUBCLASSCD").equals("1") )
	 		        ret = svf.VrsOut( "POINT1",    rs.getString("SCORE") ); //国語得点
                else if( rs.getString("TESTSUBCLASSCD").equals("2") )
	 		        ret = svf.VrsOut( "POINT2",    rs.getString("SCORE") ); //算数得点
                else if( rs.getString("TESTSUBCLASSCD").equals("3") )
	 		        ret = svf.VrsOut( "POINT3",    rs.getString("SCORE") ); //社会得点
                else if( rs.getString("TESTSUBCLASSCD").equals("4") )
	 		        ret = svf.VrsOut( "POINT4",    rs.getString("SCORE") ); //理科得点
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
     *  preparedstatement 共通部分
     *     2005/01/05Modify
     *     名簿、総ページ数、統計の得点および受験者数に共通するテーブル抽出
     *     得点データ、内申データをリンク
     *     入試区分TESTDIVは本文で指定
     *     統計はこれを使わない => statementCommon3Type4()を使用
     *
	String statementCommon1()
	{
        StringBuffer stb = new StringBuffer();
		try{
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
            stb.append(              "W1.TOTAL2 IS NOT NULL AND ");                 //志願者受付データ.２科目合計
            stb.append(              "W1.TOTAL4 IS NOT NULL AND ");                 //志願者受付データ.４科目合計
            if( param[12] != null )
                stb.append(          "VALUE(W1.TOTAL2, 0) < " + param[12] + " AND ");
            if( param[11] != null )
                stb.append(          param[11] + " <= VALUE(W1.TOTAL4, 0) AND ");
            stb.append(              "W1.EXAM_TYPE = '2' AND ");                    //志願者受付データ.受験型(４科目型)
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}
    **************************** */

	/**
     *  preparedstatement 共通部分
     *     2005/01/05Modify
     *     名簿、総ページ数、統計の得点および受験者数に共通するテーブル抽出
     *     統計はこれを使わない => statementCommon4Type4()を使用
     *     入試区分TESTDIVは本文で指定
     *
	String statementCommon2()
	{
        StringBuffer stb = new StringBuffer();
		try{
            stb.append(      "FROM    ENTEXAM_DESIRE_DAT W2 ");
            stb.append(              "INNER JOIN ENTEXAM_RECEPT_DAT W1 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(              "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");
            stb.append(      "WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(              "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(              "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(              "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.受験者区分
            stb.append(              "W1.TOTAL2 IS NOT NULL AND ");                 //志願者受付データ.２科目合計
            // 05/01/05 stb.append(              "W1.TOTAL4 IS NOT NULL AND ");                 //志願者受付データ.４科目合計
            // 05/01/15 if( param[12] != null )
            //    stb.append(          "VALUE(W1.TOTAL2, 0) < " + param[12] + " AND ");
            if( param[11] != null )
                stb.append(          param[11] + " <= VALUE(W1.TOTAL4, 0) AND ");
            stb.append(              "W1.EXAM_TYPE = '2' AND ");                    //志願者受付データ.受験型(４科目型)
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}
    ************************ */

	/**
     *  preparedstatement 受験者名簿
     */
	String statementMeiboMeisai()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("WITH T_MAIN AS ( ");
            stb.append(      "SELECT  W1.EXAMNO, ");
            stb.append(              "W3.RECOM_EXAMNO1, ");
            stb.append(              "W3.RECOM_EXAMNO2, ");
            stb.append(              "W3.RECOM_EXAMNO3, ");
            stb.append(              "W3.NAME, ");
            stb.append(              "W4.TESTSUBCLASSCD, ");
            stb.append(              "W4.SCORE, ");
            stb.append(              "W1.TOTAL2, ");
            stb.append(              "W1.TOTAL4, ");
            stb.append(              "W1.KATEN, ");
            stb.append(              "W3.SEX, ");
            stb.append(              "W3.REMARK1, ");
            stb.append(              "W5.ABSENCE_DAYS, ");
            stb.append(              "W1.TESTDIV, ");
            stb.append(              "W3.ENTDIV, ");
            stb.append(              "W3.FS_AREA_CD, ");
            stb.append(              "L1.NAME1 AS FS_AREA_NAME ");

            //stb.append( statementCommon1() );   //共通部分
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
            stb.append(              "LEFT JOIN NAME_MST L1 ON L1.NAMECD1='Z003' AND L1.NAMECD2=W3.FS_AREA_CD ");
            stb.append(      "WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(              "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(              "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(              "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.受験者区分
            stb.append(              "W1.TOTAL2 IS NOT NULL AND ");                 //志願者受付データ.２科目合計
            stb.append(              "W1.TOTAL4 IS NOT NULL AND ");                 //志願者受付データ.４科目合計
            if( param[12] != null )
                stb.append(          "VALUE(W1.TOTAL2, 0) < " + param[12] + " AND ");
            if( param[11] != null )
                stb.append(          param[11] + " <= VALUE(W1.TOTAL4, 0) AND ");
            stb.append(              "W1.EXAM_TYPE = '2' AND ");                    //志願者受付データ.受験型(４科目型)

            stb.append(              "W1.TESTDIV = ? ");
            stb.append(") ");

		    stb.append(", T_JUDGE AS ( ");
            stb.append(      "SELECT  W1.EXAMNO, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '0' THEN W1.JUDGEDIV END) AS JUDGEDIV0, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '1' THEN W1.JUDGEDIV END) AS JUDGEDIV1, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '2' THEN W1.JUDGEDIV END) AS JUDGEDIV2, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '3' THEN W1.JUDGEDIV END) AS JUDGEDIV3, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '4' THEN W1.JUDGEDIV END) AS JUDGEDIV4, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '5' THEN W1.JUDGEDIV END) AS JUDGEDIV5, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '6' THEN W1.JUDGEDIV END) AS JUDGEDIV6, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '0' THEN W1.JUDGECLASS END) AS JUDGECLASS0, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '1' THEN W1.JUDGECLASS END) AS JUDGECLASS1, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '2' THEN W1.JUDGECLASS END) AS JUDGECLASS2, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '3' THEN W1.JUDGECLASS END) AS JUDGECLASS3, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '4' THEN W1.JUDGECLASS END) AS JUDGECLASS4, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '5' THEN W1.JUDGECLASS END) AS JUDGECLASS5, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '6' THEN W1.JUDGECLASS END) AS JUDGECLASS6, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '0' THEN W1.HONORDIV END) AS HONORDIV0, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '1' THEN W1.HONORDIV END) AS HONORDIV1, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '2' THEN W1.HONORDIV END) AS HONORDIV2, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '3' THEN W1.HONORDIV END) AS HONORDIV3, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '4' THEN W1.HONORDIV END) AS HONORDIV4, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '5' THEN W1.HONORDIV END) AS HONORDIV5, ");
            stb.append(              "max(CASE WHEN W1.TESTDIV = '6' THEN W1.HONORDIV END) AS HONORDIV6, ");
            stb.append(              "SUM(CASE W1.TESTDIV WHEN '1' THEN (CASE VALUE(W1.JUDGEDIV,'0') WHEN '1' THEN (CASE VALUE(W1.JUDGECLASS,'0') WHEN '1' THEN 1 WHEN '2' THEN 1 WHEN '3' THEN 3 WHEN '4' THEN 4 END) WHEN '2' THEN 2 END) ELSE NULL END) AS TEST2,  ");
            stb.append(              "SUM(CASE W1.TESTDIV WHEN '2' THEN (CASE VALUE(W1.JUDGEDIV,'0') WHEN '1' THEN (CASE VALUE(W1.JUDGECLASS,'0') WHEN '1' THEN 1 WHEN '2' THEN 1 WHEN '3' THEN 3 WHEN '4' THEN 4 END) WHEN '2' THEN 2 END) ELSE NULL END) AS TEST3,  ");
            stb.append(              "SUM(CASE W1.TESTDIV WHEN '3' THEN (CASE VALUE(W1.JUDGEDIV,'0') WHEN '1' THEN (CASE VALUE(W1.JUDGECLASS,'0') WHEN '1' THEN 1 WHEN '2' THEN 1 WHEN '3' THEN 3 WHEN '4' THEN 4 END) WHEN '2' THEN 2 END) ELSE NULL END) AS TEST4,  ");
            stb.append(              "SUM(CASE W1.TESTDIV WHEN '4' THEN (CASE VALUE(W1.JUDGEDIV,'0') WHEN '1' THEN (CASE VALUE(W1.JUDGECLASS,'0') WHEN '1' THEN 1 WHEN '2' THEN 1 WHEN '3' THEN 3 WHEN '4' THEN 4 END) WHEN '2' THEN 2 END) ELSE NULL END) AS TEST5,  ");
            stb.append(              "SUM(CASE W1.TESTDIV WHEN '5' THEN (CASE VALUE(W1.JUDGEDIV,'0') WHEN '1' THEN (CASE VALUE(W1.JUDGECLASS,'0') WHEN '1' THEN 1 WHEN '2' THEN 1 WHEN '3' THEN 3 WHEN '4' THEN 4 END) WHEN '2' THEN 2 END) ELSE NULL END) AS TEST6  ");

            //stb.append( statementCommon2() );   //共通部分
            stb.append(      "FROM    ENTEXAM_DESIRE_DAT W2 ");
            stb.append(              "INNER JOIN ENTEXAM_RECEPT_DAT W1 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(              "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");
            stb.append(      "WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(              "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(              "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(              "W2.EXAMINEE_DIV = '1' ");                 //志願者データ.受験者区分
            if ("KNJL320".equals(param[9]) || "KNJL321".equals(param[9])) {
                stb.append(      "AND W1.TESTDIV < ? ");                            //受付データ.入試区分
            }
            stb.append(      "GROUP BY W1.EXAMNO ");
            stb.append(") ");

		    stb.append("SELECT  T1.EXAMNO, ");
            stb.append(        "T1.NAME, ");
            stb.append(        "T1.TESTSUBCLASSCD, ");
            stb.append(        "T1.SCORE, ");
            stb.append(        "T1.TOTAL2, ");
            stb.append(        "T1.TOTAL4, ");
            stb.append(        "T1.KATEN, ");
            stb.append(        "T1.SEX, ");
            stb.append(        "MEISYOU_GET(SEX,'Z002',1) AS SEXNAME, ");
            stb.append(        "T1.REMARK1, ");
            stb.append(        "T1.FS_AREA_CD, ");
            stb.append(        "T1.FS_AREA_NAME, ");
            stb.append(        "T1.TESTDIV, ");
            stb.append(        "T1.ABSENCE_DAYS, ");
            stb.append(        "T2.TEST2, ");
            stb.append(        "T2.TEST3, ");
            stb.append(        "T2.TEST4, ");
            stb.append(        "T2.TEST5, ");
            stb.append(        "T2.TEST6, ");
            for (int i = 0; i <= 6; i++) {
                stb.append(        "T2.JUDGEDIV"   + i + ", ");
                stb.append(        "T2.JUDGECLASS" + i + ", ");
                stb.append(        "T2.HONORDIV"   + i + ", ");
                //重複受検
                stb.append(        "E1.JUDGEDIV"   + i + " AS RECOM1_JUDGEDIV"   + i + ", ");
                stb.append(        "E1.JUDGECLASS" + i + " AS RECOM1_JUDGECLASS" + i + ", ");
                stb.append(        "E1.HONORDIV"   + i + " AS RECOM1_HONORDIV"   + i + ", ");
                stb.append(        "E2.JUDGEDIV"   + i + " AS RECOM2_JUDGEDIV"   + i + ", ");
                stb.append(        "E2.JUDGECLASS" + i + " AS RECOM2_JUDGECLASS" + i + ", ");
                stb.append(        "E2.HONORDIV"   + i + " AS RECOM2_HONORDIV"   + i + ", ");
                stb.append(        "E3.JUDGEDIV"   + i + " AS RECOM3_JUDGEDIV"   + i + ", ");
                stb.append(        "E3.JUDGECLASS" + i + " AS RECOM3_JUDGECLASS" + i + ", ");
                stb.append(        "E3.HONORDIV"   + i + " AS RECOM3_HONORDIV"   + i + ", ");
            }
            stb.append(        "T1.ENTDIV ");
            stb.append("FROM ");
            stb.append(      "T_MAIN T1 ");
            stb.append(      "LEFT JOIN T_JUDGE T2 ON T2.EXAMNO = T1.EXAMNO ");
            stb.append(      "LEFT JOIN T_JUDGE E1 ON E1.EXAMNO = T1.RECOM_EXAMNO1 ");
            stb.append(      "LEFT JOIN T_JUDGE E2 ON E2.EXAMNO = T1.RECOM_EXAMNO2 ");
            stb.append(      "LEFT JOIN T_JUDGE E3 ON E3.EXAMNO = T1.RECOM_EXAMNO3 ");

            stb.append("ORDER BY T1.TOTAL4 DESC, T1.EXAMNO, T1.TESTSUBCLASSCD ");	// 04/12/30Modify by nakamoto
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
            stb.append(      "SELECT  CASE WHEN 0 < MOD(COUNT(DISTINCT W1.EXAMNO),60) THEN COUNT(DISTINCT W1.EXAMNO)/60 + 1 ELSE COUNT(DISTINCT W1.EXAMNO)/60 END AS COUNT ");

            //stb.append( statementCommon1() );   //共通部分
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
            stb.append(              "W1.TOTAL2 IS NOT NULL AND ");                 //志願者受付データ.２科目合計
            stb.append(              "W1.TOTAL4 IS NOT NULL AND ");                 //志願者受付データ.４科目合計
            if( param[12] != null )
                stb.append(          "VALUE(W1.TOTAL2, 0) < " + param[12] + " AND ");
            if( param[11] != null )
                stb.append(          param[11] + " <= VALUE(W1.TOTAL4, 0) AND ");
            stb.append(              "W1.EXAM_TYPE = '2' AND ");                    //志願者受付データ.受験型(４科目型)

            stb.append(              "W1.TESTDIV = ? ");                                   //受付データ.入試区分
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

    String preTestDivMst()
    {
        if( stb == null ) stb = new StringBuffer();
        else              stb.delete(0,stb.length());
        try{
            stb.append("SELECT TESTDIV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '"+param[0]+"' ORDER BY SHOWORDER, TESTDIV ");
        } catch( Exception ex ){
            log.error("sql statement error!"+ex );
        }
        return stb.toString();
    }

}//クラスの括り
