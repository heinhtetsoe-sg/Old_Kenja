package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * 入試結果集計１
 * 
 * @author nakasone
 *
 */
public class KNJL375J {
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL375J.class);
    
    /**
     * KNJL.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
    	
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            
            _hasData = printMain(db2, svf);
            
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            close(db2, svf);
        }

    }

    /**
     * 印刷処理メイン
     * @param db2	ＤＢ接続オブジェクト
     * @param svf	帳票オブジェクト
     * @throws Exception
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        
    	boolean refflg = false;
        final String formName = (_param._infuruFlg) ? "KNJL375J_2.frm" : "KNJL375J.frm";
        svf.VrSetForm(formName, 1);

        // 受験コースマスタより定員数を取得
        _param._hcapacity = getCapacity(db2);

        final int testDivCase = (_param._infuruFlg) ? 6 : 5;
        for(int i=1 ; i<=testDivCase ; i++){
           	// 入試結果集計１データ取得
       		final List student = createStudents(db2, String.valueOf(i));
       		// 帳票出力のメソッド
            if (_param._infuruFlg) {
                outPutPrintInfuru(svf, student, String.valueOf(i));
            } else {
                outPutPrint(svf, student, String.valueOf(i));
            }
        }
        
        // 帳票出力データが存在する場合
        if(_param._recordTotalCount > 0){
        	refflg = true;
        }

		// 実人数を出力
		if (refflg) {
			// 総合計値を設定
            final int maxGyou = (_param._infuruFlg) ? 45 : 31;
			svf.VrsOutn("COUNT2" 	, maxGyou, String.valueOf(_param._siganTotalCount));
			svf.VrsOutn("COUNT3" 	, maxGyou, String.valueOf(_param._examTotalCount));
			svf.VrsOutn("COUNT4" 	, maxGyou, String.valueOf(_param._passTotalCount));

			// 実人数を取得
			final List totalrec = getTotalCount(db2);
            for (Iterator it = totalrec.iterator(); it.hasNext();) {
                final totalrec total = (totalrec) it.next();
    			// 実人数(男)
    			svf.VrsOutn("COUNT1" 	, 1, total._total_mancnt);
    			// 実人数(女)
    			svf.VrsOutn("COUNT1" 	, 2, total._total_womancnt);
    			// 実人数(計)
    			svf.VrsOutn("COUNT1" 	, 3, total._total_cnt);
            }
            svf.VrEndPage();
		}
		return 	refflg;
    }
    
    /**
     * 帳票出力処理
     * @param svf			帳票オブジェクト
     * @param student		帳票出力対象クラスオブジェクト
     * @param test_div		入試区分
     */
    private void outPutPrint(final Vrw32alp svf, final List student, final String testDiv) {
        
    	int irow = 0;

    	// 開始行数を取得
    	if(testDiv.equals("1")){
    		irow = 1;
    	} else if(testDiv.equals("2")){
    		irow = 10;
    	} else if(testDiv.equals("3")){
    		irow = 14;
    	} else if(testDiv.equals("4")){
    		irow = 23;
    	} else if(testDiv.equals("5")){
    		irow = 27;
    	}
    	
		//*================*
		//* ヘッダ         *
		//*================*
		svf.VrsOut("NENDO" 		,_param._nendo);	// 対象年度
		svf.VrsOut("DATE"	 	,_param._date);		// 作成日
        
		//*================*
		//* 明細           *
		//*================*
		svf.VrsOut("TEST_COUNT"+testDiv 	, (String)_param._hcapacity.get(testDiv));// 定員

        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();


			if(testDiv.equals("1") || testDiv.equals("2") || testDiv.equals("3")){
    			//*<【２科】>*
    			svf.VrsOutn("COUNT2" 	, irow,		sudent._sigan_cnt_2_man);	// 志願者数(男)
    			svf.VrsOutn("COUNT2" 	, irow+1,	sudent._sigan_cnt_2_woman);	// 志願者数(女)
    			svf.VrsOutn("COUNT2" 	, irow+2,	sudent._sigan_cnt_2_total);	// 志願者数(計)
    			svf.VrsOutn("COUNT3" 	, irow,		sudent._exam_cnt_2_man);	// 受験者数(男)
    			svf.VrsOutn("COUNT3" 	, irow+1,	sudent._exam_cnt_2_woman);	// 受験者数(女)
    			svf.VrsOutn("COUNT3" 	, irow+2,	sudent._exam_cnt_2_total);	// 受験者数(計)
    			svf.VrsOutn("COUNT4" 	, irow,		sudent._pass_cnt_2_man);	// 合格者数(男)
    			svf.VrsOutn("COUNT4" 	, irow+1,	sudent._pass_cnt_2_woman);	// 合格者数(女)
    			svf.VrsOutn("COUNT4" 	, irow+2,	sudent._pass_cnt_2_total);	// 合格者数(計)
        	}
			if(testDiv.equals("5")){
				//*<帰国生>*
				svf.VrsOutn("COUNT2" 	, irow, 	sudent._sigan_cnt_3_man);	// 志願者数(男)
				svf.VrsOutn("COUNT2" 	, irow+1, 	sudent._sigan_cnt_3_woman);	// 志願者数(女)
				svf.VrsOutn("COUNT2" 	, irow+2, 	sudent._sigan_cnt_3_total);	// 志願者数(計)
				svf.VrsOutn("COUNT3" 	, irow, 	sudent._exam_cnt_3_man);	// 受験者数(男)
				svf.VrsOutn("COUNT3" 	, irow+1, 	sudent._exam_cnt_3_woman);	// 受験者数(女)
				svf.VrsOutn("COUNT3" 	, irow+2, 	sudent._exam_cnt_3_total);	// 受験者数(計)
				svf.VrsOutn("COUNT4" 	, irow, 	sudent._pass_cnt_3_man);	// 合格者数(男)
				svf.VrsOutn("COUNT4" 	, irow+1, 	sudent._pass_cnt_3_woman);	// 合格者数(女)
				svf.VrsOutn("COUNT4" 	, irow+2, 	sudent._pass_cnt_3_total);	// 合格者数(計)
			}
			if(testDiv.equals("1") || testDiv.equals("3")){
				//*<【４科】>*
				svf.VrsOutn("COUNT2" 	, irow+3, sudent._sigan_cnt_4_man);		// 志願者数(男)
				svf.VrsOutn("COUNT2" 	, irow+4, sudent._sigan_cnt_4_woman);	// 志願者数(女)
				svf.VrsOutn("COUNT2" 	, irow+5, sudent._sigan_cnt_4_total);	// 志願者数(計)
				svf.VrsOutn("COUNT3" 	, irow+3, sudent._exam_cnt_4_man);		// 受験者数(男)
				svf.VrsOutn("COUNT3" 	, irow+4, sudent._exam_cnt_4_woman);	// 受験者数(女)
				svf.VrsOutn("COUNT3" 	, irow+5, sudent._exam_cnt_4_total);	// 受験者数(計)
				svf.VrsOutn("COUNT4" 	, irow+3, sudent._pass_cnt_4_man);		// 合格者数(男)
				svf.VrsOutn("COUNT4" 	, irow+4, sudent._pass_cnt_4_woman);	// 合格者数(女)
				svf.VrsOutn("COUNT4" 	, irow+5, sudent._pass_cnt_4_total);	// 合格者数(計)
			}
			
			if(testDiv.equals("4")){
				//*<【４科】>*
				svf.VrsOutn("COUNT2" 	, irow, sudent._sigan_cnt_4_man);		// 志願者数(男)
				svf.VrsOutn("COUNT2" 	, irow+1, sudent._sigan_cnt_4_woman);	// 志願者数(女)
				svf.VrsOutn("COUNT2" 	, irow+2, sudent._sigan_cnt_4_total);	// 志願者数(計)
				svf.VrsOutn("COUNT3" 	, irow, sudent._exam_cnt_4_man);		// 受験者数(男)
				svf.VrsOutn("COUNT3" 	, irow+1, sudent._exam_cnt_4_woman);	// 受験者数(女)
				svf.VrsOutn("COUNT3" 	, irow+2, sudent._exam_cnt_4_total);	// 受験者数(計)
				svf.VrsOutn("COUNT4" 	, irow, sudent._pass_cnt_4_man);		// 合格者数(男)
				svf.VrsOutn("COUNT4" 	, irow+1, sudent._pass_cnt_4_woman);	// 合格者数(女)
				svf.VrsOutn("COUNT4" 	, irow+2, sudent._pass_cnt_4_total);	// 合格者数(計)
			}
			//*<【計】>*
			if(testDiv.equals("1") || testDiv.equals("3")){
				svf.VrsOutn("COUNT2" 	, irow+6, sumVal(sudent._sigan_cnt_2_man, sudent._sigan_cnt_4_man));
				svf.VrsOutn("COUNT2" 	, irow+7, sumVal(sudent._sigan_cnt_2_woman, sudent._sigan_cnt_4_woman));
				svf.VrsOutn("COUNT2" 	, irow+8, sudent._sigan_cnt_total);
				svf.VrsOutn("COUNT3" 	, irow+6, sumVal(sudent._exam_cnt_2_man, sudent._exam_cnt_4_man));
				svf.VrsOutn("COUNT3" 	, irow+7, sumVal(sudent._exam_cnt_2_woman, sudent._exam_cnt_4_woman));
				svf.VrsOutn("COUNT3" 	, irow+8, sudent._exam_cnt_total);
				svf.VrsOutn("COUNT4" 	, irow+6, sumVal(sudent._pass_cnt_2_man, sudent._pass_cnt_4_man));
				svf.VrsOutn("COUNT4" 	, irow+7, sumVal(sudent._pass_cnt_2_woman, sudent._pass_cnt_4_woman));
				svf.VrsOutn("COUNT4" 	, irow+8, sudent._pass_cnt_total);
				// 総合計値を加算
				_param._siganTotalCount += Integer.parseInt(sudent._sigan_cnt_total);            
				_param._examTotalCount += Integer.parseInt(sudent._exam_cnt_total);            
				_param._passTotalCount += Integer.parseInt(sudent._pass_cnt_total);            
			}
			if(testDiv.equals("2")){
				svf.VrsOutn("COUNT2" 	, irow+3, sudent._sigan_cnt_2_total);
				svf.VrsOutn("COUNT3" 	, irow+3, sudent._exam_cnt_2_total);
				svf.VrsOutn("COUNT4" 	, irow+3, sudent._pass_cnt_2_total);
				// 総合計値を加算
				_param._siganTotalCount += Integer.parseInt(sudent._sigan_cnt_2_total);            
				_param._examTotalCount += Integer.parseInt(sudent._exam_cnt_2_total);            
				_param._passTotalCount += Integer.parseInt(sudent._pass_cnt_2_total);            
			}
			if(testDiv.equals("4")){
				svf.VrsOutn("COUNT2" 	, irow+3, sudent._sigan_cnt_4_total);
				svf.VrsOutn("COUNT3" 	, irow+3, sudent._exam_cnt_4_total);
				svf.VrsOutn("COUNT4" 	, irow+3, sudent._pass_cnt_4_total);
				// 総合計値を加算
				_param._siganTotalCount += Integer.parseInt(sudent._sigan_cnt_4_total);            
				_param._examTotalCount += Integer.parseInt(sudent._exam_cnt_4_total);            
				_param._passTotalCount += Integer.parseInt(sudent._pass_cnt_4_total);            
			}
			if(testDiv.equals("5")){
				svf.VrsOutn("COUNT2" 	, irow+3, sudent._sigan_cnt_3_total);
				svf.VrsOutn("COUNT3" 	, irow+3, sudent._exam_cnt_3_total);
				svf.VrsOutn("COUNT4" 	, irow+3, sudent._pass_cnt_3_total);
				// 総合計値を加算
				_param._siganTotalCount += Integer.parseInt(sudent._sigan_cnt_3_total);            
				_param._examTotalCount += Integer.parseInt(sudent._exam_cnt_3_total);            
				_param._passTotalCount += Integer.parseInt(sudent._pass_cnt_3_total);            
			}

			if(Integer.parseInt(sudent._sigan_cnt_total) > 0){
				++_param._recordTotalCount;
			}
        }
    }
    
    /**
     * 帳票出力処理（インフルエンザ）
     * @param svf           帳票オブジェクト
     * @param student       帳票出力対象クラスオブジェクト
     * @param test_div      入試区分
     */
    private void outPutPrintInfuru(final Vrw32alp svf, final List student, final String testDiv) {
        
        int irow = 0;

        // 開始行数を取得
        if(testDiv.equals("1")){
            irow = 1;
        } else if(testDiv.equals("2")){
            irow = 10;
        } else if(testDiv.equals("3")){
            irow = 14;
        } else if(testDiv.equals("4")){
            irow = 23;
        } else if(testDiv.equals("6")){
            irow = 32;
        } else if(testDiv.equals("5")){
            irow = 41;
        }
        
        //*================*
        //* ヘッダ         *
        //*================*
        svf.VrsOut("NENDO"      ,_param._nendo);    // 対象年度
        svf.VrsOut("DATE"       ,_param._date);     // 作成日
        
        //*================*
        //* 明細           *
        //*================*
        svf.VrsOut("TEST_COUNT"+testDiv     , (String)_param._hcapacity.get(testDiv));// 定員

        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();


            if(testDiv.equals("1") || testDiv.equals("2") || testDiv.equals("3") || testDiv.equals("4") || testDiv.equals("6")){
                //*<【２科】>*
                svf.VrsOutn("COUNT2"    , irow,     sudent._sigan_cnt_2_man);   // 志願者数(男)
                svf.VrsOutn("COUNT2"    , irow+1,   sudent._sigan_cnt_2_woman); // 志願者数(女)
                svf.VrsOutn("COUNT2"    , irow+2,   sudent._sigan_cnt_2_total); // 志願者数(計)
                svf.VrsOutn("COUNT3"    , irow,     sudent._exam_cnt_2_man);    // 受験者数(男)
                svf.VrsOutn("COUNT3"    , irow+1,   sudent._exam_cnt_2_woman);  // 受験者数(女)
                svf.VrsOutn("COUNT3"    , irow+2,   sudent._exam_cnt_2_total);  // 受験者数(計)
                svf.VrsOutn("COUNT4"    , irow,     sudent._pass_cnt_2_man);    // 合格者数(男)
                svf.VrsOutn("COUNT4"    , irow+1,   sudent._pass_cnt_2_woman);  // 合格者数(女)
                svf.VrsOutn("COUNT4"    , irow+2,   sudent._pass_cnt_2_total);  // 合格者数(計)
            }
            if(testDiv.equals("5")){
                //*<帰国生>*
                svf.VrsOutn("COUNT2"    , irow,     sudent._sigan_cnt_3_man);   // 志願者数(男)
                svf.VrsOutn("COUNT2"    , irow+1,   sudent._sigan_cnt_3_woman); // 志願者数(女)
                svf.VrsOutn("COUNT2"    , irow+2,   sudent._sigan_cnt_3_total); // 志願者数(計)
                svf.VrsOutn("COUNT3"    , irow,     sudent._exam_cnt_3_man);    // 受験者数(男)
                svf.VrsOutn("COUNT3"    , irow+1,   sudent._exam_cnt_3_woman);  // 受験者数(女)
                svf.VrsOutn("COUNT3"    , irow+2,   sudent._exam_cnt_3_total);  // 受験者数(計)
                svf.VrsOutn("COUNT4"    , irow,     sudent._pass_cnt_3_man);    // 合格者数(男)
                svf.VrsOutn("COUNT4"    , irow+1,   sudent._pass_cnt_3_woman);  // 合格者数(女)
                svf.VrsOutn("COUNT4"    , irow+2,   sudent._pass_cnt_3_total);  // 合格者数(計)
            }
            if(testDiv.equals("1") || testDiv.equals("3") || testDiv.equals("4") || testDiv.equals("6")){
                //*<【４科】>*
                svf.VrsOutn("COUNT2"    , irow+3, sudent._sigan_cnt_4_man);     // 志願者数(男)
                svf.VrsOutn("COUNT2"    , irow+4, sudent._sigan_cnt_4_woman);   // 志願者数(女)
                svf.VrsOutn("COUNT2"    , irow+5, sudent._sigan_cnt_4_total);   // 志願者数(計)
                svf.VrsOutn("COUNT3"    , irow+3, sudent._exam_cnt_4_man);      // 受験者数(男)
                svf.VrsOutn("COUNT3"    , irow+4, sudent._exam_cnt_4_woman);    // 受験者数(女)
                svf.VrsOutn("COUNT3"    , irow+5, sudent._exam_cnt_4_total);    // 受験者数(計)
                svf.VrsOutn("COUNT4"    , irow+3, sudent._pass_cnt_4_man);      // 合格者数(男)
                svf.VrsOutn("COUNT4"    , irow+4, sudent._pass_cnt_4_woman);    // 合格者数(女)
                svf.VrsOutn("COUNT4"    , irow+5, sudent._pass_cnt_4_total);    // 合格者数(計)
            }
            
            //*<【計】>*
            if(testDiv.equals("1") || testDiv.equals("3") || testDiv.equals("4") || testDiv.equals("6")){
                svf.VrsOutn("COUNT2"    , irow+6, sumVal(sudent._sigan_cnt_2_man, sudent._sigan_cnt_4_man));
                svf.VrsOutn("COUNT2"    , irow+7, sumVal(sudent._sigan_cnt_2_woman, sudent._sigan_cnt_4_woman));
                svf.VrsOutn("COUNT2"    , irow+8, sudent._sigan_cnt_total);
                svf.VrsOutn("COUNT3"    , irow+6, sumVal(sudent._exam_cnt_2_man, sudent._exam_cnt_4_man));
                svf.VrsOutn("COUNT3"    , irow+7, sumVal(sudent._exam_cnt_2_woman, sudent._exam_cnt_4_woman));
                svf.VrsOutn("COUNT3"    , irow+8, sudent._exam_cnt_total);
                svf.VrsOutn("COUNT4"    , irow+6, sumVal(sudent._pass_cnt_2_man, sudent._pass_cnt_4_man));
                svf.VrsOutn("COUNT4"    , irow+7, sumVal(sudent._pass_cnt_2_woman, sudent._pass_cnt_4_woman));
                svf.VrsOutn("COUNT4"    , irow+8, sudent._pass_cnt_total);
                // 総合計値を加算
                _param._siganTotalCount += Integer.parseInt(sudent._sigan_cnt_total);
                _param._examTotalCount += Integer.parseInt(sudent._exam_cnt_total);
                _param._passTotalCount += Integer.parseInt(sudent._pass_cnt_total);
            }
            if(testDiv.equals("2")){
                svf.VrsOutn("COUNT2"    , irow+3, sudent._sigan_cnt_2_total);
                svf.VrsOutn("COUNT3"    , irow+3, sudent._exam_cnt_2_total);
                svf.VrsOutn("COUNT4"    , irow+3, sudent._pass_cnt_2_total);
                // 総合計値を加算
                _param._siganTotalCount += Integer.parseInt(sudent._sigan_cnt_2_total);
                _param._examTotalCount += Integer.parseInt(sudent._exam_cnt_2_total);
                _param._passTotalCount += Integer.parseInt(sudent._pass_cnt_2_total);
            }
            if(testDiv.equals("5")){
                svf.VrsOutn("COUNT2"    , irow+3, sudent._sigan_cnt_3_total);
                svf.VrsOutn("COUNT3"    , irow+3, sudent._exam_cnt_3_total);
                svf.VrsOutn("COUNT4"    , irow+3, sudent._pass_cnt_3_total);
                // 総合計値を加算
                _param._siganTotalCount += Integer.parseInt(sudent._sigan_cnt_3_total);
                _param._examTotalCount += Integer.parseInt(sudent._exam_cnt_3_total);
                _param._passTotalCount += Integer.parseInt(sudent._pass_cnt_3_total);
            }

            if(Integer.parseInt(sudent._sigan_cnt_total) > 0){
                ++_param._recordTotalCount;
            }
        }
    }
    
    /**
     * @param db2				ＤＢ接続オブジェクト
     * @param test_div			入試区分
     * @return					帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudents(final DB2UDB db2,	final String test_div)
    	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getStudentSql(test_div);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final student student = new student(
                		rs.getString("SIGAN_CNT_2_MAN"),
                		rs.getString("SIGAN_CNT_2_WOMAN"),
                		rs.getString("SIGAN_CNT_2_TOTAL"),
                		rs.getString("SIGAN_CNT_4_MAN"),
                		rs.getString("SIGAN_CNT_4_WOMAN"),
                		rs.getString("SIGAN_CNT_4_TOTAL"),
                		rs.getString("SIGAN_CNT_3_MAN"),
                		rs.getString("SIGAN_CNT_3_WOMAN"),
                		rs.getString("SIGAN_CNT_3_TOTAL"),
                		rs.getString("SIGAN_CNT_TOTAL"),
                		rs.getString("EXAM_CNT_2_MAN"),
                		rs.getString("EXAM_CNT_2_WOMAN"),
                		rs.getString("EXAM_CNT_2_TOTAL"),
                		rs.getString("EXAM_CNT_4_MAN"),
                		rs.getString("EXAM_CNT_4_WOMAN"),
                		rs.getString("EXAM_CNT_4_TOTAL"),
                		rs.getString("EXAM_CNT_3_MAN"),
                		rs.getString("EXAM_CNT_3_WOMAN"),
                		rs.getString("EXAM_CNT_3_TOTAL"),
                		rs.getString("EXAM_CNT_TOTAL"),
                		rs.getString("PASS_CNT_2_MAN"),
                		rs.getString("PASS_CNT_2_WOMAN"),
                		rs.getString("PASS_CNT_2_TOTAL"),
                		rs.getString("PASS_CNT_4_MAN"),
                		rs.getString("PASS_CNT_4_WOMAN"),
                		rs.getString("PASS_CNT_4_TOTAL"),
                		rs.getString("PASS_CNT_3_MAN"),
                		rs.getString("PASS_CNT_3_WOMAN"),
                		rs.getString("PASS_CNT_3_TOTAL"),
                		rs.getString("PASS_CNT_TOTAL")
                );
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @param test_div		入試区分
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql(final String test_div){
		StringBuffer stb = new StringBuffer();
		
		stb.append("select ");
		// 志願者数
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='1' and W3.SEX='1' THEN '1' END) AS SIGAN_CNT_2_MAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='1' and W3.SEX='2' THEN '1' END) AS SIGAN_CNT_2_WOMAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='1' THEN '1' END) AS SIGAN_CNT_2_TOTAL,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='2' and W3.SEX='1' THEN '1' END) AS SIGAN_CNT_4_MAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='2' and W3.SEX='2' THEN '1' END) AS SIGAN_CNT_4_WOMAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='2' THEN '1' END) AS SIGAN_CNT_4_TOTAL,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='3' and W3.SEX='1' THEN '1' END) AS SIGAN_CNT_3_MAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='3' and W3.SEX='2' THEN '1' END) AS SIGAN_CNT_3_WOMAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='3' THEN '1' END) AS SIGAN_CNT_3_TOTAL,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' THEN '1' END) AS SIGAN_CNT_TOTAL,");
		// 受験者数
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='1' and W1.EXAMINEE_DIV='1' and W3.SEX='1' THEN '1' END) AS EXAM_CNT_2_MAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='1' and W1.EXAMINEE_DIV='1' and W3.SEX='2' THEN '1' END) AS EXAM_CNT_2_WOMAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='1' and W1.EXAMINEE_DIV='1' THEN '1' END) AS EXAM_CNT_2_TOTAL,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='2' and W1.EXAMINEE_DIV='1' and W3.SEX='1' THEN '1' END) AS EXAM_CNT_4_MAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='2' and W1.EXAMINEE_DIV='1' and W3.SEX='2' THEN '1' END) AS EXAM_CNT_4_WOMAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='2' and W1.EXAMINEE_DIV='1' THEN '1' END) AS EXAM_CNT_4_TOTAL,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='3' and W1.EXAMINEE_DIV='1' and W3.SEX='1' THEN '1' END) AS EXAM_CNT_3_MAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='3' and W1.EXAMINEE_DIV='1' and W3.SEX='2' THEN '1' END) AS EXAM_CNT_3_WOMAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='3' and W1.EXAMINEE_DIV='1' THEN '1' END) AS EXAM_CNT_3_TOTAL,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAMINEE_DIV='1' THEN '1' END) AS EXAM_CNT_TOTAL,");
		// 合格者数
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='1' and W2.JUDGEDIV='1' and W3.SEX='1' THEN '1' END) AS PASS_CNT_2_MAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='1' and W2.JUDGEDIV='1' and W3.SEX='2' THEN '1' END) AS PASS_CNT_2_WOMAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='1' and W2.JUDGEDIV='1' THEN '1' END) AS PASS_CNT_2_TOTAL,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='2' and W2.JUDGEDIV='1' and W3.SEX='1' THEN '1' END) AS PASS_CNT_4_MAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='2' and W2.JUDGEDIV='1' and W3.SEX='2' THEN '1' END) AS PASS_CNT_4_WOMAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='2' and W2.JUDGEDIV='1' THEN '1' END) AS PASS_CNT_4_TOTAL,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='3' and W2.JUDGEDIV='1' and W3.SEX='1' THEN '1' END) AS PASS_CNT_3_MAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='3' and W2.JUDGEDIV='1' and W3.SEX='2' THEN '1' END) AS PASS_CNT_3_WOMAN,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W1.EXAM_TYPE='3' and W2.JUDGEDIV='1' THEN '1' END) AS PASS_CNT_3_TOTAL,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV='"+test_div+"' and W2.JUDGEDIV='1' THEN '1' END) AS PASS_CNT_TOTAL");
		
		stb.append(" from  ENTEXAM_DESIRE_DAT W1 ");
		stb.append("    left outer join ENTEXAM_RECEPT_DAT W2 ON");
		stb.append("        W1.ENTEXAMYEAR  = W2.ENTEXAMYEAR and");				// 入試年度
		stb.append("        W1.APPLICANTDIV = W2.APPLICANTDIV and");			// 入試制度
		stb.append("        W1.TESTDIV      = W2.TESTDIV and");					// 入試区分
		stb.append("        W1.EXAM_TYPE    = W2.EXAM_TYPE and");				// 受験型
		stb.append("        W1.EXAMNO       = W2.EXAMNO ");						// 受験番号
		stb.append("    inner join ENTEXAM_APPLICANTBASE_DAT W3 ON");
		stb.append("        W1.ENTEXAMYEAR  = W3.ENTEXAMYEAR and");				// 入試年度
		stb.append("        W1.APPLICANTDIV = W3.APPLICANTDIV and");			// 入試制度
		stb.append("        W1.EXAMNO       = W3.EXAMNO ");						// 受験番号
		stb.append(" where");
		stb.append("    W1.ENTEXAMYEAR='"+_param._year+"' ");					// 入試年度
        log.debug("帳票出力対象データ抽出 の SQL=" + stb.toString());
        return stb.toString();
    }
    
    /**
     * @param db2			ＤＢ接続オブジェクト
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List getTotalCount(final DB2UDB db2)
    	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getTotalSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final totalrec totalrec = new totalrec(
                		rs.getString("CNT_total_man"),
                		rs.getString("CNT_total_woman"),
                		rs.getString("CNT_total")
                );
                rtnList.add(totalrec);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /** 実人数取得処理 */
    private String getTotalSql() throws SQLException {
        final String rtn;

		rtn = " select"
            + "    COUNT(CASE WHEN W1.SEX='1' THEN '1' END) AS CNT_total_man,"
            + "    COUNT(CASE WHEN W1.SEX='2' THEN '1' END) AS CNT_total_woman,"
            + "    COUNT(*) AS CNT_total"
            + " from ("
            + "    select"
            + "       APPLICANTDIV, EXAMNO"
            + "    from"
            + "    ENTEXAM_DESIRE_DAT"
            + "    where"
            + "        ENTEXAMYEAR = '"+_param._year+"' "
            + "    group by APPLICANTDIV, EXAMNO "
            + "    ) T1"
            + "    inner join ENTEXAM_APPLICANTBASE_DAT W1 ON"
            + "        T1.APPLICANTDIV = W1.APPLICANTDIV and "
            + "        T1.EXAMNO       = W1.EXAMNO "
            + "    where "
            + "        ENTEXAMYEAR = '"+_param._year+"' "
            ;

        return rtn;
    }

    /** 受験コースマスタ抽出処理 */
    public HashMap getCapacity(DB2UDB db2) throws SQLException, Exception {
        final String sql = sqlgetCapacityMst();
        HashMap rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String code = rs.getString("TESTDIV");
                final String name = rs.getString("CAPACITY");
                rtn.put(code, name);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    /** 受験コースマスタ取得SQL生成 */
    private String sqlgetCapacityMst(){
        final String rtn;

		rtn = " select"
            + "    TESTDIV,"
            + "    CAPACITY"
            + " from"
            + "    ENTEXAM_COURSE_MST"
            + " where"
            + "    ENTEXAMYEAR = '"+_param._year+"' and"	// 入試年度
            + "    COURSECD = '1' and"					// 課程コード
            + "    MAJORCD = '001' and"					// 学科コード
            + "    EXAMCOURSECD = '0001' "				// コースコード
            ;
        return rtn;
    }
    
    /** 生徒クラス */
    private class student {
        final String _sigan_cnt_2_man;
        final String _sigan_cnt_2_woman;
        final String _sigan_cnt_2_total;
        final String _sigan_cnt_4_man;
        final String _sigan_cnt_4_woman;
        final String _sigan_cnt_4_total;
        final String _sigan_cnt_3_man;
        final String _sigan_cnt_3_woman;
        final String _sigan_cnt_3_total;
        final String _sigan_cnt_total;
        final String _exam_cnt_2_man;
        final String _exam_cnt_2_woman;
        final String _exam_cnt_2_total;
        final String _exam_cnt_4_man;
        final String _exam_cnt_4_woman;
        final String _exam_cnt_4_total;
        final String _exam_cnt_3_man;
        final String _exam_cnt_3_woman;
        final String _exam_cnt_3_total;
        final String _exam_cnt_total;
        final String _pass_cnt_2_man;
        final String _pass_cnt_2_woman;
        final String _pass_cnt_2_total;
        final String _pass_cnt_4_man;
        final String _pass_cnt_4_woman;
        final String _pass_cnt_4_total;
        final String _pass_cnt_3_man;
        final String _pass_cnt_3_woman;
        final String _pass_cnt_3_total;
        final String _pass_cnt_total;

        student(
                final String sigan_cnt_2_man,
                final String sigan_cnt_2_woman,
                final String sigan_cnt_2_total,
                final String sigan_cnt_4_man,
                final String sigan_cnt_4_woman,
                final String sigan_cnt_4_total,
                final String sigan_cnt_3_man,
                final String sigan_cnt_3_woman,
                final String sigan_cnt_3_total,
                final String sigan_cnt_total,
                final String exam_cnt_2_man,
                final String exam_cnt_2_woman,
                final String exam_cnt_2_total,
                final String exam_cnt_4_man,
                final String exam_cnt_4_woman,
                final String exam_cnt_4_total,
                final String exam_cnt_3_man,
                final String exam_cnt_3_woman,
                final String exam_cnt_3_total,
                final String exam_cnt_total,
                final String pass_cnt_2_man,
                final String pass_cnt_2_woman,
                final String pass_cnt_2_total,
                final String pass_cnt_4_man,
                final String pass_cnt_4_woman,
                final String pass_cnt_4_total,
                final String pass_cnt_3_man,
                final String pass_cnt_3_woman,
                final String pass_cnt_3_total,
                final String pass_cnt_total
        ) {
			_sigan_cnt_2_man   = sigan_cnt_2_man;
			_sigan_cnt_2_woman = sigan_cnt_2_woman;
			_sigan_cnt_2_total = sigan_cnt_2_total;
			_sigan_cnt_4_man   = sigan_cnt_4_man;
			_sigan_cnt_4_woman = sigan_cnt_4_woman;
			_sigan_cnt_4_total = sigan_cnt_4_total;
			_sigan_cnt_3_man   = sigan_cnt_3_man;
			_sigan_cnt_3_woman = sigan_cnt_3_woman;
			_sigan_cnt_3_total = sigan_cnt_3_total;
			_sigan_cnt_total   = sigan_cnt_total;
			_exam_cnt_2_man    = exam_cnt_2_man;
			_exam_cnt_2_woman  = exam_cnt_2_woman;
			_exam_cnt_2_total  = exam_cnt_2_total;
			_exam_cnt_4_man    = exam_cnt_4_man;
			_exam_cnt_4_woman  = exam_cnt_4_woman;
			_exam_cnt_4_total  = exam_cnt_4_total;
			_exam_cnt_3_man    = exam_cnt_3_man;
			_exam_cnt_3_woman  = exam_cnt_3_woman;
			_exam_cnt_3_total  = exam_cnt_3_total;
			_exam_cnt_total    = exam_cnt_total;
			_pass_cnt_2_man    = pass_cnt_2_man;
			_pass_cnt_2_woman  = pass_cnt_2_woman;
			_pass_cnt_2_total  = pass_cnt_2_total;
			_pass_cnt_4_man    = pass_cnt_4_man;
			_pass_cnt_4_woman  = pass_cnt_4_woman;
			_pass_cnt_4_total  = pass_cnt_4_total;
			_pass_cnt_3_man    = pass_cnt_3_man;
			_pass_cnt_3_woman  = pass_cnt_3_woman;
			_pass_cnt_3_total  = pass_cnt_3_total;
			_pass_cnt_total    = pass_cnt_total;
        }
    }

    /** 実人数クラス */
    private class totalrec {
        final String _total_mancnt;
        final String _total_womancnt;
        final String _total_cnt;

        totalrec(
                final String total_mancnt,
                final String total_womancnt,
                final String total_cnt
        ) {
			_total_mancnt = total_mancnt;
			_total_womancnt = total_womancnt;
			_total_cnt = total_cnt;
        }
    }
    
    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
    	private final String _programid;
    	private final String _year;
    	private final String _semester;
    	private final String _loginDate;
    	private final String _nendo;
    	private final String _date;
        private HashMap _hcapacity = new HashMap();
    	private int _siganTotalCount = 0;
    	private int _examTotalCount = 0;
    	private int _passTotalCount = 0;
    	private int _recordTotalCount = 0;
        private final boolean _infuruFlg;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _nendo = KenjaProperties.gengou(Integer.parseInt(_year))+"年度";
            _date = KNJ_EditDate.h_format_JP(_loginDate);
            _infuruFlg = getInfuruFlg(db2);
        }

        /**インフルエンザ区分を取得**/
        private boolean getInfuruFlg(final DB2UDB db2) throws SQLException {
            String str = "";
            final String sql = "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1='L017' AND NAMECD2='01'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    str = rs.getString("NAMESPARE1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return "1".equals(str);
        }
    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }

    /**
     * 合計値を算出し返す
     * @param val1	加算値１
     * @param val1	加算値２
     * @return
     */
    private String sumVal(String val1, String val2) {

		int total = Integer.parseInt(val1) + Integer.parseInt(val2);

    	return String.valueOf(total);
    }
    
}
