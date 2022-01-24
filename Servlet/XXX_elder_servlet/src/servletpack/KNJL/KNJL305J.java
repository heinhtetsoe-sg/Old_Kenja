package servletpack.KNJL;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
 * 入試出願状況集計
 * 
 * @author nakasone
 *
 */
public class KNJL305J {
    private static final String FORM_NAME = "KNJL305J.frm";
    private static final String FORM_NAME2 = "KNJL305J_2.frm";
    private static final int TEST_DIV_CASE = 5;
    private static final int TEST_DIV_CASE2 = 6;
    private int reccnt_man = 0;
    private int reccnt_woman = 0;
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL305J.class);
    
    /**
     * KNJD.classから呼ばれる処理
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
            
            _hasData = false;

            printMain(db2, svf);
            
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
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        
        String formName;
        int testDivCase;
        if (!_param._isInfluence) {
            formName = FORM_NAME;
            testDivCase = TEST_DIV_CASE;
        } else {
            formName = FORM_NAME2;
            testDivCase = TEST_DIV_CASE2;
        }
        svf.VrSetForm(formName, 1);
        for(int i=1 ; i<=testDivCase ; i++){
            // 受験コースマスタより定員数を取得
            String retCapacity = getCapacity(db2, String.valueOf(i));
            // 志願者名簿データ取得
            List students = createStudents(db2, String.valueOf(i));
            // 帳票出力のメソッド
            outPutPrint(svf, students, retCapacity, i);
        }
        
        //延人数及び実人数を出力
        if (_hasData) {
            //最終ページに男女合計を出力
            // 延人数(男)
            svf.VrsOutn("COUNT2"    , 1, String.valueOf(reccnt_man));
            // 延人数(女)
            svf.VrsOutn("COUNT2"    , 2, String.valueOf(reccnt_woman));
            // 延人数(計)
            svf.VrsOutn("COUNT2"    , 3, String.valueOf(reccnt_man+reccnt_woman));
            // 実人数を取得
            final List totalrec = getTotalCount(db2);
            for (Iterator it = totalrec.iterator(); it.hasNext();) {
                final totalrec total = (totalrec) it.next();
                // 実人数(男)
                svf.VrsOutn("COUNT3"    , 1, total._total_mancnt);
                // 実人数(女)
                svf.VrsOutn("COUNT3"    , 2, total._total_womancnt);
                // 実人数(計)
                svf.VrsOutn("COUNT3"    , 3, total._total_cnt);
            }
            svf.VrEndPage();
        }

    }
    
    /**
     * 帳票出力処理
     * @param svf			帳票オブジェクト
     * @param student		帳票出力対象クラスオブジェクト
     * @param sCapacity	定員
     * @param irow			帳票出力行数
     */
    private void outPutPrint(final Vrw32alp svf, final List student, final String sCapacity, final int irow) {
        
        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();

            // 対象件数が０件の場合
            if(Integer.valueOf(sudent._examno_cnt).intValue() <= 0){
                return;            
            }
            
			//*================*
			//* ヘッダ         *
			//*================*
            if(irow == 1){
    			svf.VrsOut("NENDO" 		,_param._nendo);	// 対象年度
    			svf.VrsOut("DATE"	 	,_param._date);		// 作成日
	        }
			//*================*
			//* 明細           *
			//*================*
            // 入試区分：Ａ１
            if(irow == 1){
            	// 定員
    			svf.VrsOut("TEST_COUNT1" 	, sCapacity);
            	//*< 入試区分：Ａ１【２科】>*
            	int a1_2ka_man = 0;
            	int a1_2ka_woman = 0;
    			// 出願数(男)
    			svf.VrsOutn("COUNT1" 	, 1, sudent._cnta1_2_1);
    			a1_2ka_man = Integer.valueOf(sudent._cnta1_2_1).intValue();
    			reccnt_man += a1_2ka_man;
    			// 出願数(女)
    			svf.VrsOutn("COUNT1" 	, 2, sudent._cnta1_2_2);
    			a1_2ka_woman = Integer.valueOf(sudent._cnta1_2_2).intValue();
    			reccnt_woman += a1_2ka_woman;
    			// 出願数(計)
    			int a1_2ka_total = a1_2ka_man + a1_2ka_woman;
    			svf.VrsOutn("COUNT1" 	, 3, String.valueOf(a1_2ka_total));
    			
    			//*< 入試区分：Ａ１【４科】>*
            	int a1_4ka_man = 0;
            	int a1_4ka_woman = 0;
    			// 出願数(男)
    			svf.VrsOutn("COUNT1" 	, 4, sudent._cnta1_4_1);
    			a1_4ka_man = Integer.valueOf(sudent._cnta1_4_1).intValue();
    			reccnt_man += a1_4ka_man;
    			// 出願数(女)
    			svf.VrsOutn("COUNT1" 	, 5, sudent._cnta1_4_2);
    			a1_4ka_woman = Integer.valueOf(sudent._cnta1_4_2).intValue();
    			reccnt_woman += a1_4ka_woman;
    			// 出願数(計)
    			int a1_4ka_total = a1_4ka_man + a1_4ka_woman;
    			svf.VrsOutn("COUNT1" 	, 6, String.valueOf(a1_4ka_total));
    			
    			//*< 入試区分：Ａ１【計】>*
    			int a1_man_total = a1_2ka_man + a1_4ka_man;
    			int a1_woman_total = a1_2ka_woman + a1_4ka_woman;
    			int a1_total = a1_2ka_total + a1_4ka_total;
    			svf.VrsOutn("COUNT1" 	, 7, String.valueOf(a1_man_total));
    			svf.VrsOutn("COUNT1" 	, 8, String.valueOf(a1_woman_total));
    			svf.VrsOutn("COUNT1" 	, 9, String.valueOf(a1_total));

    			// 倍率
    			if(Integer.valueOf(sCapacity).intValue() != 0){
        	        svf.VrsOutn("MAGNIFICATION",   9,
            	       		String.valueOf((float)Math.round( (float)a1_total / (float)Integer.valueOf(sCapacity).intValue() * 100) / 100));
    			}
            }

            // 入試区分：Ａ２
            if(irow == 2){
            	// 定員
    			svf.VrsOut("TEST_COUNT2" 	, sCapacity);
            	//*< 入試区分：Ａ２【２科】>*
            	int a2_2ka_man = 0;
            	int a2_2ka_woman = 0;
    			// 出願数(男)
    			svf.VrsOutn("COUNT1" 	, 10, sudent._cnta2_2_1);
    			a2_2ka_man = Integer.valueOf(sudent._cnta2_2_1).intValue();
    			reccnt_man += a2_2ka_man;
    			// 出願数(女)
    			svf.VrsOutn("COUNT1" 	, 11, sudent._cnta2_2_2);
    			a2_2ka_woman = Integer.valueOf(sudent._cnta2_2_2).intValue();
    			reccnt_woman += a2_2ka_woman;
    			// 出願数(計)
    			int a2_2ka_total =a2_2ka_man + a2_2ka_woman;
    			svf.VrsOutn("COUNT1" 	, 12, String.valueOf(a2_2ka_total));
    			//*< 入試区分：Ａ２【計】>*
    			svf.VrsOutn("COUNT1" 	, 13, String.valueOf(a2_2ka_total));
    			// 倍率
    			if(Integer.valueOf(sCapacity).intValue() != 0){
        	        svf.VrsOutn("MAGNIFICATION",   13,
            	       		String.valueOf((float)Math.round( (float)a2_2ka_total / (float)Integer.valueOf(sCapacity).intValue() * 100) / 100));
    			}
            }

            // 入試区分：Ｂ
            if(irow == 3){
            	// 定員
    			svf.VrsOut("TEST_COUNT3" 	, sCapacity);
            	//*< 入試区分：Ｂ【２科】>*
            	int b_2ka_man = 0;
            	int b_2ka_woman = 0;
    			// 出願数(男)
    			svf.VrsOutn("COUNT1" 	, 14, sudent._cntb_2_1);
    			b_2ka_man = Integer.valueOf(sudent._cntb_2_1).intValue();
    			reccnt_man += b_2ka_man;
    			// 出願数(女)
    			svf.VrsOutn("COUNT1" 	, 15, sudent._cntb_2_2);
    			b_2ka_woman = Integer.valueOf(sudent._cntb_2_2).intValue();
    			reccnt_woman += b_2ka_woman;
    			// 出願数(計)
    			int b_2ka_total = b_2ka_man + b_2ka_woman;
    			svf.VrsOutn("COUNT1" 	, 16, String.valueOf(b_2ka_total));
    			
    			//*< 入試区分：Ｂ【４科】>*
            	int b_4ka_man = 0;
            	int b_4ka_woman = 0;
    			// 出願数(男)
    			svf.VrsOutn("COUNT1" 	, 17, sudent._cntb_4_1);
    			b_4ka_man = Integer.valueOf(sudent._cntb_4_1).intValue();
    			reccnt_man += b_4ka_man;
    			// 出願数(女)
    			svf.VrsOutn("COUNT1" 	, 18, sudent._cntb_4_2);
    			b_4ka_woman = Integer.valueOf(sudent._cntb_4_2).intValue();
    			reccnt_woman += b_4ka_woman;
    			// 出願数(計)
    			int b_4ka_total = b_4ka_man + b_4ka_woman;
    			svf.VrsOutn("COUNT1" 	, 19, String.valueOf(b_4ka_total));
    			
    			//*< 入試区分：Ｂ【計】>*
    			int b_man_total = b_2ka_man + b_4ka_man;
    			int b_woman_total = b_2ka_woman + b_4ka_woman;
    			int b_total = b_2ka_total + b_4ka_total;
    			svf.VrsOutn("COUNT1" 	, 20, String.valueOf(b_man_total));
    			svf.VrsOutn("COUNT1" 	, 21, String.valueOf(b_woman_total));
    			svf.VrsOutn("COUNT1" 	, 22, String.valueOf(b_total));
    			// 倍率
    			if(Integer.valueOf(sCapacity).intValue() != 0){
        	        svf.VrsOutn("MAGNIFICATION",   22,
            	       		String.valueOf((float)Math.round( (float)b_total / (float)Integer.valueOf(sCapacity).intValue() * 100) / 100));
    			}
            }

            // 入試区分：Ｃ
            if(irow == 4){
                if (!_param._isInfluence) {
                    // 定員
                    svf.VrsOut("TEST_COUNT4"    , sCapacity);
                    //*< 入試区分：Ｃ【２科】>*
                    // 出願数(男)
                    int c_2ka_man = Integer.valueOf(sudent._cntc_2_1).intValue();
                    reccnt_man += c_2ka_man;
                    // 出願数(女)
                    int c_2ka_woman = Integer.valueOf(sudent._cntc_2_2).intValue();
                    reccnt_woman += c_2ka_woman;
                    int c_2ka_total =c_2ka_man + c_2ka_woman;
                    int c_4ka_man = 0;
                    int c_4ka_woman = 0;
                    // 出願数(男)
                    svf.VrsOutn("COUNT1"    , 23, sudent._cntc_4_1);
                    c_4ka_man = Integer.valueOf(sudent._cntc_4_1).intValue();
                    reccnt_man += c_4ka_man;
                    // 出願数(女)
                    svf.VrsOutn("COUNT1"    , 24, sudent._cntc_4_2);
                    c_4ka_woman = Integer.valueOf(sudent._cntc_4_2).intValue();
                    reccnt_woman += c_4ka_woman;
                    // 出願数(計)
                    int c_4ka_total =c_4ka_man + c_4ka_woman;
                    svf.VrsOutn("COUNT1"    , 25, String.valueOf(c_4ka_total));
                    //*< 入試区分：Ｃ【計】>*
                    int c_total = c_4ka_total + c_2ka_total;
                    svf.VrsOutn("COUNT1"    , 26, String.valueOf(c_total));
                    // 倍率
                    if(Integer.valueOf(sCapacity).intValue() != 0){
                        svf.VrsOutn("MAGNIFICATION",   26,
                                String.valueOf((float)Math.round( (float)c_total / (float)Integer.valueOf(sCapacity).intValue() * 100) / 100));
                    }
                } else {
                    // 定員
                    svf.VrsOut("TEST_COUNT4"    , sCapacity);
                    //*< 入試区分：Ｃ【２科】>*
                    // 出願数(男)
                    svf.VrsOutn("COUNT1"    , 23, sudent._cntc_2_1);
                    int c_2ka_man = Integer.valueOf(sudent._cntc_2_1).intValue();
                    // 出願数(女)
                    svf.VrsOutn("COUNT1"    , 24, sudent._cntc_2_2);
                    int c_2ka_woman = Integer.valueOf(sudent._cntc_2_2).intValue();
                    // 出願数(計)
                    int c_2ka_total =c_2ka_man + c_2ka_woman;
                    svf.VrsOutn("COUNT1"    , 25, String.valueOf(c_2ka_total));

                    //*< 入試区分：Ｃ【４科】>*
                    // 出願数(男)
                    svf.VrsOutn("COUNT1"    , 26, sudent._cntc_4_1);
                    int c_4ka_man = Integer.valueOf(sudent._cntc_4_1).intValue();
                    // 出願数(女)
                    svf.VrsOutn("COUNT1"    , 27, sudent._cntc_4_2);
                    int c_4ka_woman = Integer.valueOf(sudent._cntc_4_2).intValue();

                    // 出願数(計)
                    int c_4ka_total =c_4ka_man + c_4ka_woman;
                    svf.VrsOutn("COUNT1"    , 28, String.valueOf(c_4ka_total));

                    //*< 入試区分：Ｃ【計】>*
                    svf.VrsOutn("COUNT1"    , 29, String.valueOf(c_2ka_man + c_4ka_man));
                    svf.VrsOutn("COUNT1"    , 30, String.valueOf(c_2ka_woman + c_4ka_woman));
                    reccnt_man += c_2ka_man + c_4ka_man;
                    reccnt_woman += c_2ka_woman + c_4ka_woman;
                    int c_total = c_2ka_total + c_4ka_total;
                    svf.VrsOutn("COUNT1"    , 31, String.valueOf(c_total));
                    // 倍率
                    if(Integer.valueOf(sCapacity).intValue() != 0){
                        svf.VrsOutn("MAGNIFICATION",   31,
                                String.valueOf((float)Math.round( (float)c_total / (float)Integer.valueOf(sCapacity).intValue() * 100) / 100));
                    }
                }
            }

            // 入試区分：帰国生
            if(irow == 5){
                //*< 入試区分：帰国生【３科】>*
                if (!_param._isInfluence) {
                    // 出願数(男)
                    svf.VrsOutn("COUNT1"    , 27, sudent._cntk_3_1);
                    int k_3ka_man = Integer.valueOf(sudent._cntk_3_1).intValue();
                    reccnt_man += k_3ka_man;
                    // 出願数(女)
                    svf.VrsOutn("COUNT1"    , 28, sudent._cntk_3_2);
                    int k_3ka_woman = Integer.valueOf(sudent._cntk_3_2).intValue();
                    reccnt_woman += k_3ka_woman;
                    // 出願数(計)
                    int k_3ka_total =k_3ka_man + k_3ka_woman;
                    svf.VrsOutn("COUNT1"    , 29, String.valueOf(k_3ka_total));
                    //*< 入試区分：帰国生【計】>*
                    svf.VrsOutn("COUNT1"    , 30, String.valueOf(k_3ka_total));
                } else {
                    // 出願数(男)
                    svf.VrsOutn("COUNT1"    , 41, sudent._cntk_3_1);
                    int k_3ka_man = Integer.valueOf(sudent._cntk_3_1).intValue();
                    reccnt_man += k_3ka_man;
                    // 出願数(女)
                    svf.VrsOutn("COUNT1"    , 42, sudent._cntk_3_2);
                    int k_3ka_woman = Integer.valueOf(sudent._cntk_3_2).intValue();
                    reccnt_woman += k_3ka_woman;
                    // 出願数(計)
                    int k_3ka_total =k_3ka_man + k_3ka_woman;
                    svf.VrsOutn("COUNT1"    , 43, String.valueOf(k_3ka_total));
                    //*< 入試区分：帰国生【計】>*
                    svf.VrsOutn("COUNT1"    , 44, String.valueOf(k_3ka_total));
                }
            }
            
            //*< 入試区分：Ｄ>*
            if (irow == 6 && _param._isInfluence) {
                // 定員
                svf.VrsOut("TEST_COUNT5"    , sCapacity);
                // 出願数(男)
                svf.VrsOutn("COUNT1"    , 32, sudent._cntd_2_1);
                int _2ka_man = Integer.valueOf(sudent._cntd_2_1).intValue();
                reccnt_man += _2ka_man;
                // 出願数(女)
                svf.VrsOutn("COUNT1"    , 33, sudent._cntd_2_2);
                int _2ka_woman = Integer.valueOf(sudent._cntd_2_2).intValue();
                reccnt_woman += _2ka_woman;
                // 出願数(計)
                int _2ka_total = _2ka_man + _2ka_woman;
                svf.VrsOutn("COUNT1"    , 34, String.valueOf(_2ka_total));
                
                //*<【４科】>*
                // 出願数(男)
                svf.VrsOutn("COUNT1"    , 35, sudent._cntd_4_1);
                int _4ka_man = Integer.valueOf(sudent._cntd_4_1).intValue();
                reccnt_man += _4ka_man;
                // 出願数(女)
                svf.VrsOutn("COUNT1"    , 36, sudent._cntd_4_2);
                int _4ka_woman = Integer.valueOf(sudent._cntd_4_2).intValue();
                reccnt_woman += _4ka_woman;
                // 出願数(計)
                int _4ka_total = _4ka_man + _4ka_woman;
                svf.VrsOutn("COUNT1"    , 37, String.valueOf(_4ka_total));
                
                //*<【計】>*
                int _man_total = _2ka_man + _4ka_man;
                int _woman_total = _2ka_woman + _4ka_woman;
                int _total = _2ka_total + _4ka_total;
                svf.VrsOutn("COUNT1"    , 38, String.valueOf(_man_total));
                svf.VrsOutn("COUNT1"    , 39, String.valueOf(_woman_total));
                svf.VrsOutn("COUNT1"    , 40, String.valueOf(_total));
                // 倍率
                if(Integer.valueOf(sCapacity).intValue() != 0){
                    svf.VrsOutn("MAGNIFICATION",   40,
                            String.valueOf((float)Math.round( (float)_total / (float)Integer.valueOf(sCapacity).intValue() * 100) / 100));
                }
            }
                
            
            _hasData = true;            
        }
    }
    
    /**
     * @param db2				ＤＢ接続オブジェクト
     * @param test_div			入試区分
     * @param iapplicantdiv	入試制度
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
                		rs.getString("EXAMNO_CNT"),
                		rs.getString("CNTA1_2_1"),
						rs.getString("CNTA1_2_2"),
						rs.getString("CNTA1_4_1"),
						rs.getString("CNTA1_4_2"),
						rs.getString("CNTA2_2_1"),
						rs.getString("CNTA2_2_2"),
						rs.getString("CNTB_2_1"),
						rs.getString("CNTB_2_2"),
						rs.getString("CNTB_4_1"),
						rs.getString("CNTB_4_2"),
                        rs.getString("CNTC_2_1"),
                        rs.getString("CNTC_2_2"),
						rs.getString("CNTC_4_1"),
						rs.getString("CNTC_4_2"),
						rs.getString("CNTK_3_1"),
						rs.getString("CNTK_3_2"),
                        rs.getString("CNTD_2_1"),
                        rs.getString("CNTD_2_2"),
                        rs.getString("CNTD_4_1"),
                        rs.getString("CNTD_4_2")
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
		
		String serchReceptDate = fomatSakuseiDate(_param._receipt_date,"yyyy-MM-dd", "yyyyMMdd");
		
		stb.append("select ");
		// 入試区分：Ａ１
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='1' and W1.SEX='1' THEN '1' END) AS CNTA1_2_1,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='1' and W1.SEX='2' THEN '1' END) AS CNTA1_2_2,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='2' and W1.SEX='1' THEN '1' END) AS CNTA1_4_1,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='2' and W1.SEX='2' THEN '1' END) AS CNTA1_4_2,");
		// 入試区分：Ａ２
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='1' and W1.SEX='1' THEN '1' END) AS CNTA2_2_1,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='1' and W1.SEX='2' THEN '1' END) AS CNTA2_2_2,");
		// 入試区分：Ｂ
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='1' and W1.SEX='1' THEN '1' END) AS CNTB_2_1,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='1' and W1.SEX='2' THEN '1' END) AS CNTB_2_2,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='2' and W1.SEX='1' THEN '1' END) AS CNTB_4_1,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='2' and W1.SEX='2' THEN '1' END) AS CNTB_4_2,");
		// 入試区分：Ｃ
        stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='1' and W1.SEX='1' THEN '1' END) AS CNTC_2_1,");
        stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='1' and W1.SEX='2' THEN '1' END) AS CNTC_2_2,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='2' and W1.SEX='1' THEN '1' END) AS CNTC_4_1,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='2' and W1.SEX='2' THEN '1' END) AS CNTC_4_2,");
		// 入試区分：帰国生
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='3' and W1.SEX='1' THEN '1' END) AS CNTK_3_1,");
		stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='3' and W1.SEX='2' THEN '1' END) AS CNTK_3_2,");
        // 入試区分：Ｄ
        stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='1' and W1.SEX='1' THEN '1' END) AS CNTD_2_1,");
        stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='1' and W1.SEX='2' THEN '1' END) AS CNTD_2_2,");
        stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='2' and W1.SEX='1' THEN '1' END) AS CNTD_4_1,");
        stb.append("    COUNT(CASE WHEN W1.TESTDIV"+ test_div + "='2' and W1.SEX='2' THEN '1' END) AS CNTD_4_2,");
		// 志願者数
		stb.append("    COUNT(*) AS EXAMNO_CNT ");
		
		stb.append(" from  ENTEXAM_APPLICANTBASE_DAT W1 ");
		stb.append(" where  W1.ENTEXAMYEAR='"+_param._year+"' and");			// 入試年度
		stb.append("        substr(REPLACE(char(W1.RECEPTDATE ),'-',''),1,8) <= '"+serchReceptDate+"' and");// 受付日付
		stb.append("        W1.TESTDIV"+ test_div + " is not null");			// 入試区分
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
		String serchReceptDate = fomatSakuseiDate(_param._receipt_date,"yyyy-MM-dd", "yyyyMMdd");

		rtn = " select"
            + "    COUNT(CASE WHEN W1.SEX='1' THEN '1' END) AS CNT_total_man,"
            + "    COUNT(CASE WHEN W1.SEX='2' THEN '1' END) AS CNT_total_woman,"
            + "    COUNT(*) AS CNT_total"
            + " from"
            + "    ENTEXAM_APPLICANTBASE_DAT W1"
            + " where"
            + "    W1.ENTEXAMYEAR = '"+_param._year+"' and"
            + "    substr(REPLACE(char(W1.RECEPTDATE ),'-',''),1,8) <= '"+serchReceptDate+"' "
            ;

        return rtn;
    }


    /** 受験コースマスタ抽出処理 */
    private String getCapacity(final DB2UDB db2, final String tesdiv) throws SQLException {
        String retCapacity = "";

        PreparedStatement ps = null;
        ResultSet rs = null;
		StringBuffer stb = new StringBuffer();
        try {
			stb.append("select ");
			stb.append("    CAPACITY ");								// 定員
			stb.append(" from  ENTEXAM_COURSE_MST ");
			stb.append(" where  ENTEXAMYEAR='"+_param._year+"' and");	// 入試年度
			stb.append("        TESTDIV='"+tesdiv+"' and");				// 入試区分
			stb.append("        COURSECD = '1' and");					// 課程コード
			stb.append("        MAJORCD = '001' and");					// 学科コード
			stb.append("        EXAMCOURSECD = '0001' ");				// コースコード
			db2.query(stb.toString());
			rs = db2.getResultSet();
			while( rs.next() ){
				retCapacity = rs.getString("CAPACITY");
			}
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retCapacity;
    }

    /** 生徒クラス */
    private class student {
        final String _examno_cnt;
        final String _cnta1_2_1;
        final String _cnta1_2_2;
        final String _cnta1_4_1;
        final String _cnta1_4_2;
        final String _cnta2_2_1;
        final String _cnta2_2_2;
        final String _cntb_2_1;
        final String _cntb_2_2;
        final String _cntb_4_1;
        final String _cntb_4_2;
        final String _cntc_2_1;
        final String _cntc_2_2;
        final String _cntc_4_1;
        final String _cntc_4_2;
        final String _cntk_3_1;
        final String _cntk_3_2;
        final String _cntd_2_1;
        final String _cntd_2_2;
        final String _cntd_4_1;
        final String _cntd_4_2;

        student(
                final String examno_cnt,
                final String cnta1_2_1,
                final String cnta1_2_2,
                final String cnta1_4_1,
                final String cnta1_4_2,
                final String cnta2_2_1,
                final String cnta2_2_2,
                final String cntb_2_1,
                final String cntb_2_2,
                final String cntb_4_1,
                final String cntb_4_2,
                final String cntc_2_1,
                final String cntc_2_2,
                final String cntc_4_1,
                final String cntc_4_2,
                final String cntk_3_1,
                final String cntk_3_2,
                final String cntd_2_1,
                final String cntd_2_2,
                final String cntd_4_1,
                final String cntd_4_2
        ) {
			_examno_cnt = examno_cnt;
			_cnta1_2_1 = cnta1_2_1;
			_cnta1_2_2 = cnta1_2_2;
			_cnta1_4_1 = cnta1_4_1;
			_cnta1_4_2 = cnta1_4_2;
			_cnta2_2_1 = cnta2_2_1;
			_cnta2_2_2 = cnta2_2_2;
			_cntb_2_1 = cntb_2_1;
			_cntb_2_2 = cntb_2_2;
			_cntb_4_1 = cntb_4_1;
			_cntb_4_2 = cntb_4_2;
            _cntc_2_1 = cntc_2_1;
            _cntc_2_2 = cntc_2_2;
			_cntc_4_1 = cntc_4_1;
			_cntc_4_2 = cntc_4_2;
			_cntk_3_1 = cntk_3_1;
			_cntk_3_2 = cntk_3_2;
            _cntd_2_1 = cntd_2_1;
            _cntd_2_2 = cntd_2_2;
            _cntd_4_1 = cntd_4_1;
            _cntd_4_2 = cntd_4_2;
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
    
	private void preStatClose(final PreparedStatement ps1) {
		try {
			ps1.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
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
    	private final String _receipt_date;
    	private final String _nendo;
    	private final String _date;
        /** フォームKNJL305J_2.frm、KNJL305J_3.frmを使用する */
        private boolean _isInfluence = false;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _receipt_date = fomatSakuseiDate(request.getParameter("RECEIPT_DATE"),"yyyy/MM/dd","yyyy-MM-dd");
            _nendo = KenjaProperties.gengou(Integer.parseInt(_year))+"年度";
            _date = KNJ_EditDate.h_format_JP(_receipt_date);
            setInfluence(db2);
        }
        
        private void setInfluence(DB2UDB db2) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;
            ps = db2.prepareStatement("SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '01' ");
            rs = ps.executeQuery();
            if (rs.next()) {
                _isInfluence = "1".equals(rs.getString("NAMESPARE1"));
            }
            DbUtils.closeQuietly(null, ps, rs);
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
     * 日付をフォーマットYYYY-MM-DDに設定する
     * @param s
     * @return
     */
    private String fomatSakuseiDate(String cnvDate, String beforeDate, String afterDate) {

    	String retDate = "";
    	try {
			DateFormat foramt = new SimpleDateFormat(beforeDate); 
			//文字列よりDate型へ変換
			Date date1 = foramt.parse(cnvDate); 
			// 年月日のフォーマットを指定
			SimpleDateFormat sdf1 = new SimpleDateFormat(afterDate);
			// Date型より文字列へ変換
			retDate = sdf1.format(date1);
		} catch( Exception e ){
			log.error("setHeader set error!");
		}
		return retDate;
    }
}
