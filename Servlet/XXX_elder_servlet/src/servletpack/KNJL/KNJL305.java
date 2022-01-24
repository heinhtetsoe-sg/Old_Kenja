/**
 *
 *	学校教育システム 賢者 [入試処理]  
 *
 *
 */

package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJL305 {

    private static final Log log = LogFactory.getLog(KNJL305.class);

    private boolean _hasData;
    private int pageno;
    private int totalpageno;
    private int totalnum[];
    private int outcount;
    private StringBuffer stb = new StringBuffer();

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }


	/**
     *  svf print 印刷処理 
     */
    private void printMain(DB2UDB db2, Vrw32alp svf)
	{
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;

        try {
            ps3 = db2.prepareStatement( statementMeiboTotalPage() );
            ps2 = db2.prepareStatement( statementTestDivMst() );
            printHead(db2, svf, ps2);		                        //見出し項目のセット＆出力
            ps1 = db2.prepareStatement( statementMeiboMeisai() );
            printMeibo(db2, svf, ps1, ps3);                        	//名簿のセット＆出力
        } catch( Exception ex ){
            log.error("printMain error!",ex);
        } finally{
            if( _hasData ) svf.VrPrint();
            try {
                if( ps1 != null ) ps1.close();
                if( ps2 != null ) ps2.close();
                if( ps3 != null ) ps3.close();
            } catch( Exception ex ){
                log.error("printMain error!",ex);
            }
        }

    }


	/**
     *  svf print 見出し印刷処理
     */
    private void printHead(DB2UDB db2, Vrw32alp svf, PreparedStatement ps2)
	{
		ResultSet rs = null;

        String nendo = "";
        try {
            svf.VrSetForm("KNJL305.frm", 4);
// 			svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度");
 	        nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度";
        } catch( Exception ex ){
            log.error("printHead error!",ex);
        }

	//	作成日(現在処理日)
		try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();		//各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;		//各情報を返すためのクラス
			returnval = getinfo.Control(db2);
			svf.VrsOut("DATE",   KNJ_EditDate.h_format_JP(db2, returnval.val3));
		} catch( Exception ex ){
			log.warn("ymd1 svf-out error!",ex);
		}

        //入試区分
		String testdivName = "";
		try{
			ps2.setString( 1, _param._entexamyear );
			ps2.setString( 2, _param._testdiv );
			rs = ps2.executeQuery();
			if ( rs.next()  &&  rs.getString(1) != null ) {
//			    svf.VrsOut("TESTDIV",  rs.getString(1) );
			    testdivName = rs.getString(1);
			}
        } catch( Exception ex ){
            log.error("printHead error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
        }

        //タイトル
		svf.VrsOut("TITLE",  nendo + "　" + testdivName + "　午後受験対象者一覧表");
/* *********************************
        //総ページ数
		try{
			rs = ps1.executeQuery();
            if( rs.next() ) totalpageno = rs.getInt("COUNT");
		} catch( Exception ex ){
			log.error("printMeibo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
		}
************************************ */
   }


	/**
     *  svf print 名簿明細印刷処理
     */
    private void printMeibo(DB2UDB db2, Vrw32alp svf, PreparedStatement ps1, PreparedStatement ps3)
	{
		ResultSet rs = null;
		try{
			rs = ps1.executeQuery();
            String exam_type_hall = null;
            totalnum = new int[3];

			while( rs.next() ){
                //会場のブレイク
                if( exam_type_hall == null  ||  ! exam_type_hall.equals( rs.getString("EXAM_TYPE_HALL") ) ){
                    if( exam_type_hall != null ){
                        for( ; 0 < outcount  &&  outcount < 40 ; outcount++ ) svf.VrEndRecord();
                        outcount = 0;
                    }
                    printsvfKaijyou(db2, svf, rs, ps3);
                    exam_type_hall = rs.getString("EXAM_TYPE_HALL");
                } else{
                    if( 40 == outcount ){
                        outcount = 0;
                        svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );       //ページ
                    }
                }
                if( "1".equals(rs.getString("SEX")) ) totalnum[0]++;
                if( "2".equals(rs.getString("SEX")) ) totalnum[1]++;
                totalnum[2]++;
                printsvfMeibo(svf, rs);
			}
		} catch( Exception ex ){
			log.error("printMeibo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
		}
    }


	/**
     *  svf print 名簿明細出力
     */
    private void printsvfMeibo(Vrw32alp svf, ResultSet rs)
	{
		try{
            svf.VrsOut("SELECT", rs.getString("TARGET_MARU")); //対象者
            svf.VrsOut("RECEPTNO", rs.getString("RECEPTNO")); //受付番号
	 		svf.VrsOut("EXAMNO", rs.getString("EXAMNO")); //受験番号
	 		svf.VrsOut("NAME", rs.getString("NAME")); //氏名
	 		svf.VrsOut("KANA", rs.getString("NAME_KANA")); //フリガナ
            svf.VrsOut("SEX", rs.getString("SEXNAME")); //性別
            printTotalNum(svf);		    //人数セット＆出力
            svf.VrEndRecord();
            _hasData = true;
            outcount++;
		} catch( Exception ex ){
			log.error("printMeiboSvf error!",ex);
		}
	}


	/**
     *  svf print 会場名出力
     *            2004/12/27 会場ごとの総ページ数をココへ移動
     */
    private void printsvfKaijyou(DB2UDB db2, Vrw32alp svf, ResultSet rs1, PreparedStatement ps3)
	{
        //総ページ数
        ResultSet rs = null;
		try{
			rs = ps3.executeQuery();
            if( rs.next() ) totalpageno = rs.getInt("COUNT");
		} catch( Exception ex ){
			log.error("printMeibo error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
            db2.commit();
		}

		try{
	 		svf.VrsOut( "EXAM_PLACE",  ( rs1.getString("EXAMHALL_NAME") != null )? rs1.getString("EXAMHALL_NAME") : ""  ); //会場名
	 		svf.VrsOut( "PAGE",        String.valueOf( ++pageno )  );       //ページ
	 		svf.VrsOut( "TOTAL_PAGE",  String.valueOf( totalpageno )  );    //総ページ
		} catch( Exception ex ){
			log.error("printsvfKaijyou error!",ex);
		}

        for( int i = 0 ; i < totalnum.length ; i++ ) totalnum[i] = 0;
	}


	/**
     *  svf print 最終人数出力
     */
    private void printTotalNum(Vrw32alp svf)
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
     *  preparedstatement 名簿、総ページ数STATEMENT共通部分
     */
	private String statementMeiboCommon()
	{
        StringBuffer stb = new StringBuffer();
		try{
            stb.append("FROM   ENTEXAM_RECEPT_DAT W1 ");
            stb.append(       "INNER JOIN ENTEXAM_HALL_DAT W2 ON W2.TESTDIV = W1.TESTDIV AND ");
            stb.append(                                         "W2.EXAM_TYPE = W1.EXAM_TYPE AND ");
            stb.append(                                         "W2.EXAM_TYPE || W2.EXAMHALLCD IN " + _param._hallIn + " AND ");
            stb.append(                                         "W1.RECEPTNO BETWEEN W2.S_RECEPTNO AND W2.E_RECEPTNO ");
            stb.append(       "INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(                                         "W3.EXAMNO = W1.EXAMNO ");
            stb.append(       "LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = W3.SEX ");
            stb.append(       "LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD ON RD.ENTEXAMYEAR = W1.ENTEXAMYEAR ");
            stb.append(                                         "AND RD.APPLICANTDIV = W1.APPLICANTDIV ");
            stb.append(                                         "AND RD.TESTDIV = W1.TESTDIV ");
            stb.append(                                         "AND RD.EXAM_TYPE = W1.EXAM_TYPE ");
            stb.append(                                         "AND RD.RECEPTNO = W1.RECEPTNO ");
            stb.append(                                         "AND RD.SEQ = '010' ");
            stb.append("WHERE  W1.ENTEXAMYEAR = '" + _param._entexamyear + "' AND ");

            if( Integer.parseInt(_param._applicantdiv) != 0  )                                  //入試制度(全て：_param._applicantdiv == 0)
                stb.append(   "W1.APPLICANTDIV = '" + _param._applicantdiv + "' AND ");

            if( Integer.parseInt(_param._examType) != 0  )                                  //受験型(全て：_param._examType == 0)
                stb.append(   "W1.EXAM_TYPE = '" + _param._examType + "' AND ");

            stb.append(       "W1.TESTDIV = '" + _param._testdiv + "' ");                  //入試区分

            if ("2".equals(_param._targetDiv)) {
                stb.append(       "AND RD.REMARK1 = '1' ");                         //対象者（_param._targetDiv == 1:全て、2:対象者のみ）
            }
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 受験者名簿
     */
	private String statementMeiboMeisai()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT W1.APPLICANTDIV, ");
            stb.append(       "W1.EXAM_TYPE || W2.EXAMHALLCD AS EXAM_TYPE_HALL, ");
            stb.append(       "W1.TESTDIV, ");
            stb.append(       "W1.EXAM_TYPE, ");
            stb.append(       "W1.RECEPTNO, ");
            stb.append(       "W1.EXAMNO, ");
            stb.append(       "W2.EXAMHALLCD, ");
            stb.append(       "W2.EXAMHALL_NAME, ");
            stb.append(       "CASE WHEN RD.REMARK1 = '1' THEN '〇' ELSE '' END AS TARGET_MARU, ");
            stb.append(       "W3.NAME, ");
            stb.append(       "W3.NAME_KANA, ");
            stb.append(       "W3.SEX, ");
            stb.append(       "N1.NAME1 AS SEXNAME ");

            stb.append( statementMeiboCommon() );   //共通部分

            stb.append("ORDER BY W1.EXAM_TYPE, W2.EXAMHALLCD, W1.RECEPTNO ");

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
        log.info("sql statementMeiboMeisai = " + stb.toString() );
		return stb.toString();
	}


	/**
     *  preparedstatement 総ページ数
     *                    2004/12/27 会場ごとの総頁に変更
     */
	private String statementMeiboTotalPage()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("WITH PAGES AS( ");
            stb.append("SELECT CASE WHEN 0 < MOD(COUNT(*),40) THEN COUNT(*)/40 + 1 ELSE COUNT(*)/40 END AS COUNT ");
            stb.append( statementMeiboCommon() );   //共通部分
            stb.append("GROUP BY ");
            stb.append("    W2.EXAM_TYPE || W2.EXAMHALLCD ");
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    SUM(COUNT) AS COUNT ");
            stb.append("FROM ");
            stb.append("    PAGES ");

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  preparedstatement 入試区分マスターから名称を取得
     */
	private String statementTestDivMst()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT NAME AS NAME1 FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = ? AND TESTDIV = ? ");
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71043 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _examType;
        final String _targetDiv;
        final String _hallIn;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");    //入試年度
            _applicantdiv = request.getParameter("APDIV");  //入試制度(全て：0)
            _testdiv = request.getParameter("TESTDV");      //入試区分
            _examType = request.getParameter("EXAM_TYPE");  //受験型(全て：0)
            _targetDiv = request.getParameter("TARGET_DIV");//対象者（1:全て、2:対象者のみ）

            String hall[] = request.getParameterValues("category_name");//会場
            stb.delete(0,stb.length());
            stb.append("(");
            for( int i=0 ; i<hall.length ; i++ ){
                if( i > 0 ) stb.append(",");
                stb.append("'").append(hall[i]).append("'");
            }
            stb.append(")");
            _hallIn = stb.toString();//対象会場(カンマで接続)
        }

    }

}//クラスの括り
