/**
 *
 *	学校教育システム 賢者 [入試処理]  本選考資料表紙
 *
 *	2004/12/20
 *  2005/01/05 科目コードを社会:3 理科:4に変更
 *             入試区分の履歴の表記において、受付データの合否区分=1の時は◎を出力、を追加
 *             結果表の集計仕様を変更
 *             結果表の出力を追加
 *             ２科目成績一覧表の出力を追加
 *  2005/01/08 出願コース名は出力しない
 *  2005/01/12 db2.commit()を随所に入れる
 *
 */

package servletpack.KNJL;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJL321HYOUSI extends KNJL321BASE {

    private static final Log log = LogFactory.getLog(KNJL321HYOUSI.class);

    private StringBuffer stb;
	private ResultSet rs;
    int outcount;
    int absent_norecept;    //受付データがない欠席者数 ２科目


    KNJL321HYOUSI(DB2UDB db2, Vrw32alp svf, String param[]){
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
        printSvfMain();	          //統計表・名簿の印刷

        try {
            if( ps1 != null ) ps1.close();
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
            ret = svf.VrSetForm("KNJL320_1.frm", 1);
 			ret = svf.VrsOut("PRGID",          param[9] + "_1" );
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

        boolean nonedata = false;
		ResultSet rs = null;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
for( int i=0 ; i<param.length ; i++ )log.debug("param["+i+"]="+param[i]);

        //２科目計
		try{
            ps1 = db2.prepareStatement( statementKekkahyoType2() );                //結果表
//log.debug("kamoku2="+ps1.toString());
			rs = ps1.executeQuery();
log.debug("kamoku2 end");

			while( rs.next() ){
                printSvfKekkahyoOut1( rs );                     //統計出力
                nonedata = true;
			}
		} catch( Exception ex ){
			log.error("printSvfKekkahyo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
		}

        //４科目計
		try{
            if( param[11] != null ){
                ps1 = db2.prepareStatement( statementKekkahyoType4() );                //結果表
//log.debug("kamoku4="+ps1.toString());
			    rs = ps1.executeQuery();
log.debug("kamoku4 end");
                while( rs.next() ){
                    printSvfKekkahyoOut1( rs );                     //統計出力
                    nonedata = true;
                }
            }
		} catch( Exception ex ){
			log.error("printSvfKekkahyo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
		}

        //国・算
		try{
            ps1 = db2.prepareStatement( statementKekkahyoKokuSan() );                //結果表
//log.debug("KokuSan="+ps1.toString());
			rs = ps1.executeQuery();
log.debug("KokuSan end");
			while( rs.next() ){
                printSvfKekkahyoOut1( rs );                     //統計出力
                nonedata = true;
			}
		} catch( Exception ex ){
			log.error("printSvfKekkahyo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
		}

        //社・理
		try{
            if( param[11] != null ){
                ps1 = db2.prepareStatement( statementKekkahyoShaRi() );                //結果表
//log.debug("ShaRi="+ps1.toString());
			    rs = ps1.executeQuery();
log.debug("ShaRoka end");
                while( rs.next() ){
                    printSvfKekkahyoOut1( rs );                     //統計出力
                    nonedata = true;
                }
            }
		} catch( Exception ex ){
			log.error("printSvfKekkahyo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
		}

        if( nonedata ){
            ret = svf.VrEndPage();
            ret = svf.VrPrint();
            super.nonedata = true;
        }

    }


	/**
     *  svf print 統計表 統計出力
     *      変数について absent_norecept       :受付データがない欠席者数 ２科目
     *                   absent_norecept_type4 :受付データがない欠席者数 ４科目 無用？
     *      値について   ４科目受験者数は２科目受験者数から２科目型の受験者数を引いた数
     *                   ４科目欠席者数は２科目欠席者数から２科目型の欠席者数を引いた数
     *                   科目の欠席者数は科目欠席者に受付データがない欠席者数を足した数
     */
    void printSvfKekkahyoOut1( ResultSet rs )
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        String subclass = null;

		try{
            if( rs.getString("SUBCLASSCD").equals("A") ){            //２科目合計
                subclass = "2TOTAL";
                if( rs.getString("ABSENT_NOTRECEPT") != null ) absent_norecept = Integer.parseInt( rs.getString("ABSENT_NOTRECEPT") );
            } else if( rs.getString("SUBCLASSCD").equals("B") ){     //４科目合計
                subclass = "4TOTAL";
            } else if( rs.getString("SUBCLASSCD").equals("1") )      //国語
                subclass = "SUBCLASS1";
            else if( rs.getString("SUBCLASSCD").equals("2") )        //算数
                subclass = "SUBCLASS2";
            else if( rs.getString("SUBCLASSCD").equals("3") )        //社会
                subclass = "SUBCLASS3";
            else if( rs.getString("SUBCLASSCD").equals("4") )        //理科
                subclass = "SUBCLASS4";

            if( subclass == null ) return;

            ret = svf.VrsOutn( subclass,  1,  ( rs.getString("MAXSCORE") != null )? rs.getString("MAXSCORE") : ""  ); //最高点
            ret = svf.VrsOutn( subclass,  2,  ( rs.getString("MINSCORE") != null )? rs.getString("MINSCORE") : ""  ); //最低点
            ret = svf.VrsOutn( subclass,  3,  ( rs.getString("AVERAGE")  != null )? String.valueOf(df.format(rs.getFloat("AVERAGE"))) : "" ); //平均点

            if( subclass.equals("SUBCLASS1")  ||  subclass.equals("SUBCLASS2") ){
                ret = svf.VrsOutn( subclass,  4,  ( rs.getString("PRESENT")  != null )? rs.getString("PRESENT")  : ""  ); //受験者数
                ret = svf.VrsOutn( subclass,  5,  ( rs.getString("ABSENT")   != null )? String.valueOf( Integer.parseInt( rs.getString("ABSENT") ) + absent_norecept )  : ""  ); //欠席者数
            } else if( subclass.equals("SUBCLASS3")  ||  subclass.equals("SUBCLASS4") ){
                ret = svf.VrsOutn( subclass,  4,  ( rs.getString("PRESENT")  != null )? rs.getString("PRESENT")  : ""  ); //受験者数
                ret = svf.VrsOutn( subclass,  5,  ( rs.getString("ABSENT")   != null )? String.valueOf( Integer.parseInt( rs.getString("ABSENT") ) + absent_norecept )  : ""  ); //欠席者数
            } else if( subclass.equals("2TOTAL") ){
                if( rs.getString("PRESENT") != null ){
                    ret = svf.VrsOutn( subclass,  4,  ( rs.getString("PRESENT")  != null )? rs.getString("PRESENT") + "(" + rs.getString("PRESENT_TYPE") + ")"  : ""  ); //受験者数
                    if( param[11] != null )
                        ret = svf.VrsOutn( "4TOTAL",  4,  String.valueOf( Integer.parseInt(rs.getString("PRESENT")) - Integer.parseInt(rs.getString("PRESENT_TYPE")) ) );    //受験者数
                }
                if( rs.getString("ABSENT") != null ){
                    ret = svf.VrsOutn( subclass,  5,  ( rs.getString("ABSENT")   != null )? rs.getString("ABSENT") + "(" + rs.getString("ABSENT_TYPE") + ")"   : ""  );  //欠席者数
                    if( param[11] != null )
                        ret = svf.VrsOutn( "4TOTAL",  5,  String.valueOf( Integer.parseInt(rs.getString("ABSENT")) - Integer.parseInt(rs.getString("ABSENT_TYPE")) ) );      //欠席者数
                }
            }
		} catch( Exception ex ){
			log.error("printSvfKekkahyoOut1 error!",ex);
		}

	}


	/**
     *  preparedstatement 結果表
     */
	String statementKekkahyoKokuSan()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            //２科目（国・数）
            stb.append("SELECT  TESTSUBCLASSCD AS SUBCLASSCD, ");
            stb.append(        "MAX(SCORE) AS MAXSCORE, ");
            stb.append(        "MIN(SCORE) AS MINSCORE, ");
            stb.append(        "ROUND(AVG(FLOAT(SCORE))*10,0)/10 AS AVERAGE, ");
            stb.append(        "SUM(CASE ATTEND_FLG WHEN '1' THEN 1 ELSE 0 END) AS PRESENT, ");
            stb.append(        "0 AS PRESENT_TYPE, ");
            stb.append(        "SUM(CASE ATTEND_FLG WHEN '0' THEN 1 ELSE 0 END) AS ABSENT, ");
            stb.append(        "0 AS ABSENT_TYPE, ");
                                /* 受験していない欠席者数 */
            stb.append(        "0 AS ABSENT_NOTRECEPT, ");
            stb.append(        "0 AS ABSENT_NOTRECEPT_TYPE ");

            stb.append("FROM    ENTEXAM_DESIRE_DAT W2 ");
            stb.append(        "INNER JOIN ENTEXAM_RECEPT_DAT W1 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(        "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");
/*
            stb.append(        "INNER JOIN (");
            stb.append(        "SELECT  SCORE, ");
            stb.append(                "ATTEND_FLG, ");
            stb.append(                "TESTSUBCLASSCD, ");
            stb.append(                "EXAM_TYPE,");
            stb.append(                "RECEPTNO ");
            stb.append(        "FROM    ENTEXAM_SCORE_DAT W4 ");
            stb.append(        "WHERE   W4.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(                "W4.APPLICANTDIV = '" + param[1] + "' AND ");
            stb.append(                "W4.TESTDIV = '" + param[2] + "' AND ");
            stb.append(                "W4.TESTSUBCLASSCD IN ('1','2') AND ");
            stb.append(                "W4.SCORE IS NOT NULL ");
            stb.append(        ")W4 ON W4.EXAM_TYPE = W1.EXAM_TYPE AND W4.RECEPTNO = W1.RECEPTNO ");
*/
            //stb.append(        "INNER JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(        "LEFT JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                "W4.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                "W4.EXAM_TYPE = W1.EXAM_TYPE AND ");
            stb.append(                                                "W4.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                "W4.RECEPTNO = W1.RECEPTNO AND ");
            stb.append(                                                "W4.TESTSUBCLASSCD IN ('1','2') ");

            stb.append("WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(        "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(        "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(        "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.受験者区分
            stb.append(        "W1.TOTAL2 IS NOT NULL AND ");                 //志願者受付データ.２科目合計

            stb.append(        "W2.TESTDIV = '" + param[2] + "'  ");          //志願者データ.入試区分
            stb.append("GROUP BY W3.DESIREDIV,TESTSUBCLASSCD ");

            stb.append("ORDER BY SUBCLASSCD DESC ");

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 結果表
     */
	String statementKekkahyoShaRi()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            //４科目（社・理）
            stb.append("SELECT  TESTSUBCLASSCD AS SUBCLASSCD, ");
            stb.append(        "MAX(SCORE) AS MAXSCORE, ");
            stb.append(        "MIN(SCORE) AS MINSCORE, ");
            stb.append(        "ROUND(AVG(FLOAT(SCORE))*10,0)/10 AS AVERAGE, ");
            stb.append(        "SUM(CASE ATTEND_FLG WHEN '1' THEN 1 ELSE 0 END) AS PRESENT, ");
            stb.append(        "0 AS PRESENT_TYPE, ");
            stb.append(        "SUM(CASE ATTEND_FLG WHEN '0' THEN 1 ELSE 0 END) AS ABSENT, ");
            stb.append(        "0 AS ABSENT_TYPE, ");
                                /* 受験していない欠席者数 */
            stb.append(        "0 AS ABSENT_NOTRECEPT, ");
            stb.append(        "0 AS ABSENT_NOTRECEPT_TYPE ");

            stb.append("FROM    ENTEXAM_DESIRE_DAT W2 ");
            stb.append(        "INNER JOIN ENTEXAM_RECEPT_DAT W1 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(        "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");
/*
            stb.append(        "INNER JOIN (");
            stb.append(        "SELECT  SCORE, ");
            stb.append(                "ATTEND_FLG, ");
            stb.append(                "TESTSUBCLASSCD, ");
            stb.append(                "EXAM_TYPE,");
            stb.append(                "RECEPTNO ");
            stb.append(        "FROM    ENTEXAM_SCORE_DAT W4 ");
            stb.append(        "WHERE   W4.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(                "W4.APPLICANTDIV = '" + param[1] + "' AND ");
            stb.append(                "W4.TESTDIV = '" + param[2] + "' AND ");
            stb.append(                "W4.TESTSUBCLASSCD IN ('1','2') AND ");
            stb.append(                "W4.SCORE IS NOT NULL ");
            stb.append(        ")W4 ON W4.EXAM_TYPE = W1.EXAM_TYPE AND W4.RECEPTNO = W1.RECEPTNO ");
*/
            //stb.append(        "INNER JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(        "LEFT JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                "W4.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                "W4.EXAM_TYPE = W1.EXAM_TYPE AND ");
            stb.append(                                                "W4.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                "W4.RECEPTNO = W1.RECEPTNO AND ");
            stb.append(                                                "W4.TESTSUBCLASSCD IN ('3','4') ");

            stb.append("WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(        "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(        "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(        "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.受験者区分
            stb.append(        "W1.TOTAL2 IS NOT NULL AND ");                 //志願者受付データ.２科目合計
            stb.append(        "W1.TOTAL4 IS NOT NULL AND ");                 //志願者受付データ.２科目合計

            stb.append(        "W2.TESTDIV = '" + param[2] + "'  ");          //志願者データ.入試区分
            stb.append("GROUP BY W3.DESIREDIV,TESTSUBCLASSCD ");

            stb.append("ORDER BY SUBCLASSCD DESC ");

/*
            stb.append("SELECT  TESTSUBCLASSCD AS SUBCLASSCD, ");
            stb.append(        "MAX(SCORE) AS MAXSCORE, ");
            stb.append(        "MIN(SCORE) AS MINSCORE, ");
            stb.append(        "ROUND(AVG(FLOAT(SCORE))*10,0)/10 AS AVERAGE, ");
            stb.append(        "SUM(CASE ATTEND_FLG WHEN '1' THEN 1 ELSE 0 END) AS PRESENT, ");
            stb.append(        "0 AS PRESENT_TYPE, ");
            stb.append(        "SUM(CASE ATTEND_FLG WHEN '0' THEN 1 ELSE 0 END) AS ABSENT, ");
            stb.append(        "0 AS ABSENT_TYPE, ");
            stb.append(        "0 AS ABSENT_NOTRECEPT, ");
            stb.append(        "0 AS ABSENT_NOTRECEPT_TYPE ");

            stb.append("FROM    ENTEXAM_DESIRE_DAT W2 ");
            stb.append(        "INNER JOIN ENTEXAM_RECEPT_DAT W1 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(        "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");

            //stb.append(        "INNER JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(        "LEFT JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                "W4.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                "W4.EXAM_TYPE = W1.EXAM_TYPE AND ");
            stb.append(                                                "W4.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                "W4.RECEPTNO = W1.RECEPTNO AND ");
            stb.append(                                                "W4.TESTSUBCLASSCD IN ('3','4') ");

            stb.append("WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(        "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(        "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(        "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.受験者区分
            stb.append(        "W1.TOTAL2 IS NOT NULL AND ");                 //志願者受付データ.２科目合計
            stb.append(        "W1.TOTAL4 IS NOT NULL AND ");                 //志願者受付データ.４科目合計
            stb.append(        "W1.EXAM_TYPE = '2' AND ");                    //志願者受付データ.受験型(４科目型)

            stb.append(        "W2.TESTDIV = '" + param[2] + "'  ");          //志願者データ.入試区分
            stb.append("GROUP BY TESTSUBCLASSCD ");

            stb.append("ORDER BY SUBCLASSCD DESC ");
*/
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 結果表 部品 ２科目
     */
	String statementKekkahyoType2()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            //２科目
            stb.append("SELECT  'A' AS SUBCLASSCD, ");
            stb.append(        "T1.MAXSCORE, ");
            stb.append(        "T1.MINSCORE, ");
            stb.append(        "T1.AVERAGE, ");
            stb.append(        "T2.PRESENT, ");
            stb.append(        "T2.PRESENT_TYPE, ");
                                /* 欠席者数は、受験者−２科目すべて受験した者＋受験しなかった者 */
            stb.append(        "VALUE(T2.PRESENT,0) - VALUE(T1.PRESENT,0) + VALUE(T2.ABSENT,0) AS ABSENT, ");
            stb.append(        "VALUE(T2.PRESENT_TYPE,0) - VALUE(T1.PRESENT_TYPE,0) + VALUE(T2.ABSENT_TYPE,0) AS ABSENT_TYPE, ");
                                /* 受験していない欠席者数 */
            stb.append(        "T2.ABSENT AS ABSENT_NOTRECEPT, ");
            stb.append(        "T2.ABSENT_TYPE AS ABSENT_NOTRECEPT_TYPE ");
            stb.append("FROM   (");

                                /* 受験者数および受験していない欠席者数をカウント */
            stb.append(        "SELECT '" + param[2] + "' AS TESTDIV, ");
            stb.append(                "S1.PRESENT, ");
            stb.append(                "S2.ABSENT, ");
            stb.append(                "S1.PRESENT_TYPE, ");
            stb.append(                "S2.ABSENT_TYPE ");
            stb.append(        "FROM   (");
                                        /* 受験者数をカウント  志願者データの受験者区分＝１ */
            stb.append(                "SELECT  W1.TESTDIV, ");
            stb.append(                        "COUNT(*) AS PRESENT, ");
            stb.append(                        "SUM(CASE W1.EXAM_TYPE WHEN '1' THEN 1 ELSE 0 END) AS PRESENT_TYPE ");

            stb.append(                "FROM    ENTEXAM_DESIRE_DAT W2 ");
            stb.append(                        "INNER JOIN ENTEXAM_RECEPT_DAT W1 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(                        "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");
            stb.append(                "WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(                        "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(                        "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(                        "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.受験者区分
            stb.append(                        "W1.TOTAL2 IS NOT NULL AND ");                 //志願者受付データ.２科目合計
            stb.append(                        "W2.TESTDIV = '" + param[2] + "'  ");          //志願者データ.入試区分
            stb.append(                "GROUP BY W1.TESTDIV ");
            stb.append(               ")S1 ");
                                        /* 欠席者数をカウント  志願者データの受験者区分＝２ 志願者受付データ無しの場合 */
            stb.append(                "FULL JOIN(");
            stb.append(                "SELECT  W1.TESTDIV, ");            //志願者基礎データ・志願区分
            stb.append(                        "COUNT(*) AS ABSENT, ");
            stb.append(                        "SUM(CASE WHEN W1.EXAMNO BETWEEN '20000' AND '29999' THEN 1 ELSE 0 END) AS ABSENT_TYPE ");
            stb.append(                "FROM    ENTEXAM_DESIRE_DAT W1 ");
            stb.append(                        "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                                   "W3.EXAMNO = W1.EXAMNO ");
            stb.append(                "WHERE   W1.ENTEXAMYEAR = '" + param[0] + "'  AND ");
            stb.append(                        "W1.APPLICANTDIV = '" + param[1] + "' AND ");        //志願者データ.入試制度
            stb.append(                        "W1.TESTDIV = '" + param[2] + "' AND ");             //志願者データ.入試区分
            stb.append(                        "W1.APPLICANT_DIV = '1' AND ");                      //志願者データ.志願者区分
            stb.append(                        "W1.EXAMINEE_DIV = '2' ");                           //志願者データ.受験者区分
            stb.append(                "GROUP BY W1.TESTDIV ");
            stb.append(               ")S2 ON S2.TESTDIV = S1.TESTDIV ");
            stb.append(       ")T2 ");

            stb.append(        "LEFT JOIN(");
            stb.append(        "SELECT  TESTDIV, ");
            stb.append(                "MAX(TOTAL2) AS MAXSCORE, ");
            stb.append(                "MIN(TOTAL2) AS MINSCORE, ");
            stb.append(                "ROUND(AVG(FLOAT(TOTAL2))*10,0)/10 AS AVERAGE,");
            stb.append(                "COUNT(*) AS PRESENT, ");
            stb.append(                "SUM(CASE S1.EXAM_TYPE WHEN '1' THEN 1 ELSE 0 END) AS PRESENT_TYPE ");
                               /* ２科目全てを受験している者を抽出する＝＞受験者数および得点統計対象者 */
            stb.append(        "FROM   (");
            stb.append(                "SELECT  W1.TESTDIV, ");
            stb.append(                        "W1.EXAM_TYPE, ");
            stb.append(                        "MAX(TOTAL2) AS TOTAL2 ");

            stb.append(                "FROM    ENTEXAM_DESIRE_DAT W2 ");
            stb.append(                        "INNER JOIN ENTEXAM_RECEPT_DAT W1 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(                        "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");

            //stb.append(                        "INNER JOIN (");
            stb.append(                        "LEFT JOIN (");
            stb.append(                        "SELECT  EXAM_TYPE, ");
            stb.append(                                "RECEPTNO ");
            stb.append(                        "FROM    ENTEXAM_SCORE_DAT W4 ");
            stb.append(                        "WHERE   W4.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(                                "W4.APPLICANTDIV = '" + param[1] + "' AND ");
            stb.append(                                "W4.TESTDIV = '" + param[2] + "' AND ");
            stb.append(                                "W4.TESTSUBCLASSCD IN ('1','2') AND ");
            stb.append(                                "W4.SCORE IS NOT NULL ");
            stb.append(                        "GROUP BY EXAM_TYPE,RECEPTNO ");
            stb.append(                        "HAVING 1 < SUM( CASE VALUE(W4.ATTEND_FLG,'0') WHEN '1' THEN 1 ELSE 0 END )");
            stb.append(                        ")W4 ON W4.EXAM_TYPE = W1.EXAM_TYPE AND W4.RECEPTNO = W1.RECEPTNO ");
            stb.append(                "WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(                        "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(                        "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(                        "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.受験者区分
            stb.append(                        "W1.TOTAL2 IS NOT NULL AND ");                 //志願者受付データ.２科目合計
            stb.append(                        "W2.TESTDIV = '" + param[2] + "' ");           //志願者データ.入試区分
            stb.append(                "GROUP BY W1.TESTDIV, W2.EXAMNO, W1.EXAM_TYPE ");
            stb.append(                ")S1 ");
            stb.append(        "GROUP BY S1.TESTDIV ");
            stb.append(       ")T1 ON T1.TESTDIV = T2.TESTDIV ");

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 結果表 部品 ４科目
     */
	String statementKekkahyoType4()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT  T2.TESTDIV, ");           //志願者基礎データ・志願区分
            stb.append(        "'B' AS SUBCLASSCD, ");
            stb.append(        "T1.MAXSCORE, ");
            stb.append(        "T1.MINSCORE, ");
            stb.append(        "T1.AVERAGE, ");
            stb.append(        "T2.PRESENT, ");
            stb.append(        "T2.PRESENT_TYPE, ");
                                /* 欠席者数は、受験者−４科目すべて受験した者＋受験しなかった者 */
            stb.append(        "VALUE(T2.PRESENT,0) - VALUE(T1.PRESENT,0) + VALUE(T2.ABSENT,0) AS ABSENT, ");
            stb.append(        "VALUE(T2.PRESENT_TYPE,0) - VALUE(T1.PRESENT_TYPE,0) + VALUE(T2.ABSENT_TYPE,0) AS ABSENT_TYPE, ");
                                /* 受験していない欠席者数 */
            stb.append(        "T2.ABSENT AS ABSENT_NOTRECEPT, ");
            stb.append(        "T2.ABSENT_TYPE AS ABSENT_NOTRECEPT_TYPE ");
            stb.append("FROM   (");

                                /* 受験者数および受験していない欠席者数をカウント */
            stb.append(        "SELECT  VALUE(S1.TESTDIV, S2.TESTDIV) AS TESTDIV, ");
            stb.append(                "S1.PRESENT, ");
            stb.append(                "S2.ABSENT, ");
            stb.append(                "S1.PRESENT_TYPE, ");
            stb.append(                "S2.ABSENT_TYPE ");
            stb.append(        "FROM   (");
                                        /* 受験者数をカウント  志願者データの受験者区分＝１ */
            stb.append(                "SELECT  W2.TESTDIV, ");
            stb.append(                        "COUNT(*) AS PRESENT, ");
            stb.append(                        "SUM(CASE W1.EXAM_TYPE WHEN '2' THEN 1 ELSE 0 END) AS PRESENT_TYPE ");

            stb.append(                "FROM    ENTEXAM_DESIRE_DAT W2 ");
            stb.append(                        "INNER JOIN ENTEXAM_RECEPT_DAT W1 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(                        "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");
            stb.append(                "WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(                        "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(                        "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(                        "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.受験者区分
            stb.append(                        "W1.TOTAL2 IS NOT NULL AND ");                 //志願者受付データ.２科目合計
            stb.append(                        "W1.TOTAL4 IS NOT NULL AND ");                 //志願者受付データ.４科目合計
            stb.append(                        "W1.EXAM_TYPE = '2' AND ");                    //志願者受付データ.受験型(４科目型)
            stb.append(                        "W2.TESTDIV = '" + param[2] + "' ");           //志願者データ.入試区分
            stb.append(                "GROUP BY W2.TESTDIV ");
            stb.append(               ")S1 ");
                                        /* 欠席者数をカウント  志願者データの受験者区分＝２ 志願者受付データ無しの場合 */
            stb.append(                "FULL JOIN(");
            stb.append(                "SELECT  W1.TESTDIV, ");               //志願者基礎データ・志願区分
            stb.append(                        "COUNT(*) AS ABSENT, ");
            stb.append(                        "SUM(CASE WHEN W1.EXAMNO BETWEEN '40000' AND '49999' THEN 1 ELSE 0 END) AS ABSENT_TYPE ");
            stb.append(                "FROM    ENTEXAM_DESIRE_DAT W1 ");
            stb.append(                        "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                                   "W3.EXAMNO = W1.EXAMNO ");
            stb.append(                "WHERE   W1.ENTEXAMYEAR = '" + param[0] + "'  AND ");
            stb.append(                        "W1.APPLICANTDIV = '" + param[1] + "' AND ");        //志願者データ.入試制度
            stb.append(                        "W1.TESTDIV = '" + param[2] + "' AND ");             //志願者データ.入試区分
            stb.append(                        "W1.APPLICANT_DIV = '1' AND ");                      //志願者データ.志願者区分
            stb.append(                        "W1.EXAMINEE_DIV = '2' AND ");                       //志願者データ.受験者区分
            stb.append(                        "W1.EXAMNO BETWEEN '40000' AND '49999' ");
            stb.append(                "GROUP BY W1.TESTDIV ");
            stb.append(               ")S2 ON S2.TESTDIV = S1.TESTDIV ");
            stb.append(       ")T2 ");

            stb.append(        "LEFT JOIN(");
            stb.append(        "SELECT  TESTDIV, ");
            stb.append(                "MAX(TOTAL4) AS MAXSCORE, ");
            stb.append(                "MIN(TOTAL4) AS MINSCORE, ");
            stb.append(                "ROUND(AVG(FLOAT(TOTAL4))*10,0)/10 AS AVERAGE,");
            stb.append(                "COUNT(*) AS PRESENT, ");
            stb.append(                "SUM(CASE S1.EXAM_TYPE WHEN '2' THEN 1 ELSE 0 END) AS PRESENT_TYPE ");
                               /* ４科目全てを受験している者を抽出する＝＞受験者数および得点統計対象者 */
            stb.append(        "FROM   (");
            stb.append(                "SELECT  W2.TESTDIV, ");
            stb.append(                        "W1.EXAM_TYPE, ");
            stb.append(                        "MAX(TOTAL4) AS TOTAL4 ");

            stb.append(                "FROM    ENTEXAM_DESIRE_DAT W2 ");
            stb.append(                        "INNER JOIN ENTEXAM_RECEPT_DAT W1 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                  "W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append(                                                  "W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                                  "W2.EXAMNO = W1.EXAMNO ");
            stb.append(                        "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                                         "W3.EXAMNO = W1.EXAMNO ");

            //stb.append(                        "INNER JOIN (");
            stb.append(                        "LEFT JOIN (");
            stb.append(                        "SELECT  EXAM_TYPE, ");
            stb.append(                                "RECEPTNO ");
            stb.append(                        "FROM    ENTEXAM_SCORE_DAT W4 ");
            stb.append(                        "WHERE   W4.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(                                "W4.APPLICANTDIV = '" + param[1] + "' AND ");
            stb.append(                                "W4.TESTDIV = '" + param[2] + "' AND ");
            stb.append(                                "W4.TESTSUBCLASSCD IN ('1','2','3','4') AND ");
            stb.append(                                "W4.SCORE IS NOT NULL ");
            stb.append(                        "GROUP BY EXAM_TYPE,RECEPTNO ");
            stb.append(                        "HAVING 3 < SUM( CASE VALUE(W4.ATTEND_FLG,'0') WHEN '1' THEN 1 ELSE 0 END )");
            stb.append(                        ")W4 ON W4.EXAM_TYPE = W1.EXAM_TYPE AND W4.RECEPTNO = W1.RECEPTNO ");
            stb.append(                "WHERE   W2.ENTEXAMYEAR = '" + param[0] + "' AND ");
            stb.append(                        "W2.APPLICANTDIV = '" + param[1] + "' AND ");  //志願者データ.入試制度
            stb.append(                        "W2.APPLICANT_DIV = '1' AND ");                //志願者データ.志願者区分
            stb.append(                        "W2.EXAMINEE_DIV = '1' AND ");                 //志願者データ.受験者区分
            stb.append(                        "W1.TOTAL2 IS NOT NULL AND ");                 //志願者受付データ.２科目合計
            stb.append(                        "W1.TOTAL4 IS NOT NULL AND ");                 //志願者受付データ.４科目合計
            stb.append(                        "W1.EXAM_TYPE = '2' AND ");                    //志願者受付データ.受験型(４科目型)
            stb.append(                        "W2.TESTDIV = '" + param[2] + "' ");           //志願者データ.入試区分
            stb.append(                "GROUP BY W2.TESTDIV, W2.EXAMNO, W1.EXAM_TYPE ");
            stb.append(                ")S1 ");
            stb.append(        "GROUP BY TESTDIV ");
            stb.append(       ")T1 ON T1.TESTDIV = T2.TESTDIV ");


		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


}//クラスの括り
