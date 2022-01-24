package servletpack.KNJL;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
 * 入試結果集計２
 * 
 * @author nakasone
 *
 */
public class KNJL376J {
    private static final String FORM_NAME = "KNJL376J.frm";
    private static final String FORM_NAME2 = "KNJL376J_2.frm";
    private static final int TEST_DIV_CASE = 5;
    private static final int TEST_DIV_CASE2 = 6;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    private String week[] = {"","日","月","火","水","木","金","土"};
	private	Calendar calw = Calendar.getInstance();
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL376J.class);
    
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
        final int testDivCase;
        if (_param._isInfluence) {
            svf.VrSetForm(FORM_NAME2, 1);
            testDivCase = TEST_DIV_CASE2;
        } else {
            svf.VrSetForm(FORM_NAME, 1);
            testDivCase = TEST_DIV_CASE;
        }

        // 受験コースマスタより定員数を取得
        _param._hcapacity = getCapacity(db2);
        // 名称マスタより入学試験日を取得
        _param._testDate = getTestDate(db2);
        // 実志願者数の合計用リスト初期化
        _param._realApplicantList = new ArrayList();
        
        
        for(int i=1 ; i<=testDivCase ; i++){
           	// 入試結果集計１データ取得
       		final List student = createStudents(db2, String.valueOf(i));
       		// 帳票出力のメソッド
       		outPutPrint(svf, student, String.valueOf(i));
        }
        
        // 帳票出力データが存在する場合
        if(_param._recordTotalCount > 0){
        	refflg = true;
        }

		if (refflg) {
			// 合計数を取得
			final List totalrec = getTotalCount(db2);
	   		// 帳票出力のメソッド
	   		outTotalPrint(svf, totalrec);
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
    private void outPutPrint(final Vrw32alp svf, final List student, final String testDiv) throws Exception {
        
    	int irow = 0;

    	// 開始行数を取得
        if (_param._isInfluence) {
            if ("6".equals(testDiv)) {
                irow = 5;
            } else if ("5".equals(testDiv)) {
                irow = 6;
            } else {
                irow = Integer.parseInt(testDiv);
            }
        } else {
            irow = Integer.parseInt(testDiv);
        }
    	
		//*================*
		//* ヘッダ         *
		//*================*
		svf.VrsOut("NENDO" 		,_param._nendo);	// 対象年度
		svf.VrsOut("DATE"	 	,_param._date);		// 作成日
        
        try {
    		// 入学試験日
        	String testDate = (String)_param._testDate.get(testDiv);
        	testDate = fomatSakuseiDate(testDate,"yyyy/MM/dd","MM'月'dd'日'");
    	    calw.setTime(sdf.parse((String)_param._testDate.get(testDiv)));
    		svf.VrsOutn( "EXAMDAY", irow,  testDate+"("+week[ calw.get( Calendar.DAY_OF_WEEK ) ] +")");
        } catch (final Exception e) {
            log.error("日付設定処理エラー:", e);
        }
		
		//*================*
		//* 明細           *
		//*================*
        if(!testDiv.equals("5")){
    		svf.VrsOutn("CAPACITY", irow,(String)_param._hcapacity.get(testDiv)+"名");// 定員
        }

        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();

            if (sudent._isRealApplicant) { // 実志願者の表示は志願者数の欄のみ行う
                svf.VrsOutn("COUNT1_1_2"   , irow,     sudent._sigan_cnt_2_man);       // 志願者数(2科:男)
                svf.VrsOutn("COUNT1_3_2"   , irow,     sudent._sigan_cnt_2_woman);     // 志願者数(2科:女)
                svf.VrsOutn("COUNT1_2_2"   , irow,     sudent._sigan_cnt_4_man);       // 志願者数(4科:男)
                svf.VrsOutn("COUNT1_4_2"   , irow,     sudent._sigan_cnt_4_woman);     // 志願者数(4科:女)
                svf.VrsOutn("TOTAL1_2"     , irow,     sudent._sigan_cnt_total);       // 志願者数(計)
                continue;
            }
            svf.VrsOutn("COUNT1_1"  , irow,     sudent._sigan_cnt_2_man);       // 志願者数(2科:男)
            svf.VrsOutn("COUNT1_3"  , irow,     sudent._sigan_cnt_2_woman);     // 志願者数(2科:女)
            svf.VrsOutn("COUNT1_2"  , irow,     sudent._sigan_cnt_4_man);       // 志願者数(4科:男)
            svf.VrsOutn("COUNT1_4"  , irow,     sudent._sigan_cnt_4_woman);     // 志願者数(4科:女)
            svf.VrsOutn("TOTAL1"    , irow,     sudent._sigan_cnt_total);       // 志願者数(計)
            
   			svf.VrsOutn("COUNT2_1" 	, irow,		sudent._exam_cnt_2_man);		// 受験者数(2科:男)
   			svf.VrsOutn("COUNT2_3" 	, irow,		sudent._exam_cnt_2_woman);		// 受験者数(2科:女)
			svf.VrsOutn("COUNT2_2" 	, irow,		sudent._exam_cnt_4_man);		// 受験者数(4科:男)
			svf.VrsOutn("COUNT2_4" 	, irow,		sudent._exam_cnt_4_woman);		// 受験者数(4科:女)
			svf.VrsOutn("TOTAL2" 	, irow,		sudent._exam_cnt_total);		// 受験者数(計)

   			svf.VrsOutn("COUNT3_1" 	, irow,		sudent._pass_cnt_2_man);		// 合格者数(2科:男)
   			svf.VrsOutn("COUNT3_3" 	, irow,		sudent._pass_cnt_2_woman);		// 合格者数(2科:女)
			svf.VrsOutn("COUNT3_2" 	, irow,		sudent._pass_cnt_4_man);		// 合格者数(4科:男)
			svf.VrsOutn("COUNT3_4" 	, irow,		sudent._pass_cnt_4_woman);		// 合格者数(4科:女)
			svf.VrsOutn("TOTAL3" 	, irow,		sudent._pass_cnt_total);		// 合格者数(計)
			
   			svf.VrsOutn("COUNT4_1" 	, irow,		sudent._nyu_1sign_cnt_2_man);	// 一次入学手続者数(2科:男)
   			svf.VrsOutn("COUNT4_3" 	, irow,		sudent._nyu_1sign_cnt_2_woman);	// 一次入学手続者数(2科:女)
			svf.VrsOutn("COUNT4_2" 	, irow,		sudent._nyu_1sign_cnt_4_man);	// 一次入学手続者数(4科:男)
			svf.VrsOutn("COUNT4_4" 	, irow,		sudent._nyu_1sign_cnt_4_woman);	// 一次入学手続者数(4科:女)
			svf.VrsOutn("TOTAL4" 	, irow,		sudent._nyu_1sign_cnt_total);	// 一次入学手続者数(計)

   			svf.VrsOutn("COUNT5_1" 	, irow,		sudent._nyu_2sign_cnt_2_man);	// 二次入学手続者数(2科:男)
   			svf.VrsOutn("COUNT5_3" 	, irow,		sudent._nyu_2sign_cnt_2_woman);	// 二次入学手続者数(2科:女)
			svf.VrsOutn("COUNT5_2" 	, irow,		sudent._nyu_2sign_cnt_4_man);	// 二次入学手続者数(4科:男)
			svf.VrsOutn("COUNT5_4" 	, irow,		sudent._nyu_2sign_cnt_4_woman);	// 二次入学手続者数(4科:女)
			svf.VrsOutn("TOTAL5" 	, irow,		sudent._nyu_2sign_cnt_total);	// 二次入学手続者数(計)

   			svf.VrsOutn("COUNT6_1" 	, irow,		sudent._zitai1_cnt_2_man);		// 一次辞退者数(2科:男)
   			svf.VrsOutn("COUNT6_3" 	, irow,		sudent._zitai1_cnt_2_woman);	// 一次辞退者数(2科:女)
			svf.VrsOutn("COUNT6_2" 	, irow,		sudent._zitai1_cnt_4_man);		// 一次辞退者数(4科:男)
			svf.VrsOutn("COUNT6_4" 	, irow,		sudent._zitai1_cnt_4_woman);	// 一次辞退者数(4科:女)
			svf.VrsOutn("TOTAL6" 	, irow,		sudent._zitai1_cnt_total);		// 一次辞退者数(計)
			
   			svf.VrsOutn("COUNT7_1" 	, irow,		sudent._zitai2_cnt_2_man);		// 二次辞退者数(2科:男)
   			svf.VrsOutn("COUNT7_3" 	, irow,		sudent._zitai2_cnt_2_woman);	// 二次辞退者数(2科:女)
			svf.VrsOutn("COUNT7_2" 	, irow,		sudent._zitai2_cnt_4_man);		// 二次辞退者数(4科:男)
			svf.VrsOutn("COUNT7_4" 	, irow,		sudent._zitai2_cnt_4_woman);	// 二次辞退者数(4科:女)
			svf.VrsOutn("TOTAL7" 	, irow,		sudent._zitai2_cnt_total);		// 二次辞退者数(計)
			
   			svf.VrsOutn("COUNT8_1" 	, irow,		sudent._nyu_cnt_2_man);			// 入学者数(2科:男)
   			svf.VrsOutn("COUNT8_3" 	, irow,		sudent._nyu_cnt_2_woman);		// 入学者数(2科:女)
			svf.VrsOutn("COUNT8_2" 	, irow,		sudent._nyu_cnt_4_man);			// 入学者数(4科:男)
			svf.VrsOutn("COUNT8_4" 	, irow,		sudent._nyu_cnt_4_woman);		// 入学者数(4科:女)
			svf.VrsOutn("TOTAL8" 	, irow,		sudent._nyu_cnt_total);			// 入学者数(計)

            svf.VrsOutn("COUNT9_1"  , irow,     sudent._zitai_all_cnt_2_man);     // 辞退者数計(2科:男)
            svf.VrsOutn("COUNT9_3"  , irow,     sudent._zitai_all_cnt_2_woman);   // 辞退者数計(2科:女)
            svf.VrsOutn("COUNT9_2"  , irow,     sudent._zitai_all_cnt_4_man);     // 辞退者数計(4科:男)
            svf.VrsOutn("COUNT9_4"  , irow,     sudent._zitai_all_cnt_4_woman);   // 辞退者数計(4科:女)
            svf.VrsOutn("TOTAL9"    , irow,     sudent._zitai_all_cnt_total);     // 辞退者数計(計)

            int pass_count = Integer.parseInt(sudent._pass_cnt_total);
			int exam_count = Integer.parseInt(sudent._exam_cnt_total);
			int nyu_count = Integer.parseInt(sudent._nyu_cnt_total);
			if(pass_count != 0){
				// 受験者数÷合格者数を倍率に設定
    	        svf.VrsOutn("RATE1",   irow,
        	       		String.valueOf((float)Math.round( (float)exam_count / (float)pass_count * 100) / 100));
				// 指示画面より歩留率出力指示がされている場合
    	        if(_param._print.equals("1")){
    				// 入学者数÷合格者数を歩留率に設定
        	        svf.VrsOutn("RATE2",   irow,
            	       		String.valueOf((float)Math.round( (float)nyu_count / (float)pass_count * 100) / 100));
    	        }
			}
			
			if(Integer.parseInt(sudent._sigan_cnt_total) > 0){
				++_param._recordTotalCount;
			}
        }
    }
    
    /**
     * 帳票出力処理
     * @param svf			帳票オブジェクト
     * @param totalrec		帳票出力対象クラスオブジェクト
     */
    private void outTotalPrint(final Vrw32alp svf, final List totalrec) {

		//*================*
		//* 合計           *
		//*================*
    	for (Iterator it = totalrec.iterator(); it.hasNext();) {
            final student totalRecord = (student) it.next();

            svf.VrsOut("TOTAL_COUNT1_1",		totalRecord._sigan_cnt_2_man);		// 志願者数(2科:男)
        	svf.VrsOut("TOTAL_COUNT1_3",		totalRecord._sigan_cnt_2_woman);	// 志願者数(2科:女)
        	svf.VrsOut("TOTAL_COUNT1_2",		totalRecord._sigan_cnt_4_man);		// 志願者数(4科:男)
        	svf.VrsOut("TOTAL_COUNT1_4",		totalRecord._sigan_cnt_4_woman);	// 志願者数(4科:女)
        	svf.VrsOut("T_TOTAL1",				totalRecord._sigan_cnt_total);		// 志願者数(計)

            svf.VrsOut("TOTAL_COUNT2_1",		totalRecord._exam_cnt_2_man);		// 受験者数(2科:男)
        	svf.VrsOut("TOTAL_COUNT2_3",		totalRecord._exam_cnt_2_woman);		// 受験者数(2科:女)
        	svf.VrsOut("TOTAL_COUNT2_2",		totalRecord._exam_cnt_4_man);		// 受験者数(4科:男)
        	svf.VrsOut("TOTAL_COUNT2_4",		totalRecord._exam_cnt_4_woman);		// 受験者数(4科:女)
        	svf.VrsOut("T_TOTAL2",				totalRecord._exam_cnt_total);		// 受験者数(計)
        	
            svf.VrsOut("TOTAL_COUNT3_1",		totalRecord._pass_cnt_2_man);		// 合格者数(2科:男)
        	svf.VrsOut("TOTAL_COUNT3_3",		totalRecord._pass_cnt_2_woman);		// 合格者数(2科:女)
        	svf.VrsOut("TOTAL_COUNT3_2",		totalRecord._pass_cnt_4_man);		// 合格者数(4科:男)
        	svf.VrsOut("TOTAL_COUNT3_4",		totalRecord._pass_cnt_4_woman);		// 合格者数(4科:女)
        	svf.VrsOut("T_TOTAL3",				totalRecord._pass_cnt_total);		// 合格者数(計)
        	
        	svf.VrsOut("TOTAL_COUNT4_1",		totalRecord._nyu_1sign_cnt_2_man);	// 一次入学手続者数(2科:男)
        	svf.VrsOut("TOTAL_COUNT4_3",		totalRecord._nyu_1sign_cnt_2_woman);// 一次入学手続者数(2科:女)
        	svf.VrsOut("TOTAL_COUNT4_2",		totalRecord._nyu_1sign_cnt_4_man);	// 一次入学手続者数(4科:男)
        	svf.VrsOut("TOTAL_COUNT4_4",		totalRecord._nyu_1sign_cnt_4_woman);// 一次入学手続者数(4科:女)
        	svf.VrsOut("T_TOTAL4",				totalRecord._nyu_1sign_cnt_total);	// 一次入学手続者数(計)
        	
        	svf.VrsOut("TOTAL_COUNT5_1",		totalRecord._nyu_2sign_cnt_2_man);	// 二次入学手続者数(2科:男)
        	svf.VrsOut("TOTAL_COUNT5_3",		totalRecord._nyu_2sign_cnt_2_woman);// 二次入学手続者数(2科:女)
        	svf.VrsOut("TOTAL_COUNT5_2",		totalRecord._nyu_2sign_cnt_4_man);	// 二次入学手続者数(4科:男)
        	svf.VrsOut("TOTAL_COUNT5_4",		totalRecord._nyu_2sign_cnt_4_woman);// 二次入学手続者数(4科:女)
        	svf.VrsOut("T_TOTAL5",				totalRecord._nyu_2sign_cnt_total);	// 二次入学手続者数(計)
        	
        	svf.VrsOut("TOTAL_COUNT6_1",		totalRecord._zitai1_cnt_2_man);		// 一次辞退者数(2科:男)
        	svf.VrsOut("TOTAL_COUNT6_3",		totalRecord._zitai1_cnt_2_woman);	// 一次辞退者数(2科:女)
        	svf.VrsOut("TOTAL_COUNT6_2",		totalRecord._zitai1_cnt_4_man);		// 一次辞退者数(4科:男)
        	svf.VrsOut("TOTAL_COUNT6_4",		totalRecord._zitai1_cnt_4_woman);	// 一次辞退者数(4科:女)
        	svf.VrsOut("T_TOTAL6",				totalRecord._zitai1_cnt_total);		// 一次辞退者数(計)
        	
        	svf.VrsOut("TOTAL_COUNT7_1",		totalRecord._zitai2_cnt_2_man);		// 二次辞退者数(2科:男)
        	svf.VrsOut("TOTAL_COUNT7_3",		totalRecord._zitai2_cnt_2_woman);	// 二次辞退者数(2科:女)
        	svf.VrsOut("TOTAL_COUNT7_2",		totalRecord._zitai2_cnt_4_man);		// 二次辞退者数(4科:男)
        	svf.VrsOut("TOTAL_COUNT7_4",		totalRecord._zitai2_cnt_4_woman);	// 二次辞退者数(4科:女)
        	svf.VrsOut("T_TOTAL7",				totalRecord._zitai2_cnt_total);		// 二次辞退者数(計)
        	
        	svf.VrsOut("TOTAL_COUNT8_1",		totalRecord._nyu_cnt_2_man);		// 入学者数(2科:男)
        	svf.VrsOut("TOTAL_COUNT8_3",		totalRecord._nyu_cnt_2_woman);		// 入学者数(2科:女)
        	svf.VrsOut("TOTAL_COUNT8_2",		totalRecord._nyu_cnt_4_man);		// 入学者数(4科:男)
        	svf.VrsOut("TOTAL_COUNT8_4",		totalRecord._nyu_cnt_4_woman);		// 入学者数(4科:女)
        	svf.VrsOut("T_TOTAL8",				totalRecord._nyu_cnt_total);		// 入学者数(計)

            svf.VrsOut("TOTAL_COUNT9_1",        totalRecord._zitai_all_cnt_2_man);     // 辞退者数計(2科:男)
            svf.VrsOut("TOTAL_COUNT9_3",        totalRecord._zitai_all_cnt_2_woman);   // 辞退者数計(2科:女)
            svf.VrsOut("TOTAL_COUNT9_2",        totalRecord._zitai_all_cnt_4_man);     // 辞退者数計(4科:男)
            svf.VrsOut("TOTAL_COUNT9_4",        totalRecord._zitai_all_cnt_4_woman);   // 辞退者数計(4科:女)
            svf.VrsOut("T_TOTAL9",              totalRecord._zitai_all_cnt_total);     // 辞退者数計(計)

            int pass_count = Integer.parseInt(totalRecord._pass_cnt_total);
			int exam_count = Integer.parseInt(totalRecord._exam_cnt_total);
			int nyu_count = Integer.parseInt(totalRecord._nyu_cnt_total);
			if(pass_count != 0){
				// 受験者数÷合格者数を倍率に設定
    	        svf.VrsOut("TOTAL_RATE1",
        	       		String.valueOf((float)Math.round( (float)exam_count / (float)pass_count * 100) / 100));
				// 指示画面より歩留率出力指示がされている場合
    	        if(_param._print.equals("1")){
	    	        // 入学者数÷合格者数を歩留率に設定
	    	        svf.VrsOut("TOTAL_RATE2",
	        	       		String.valueOf((float)Math.round( (float)nyu_count / (float)pass_count * 100) / 100));
				}
			}
    	}
        // 志願者数計
        int total_real_applicant_cnt_2_man=0;
        int total_real_applicant_cnt_2_woman=0;
        int total_real_applicant_cnt_4_man=0;
        int total_real_applicant_cnt_4_woman=0;
        int total_real_applicant_cnt_total=0;
        for(Iterator it=_param._realApplicantList.iterator(); it.hasNext();) {
            student s = (student) it.next();
            total_real_applicant_cnt_2_man += Integer.valueOf(s._sigan_cnt_2_man).intValue();
            total_real_applicant_cnt_2_woman += Integer.valueOf(s._sigan_cnt_2_woman).intValue();
            total_real_applicant_cnt_4_man += Integer.valueOf(s._sigan_cnt_4_man).intValue();
            total_real_applicant_cnt_4_woman += Integer.valueOf(s._sigan_cnt_4_woman).intValue();
            total_real_applicant_cnt_total += Integer.valueOf(s._sigan_cnt_total).intValue();
        }
        svf.VrsOut("TOTAL_COUNT1_1_2", String.valueOf(total_real_applicant_cnt_2_man));      // 志願者数(2科:男)
        svf.VrsOut("TOTAL_COUNT1_3_2", String.valueOf(total_real_applicant_cnt_2_woman));    // 志願者数(2科:女)
        svf.VrsOut("TOTAL_COUNT1_2_2", String.valueOf(total_real_applicant_cnt_4_man));      // 志願者数(4科:男)
        svf.VrsOut("TOTAL_COUNT1_4_2", String.valueOf(total_real_applicant_cnt_4_woman));    // 志願者数(4科:女)
        svf.VrsOut("T_TOTAL1_2",       String.valueOf(total_real_applicant_cnt_total));      // 志願者数(計)

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
        ResultSet rs = null; 
        try {
            boolean isRealApplicant = false;
            String sql = getStudentSql(test_div, isRealApplicant);
            //log.debug("createStudents sql="+sql);
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                final student student = getStudent(rs, isRealApplicant);
                rtnList.add(student);
            }

            // 実志願者カウント
            isRealApplicant = true;
            sql = getStudentSql(test_div, isRealApplicant);
            //log.debug("createStudents (real applicant) sql="+sql);
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                final student student = getStudent(rs, isRealApplicant);
                rtnList.add(student);
                _param._realApplicantList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    
    private student getStudent(ResultSet rs, boolean isRealApplicant) 
    throws SQLException{
        return new student(
                rs.getString("SIGAN_CNT_2_MAN"),
                rs.getString("SIGAN_CNT_2_WOMAN"),
                rs.getString("SIGAN_CNT_4_MAN"),
                rs.getString("SIGAN_CNT_4_WOMAN"),
                rs.getString("SIGAN_CNT_TOTAL"),
                rs.getString("EXAM_CNT_2_MAN"),
                rs.getString("EXAM_CNT_2_WOMAN"),
                rs.getString("EXAM_CNT_4_MAN"),
                rs.getString("EXAM_CNT_4_WOMAN"),
                rs.getString("EXAM_CNT_TOTAL"),
                rs.getString("PASS_CNT_2_MAN"),
                rs.getString("PASS_CNT_2_WOMAN"),
                rs.getString("PASS_CNT_4_MAN"),
                rs.getString("PASS_CNT_4_WOMAN"),
                rs.getString("PASS_CNT_TOTAL"),

                rs.getString("NYU_1SIGN_CNT_2_MAN"),
                rs.getString("NYU_1SIGN_CNT_2_WOMAN"),
                rs.getString("NYU_1SIGN_CNT_4_MAN"),
                rs.getString("NYU_1SIGN_CNT_4_WOMAN"),
                rs.getString("NYU_1SIGN_CNT_TOTAL"),
                rs.getString("NYU_2SIGN_CNT_2_MAN"),
                rs.getString("NYU_2SIGN_CNT_2_WOMAN"),
                rs.getString("NYU_2SIGN_CNT_4_MAN"),
                rs.getString("NYU_2SIGN_CNT_4_WOMAN"),
                rs.getString("NYU_2SIGN_CNT_TOTAL"),
                rs.getString("ZITAI1_CNT_2_MAN"),
                rs.getString("ZITAI1_CNT_2_WOMAN"),
                rs.getString("ZITAI1_CNT_4_MAN"),
                rs.getString("ZITAI1_CNT_4_WOMAN"),
                rs.getString("ZITAI1_CNT_TOTAL"),
                rs.getString("ZITAI2_CNT_2_MAN"),
                rs.getString("ZITAI2_CNT_2_WOMAN"),
                rs.getString("ZITAI2_CNT_4_MAN"),
                rs.getString("ZITAI2_CNT_4_WOMAN"),
                rs.getString("ZITAI2_CNT_TOTAL"),
                rs.getString("ZITAI_ALL_CNT_2_MAN"),
                rs.getString("ZITAI_ALL_CNT_2_WOMAN"),
                rs.getString("ZITAI_ALL_CNT_4_MAN"),
                rs.getString("ZITAI_ALL_CNT_4_WOMAN"),
                rs.getString("ZITAI_ALL_CNT_TOTAL"),

                rs.getString("NYU_CNT_2_MAN"),
                rs.getString("NYU_CNT_2_WOMAN"),
                rs.getString("NYU_CNT_4_MAN"),
                rs.getString("NYU_CNT_4_WOMAN"),
                rs.getString("NYU_CNT_TOTAL"),
                isRealApplicant
                );
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @param test_div		入試区分
     * @param isRealApplicant 実志願者カウントフラグ
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql(final String test_div, boolean isRealApplicant){
		StringBuffer stb = new StringBuffer();
		String examType = "";
		
        if(test_div.equals("0")) { // 計
            examType = "('2','3')";
        } else if(test_div.equals("5")){
			examType = "('3')";	// 帰国生の場合は3科型
		} else {
			examType = "('2')";	// 帰国生以外の場合は4科型
		}
		isRealApplicant = isRealApplicant && (!test_div.equals("0")); // 合計のカウント時は実志願者のカウントをしない
        if ( isRealApplicant) {
            stb.append(" with T_JUDGEDIV_BEFORE AS( ");
            stb.append(" select ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     min(case when T1.TESTDIV in "+_param.getTestdivBeforeIn(test_div)+" then T2.JUDGEDIV ");
            stb.append("         else null end) AS JUDGEDIV_BEFORE "); // 前回の試験区分までに合格した人は'1'
            stb.append(" from ENTEXAM_DESIRE_DAT T1 ");
            stb.append("     left join ENTEXAM_RECEPT_DAT T2 on ");
            stb.append("         T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
            stb.append("         and T1.APPLICANTDIV = T2.APPLICANTDIV ");
            stb.append("         and T1.TESTDIV = T2.TESTDIV ");
            stb.append("         and T1.EXAM_TYPE = T2.EXAM_TYPE ");
            stb.append("         and T1.EXAMNO = T2.EXAMNO ");
            stb.append(" group by T1.ENTEXAMYEAR, T1.APPLICANTDIV, T1.EXAMNO ");
            stb.append(" ) ");
        }
		stb.append("select ");
		// 志願者数
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and W3.SEX='1' THEN '1' END) AS SIGAN_CNT_2_MAN,");
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and W3.SEX='2' THEN '1' END) AS SIGAN_CNT_2_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and W3.SEX='1' THEN '1' END) AS SIGAN_CNT_4_MAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and W3.SEX='2' THEN '1' END) AS SIGAN_CNT_4_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE='1' or W1.EXAM_TYPE='2' or W1.EXAM_TYPE='3') THEN '1' END) AS SIGAN_CNT_TOTAL,");
		// 受験者数
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W1.EXAMINEE_DIV, '0')='1' and W3.SEX='1' THEN '1' END) AS EXAM_CNT_2_MAN,");
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W1.EXAMINEE_DIV, '0')='1' and W3.SEX='2' THEN '1' END) AS EXAM_CNT_2_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W1.EXAMINEE_DIV, '0')='1' and W3.SEX='1' THEN '1' END) AS EXAM_CNT_4_MAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W1.EXAMINEE_DIV, '0')='1' and W3.SEX='2' THEN '1' END) AS EXAM_CNT_4_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE='1' or W1.EXAM_TYPE='2' or W1.EXAM_TYPE='3') and VALUE(W1.EXAMINEE_DIV, '0')='1' THEN '1' END) AS EXAM_CNT_TOTAL,");
		// 合格者数
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and W3.SEX='1' THEN '1' END) AS PASS_CNT_2_MAN,");
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and W3.SEX='2' THEN '1' END) AS PASS_CNT_2_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and W3.SEX='1' THEN '1' END) AS PASS_CNT_4_MAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and W3.SEX='2' THEN '1' END) AS PASS_CNT_4_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE='1' or W1.EXAM_TYPE='2' or W1.EXAM_TYPE='3') and VALUE(W2.JUDGEDIV, '0')='1' THEN '1' END) AS PASS_CNT_TOTAL,");
		// 一次入学手続き者数
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W2.PROCEDUREDIV1, '0')='1' and W3.SEX='1' THEN '1' END) AS NYU_1SIGN_CNT_2_MAN,");
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W2.PROCEDUREDIV1, '0')='1' and W3.SEX='2' THEN '1' END) AS NYU_1SIGN_CNT_2_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W2.PROCEDUREDIV1, '0')='1' and W3.SEX='1' THEN '1' END) AS NYU_1SIGN_CNT_4_MAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W2.PROCEDUREDIV1, '0')='1' and W3.SEX='2' THEN '1' END) AS NYU_1SIGN_CNT_4_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE='1' or W1.EXAM_TYPE='2' or W1.EXAM_TYPE='3') and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W2.PROCEDUREDIV1, '0')='1' THEN '1' END) AS NYU_1SIGN_CNT_TOTAL,");
		// 二次入学手続き者数
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.PROCEDUREDIV, '0')='1' and W3.SEX='1' THEN '1' END) AS NYU_2SIGN_CNT_2_MAN,");
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.PROCEDUREDIV, '0')='1' and W3.SEX='2' THEN '1' END) AS NYU_2SIGN_CNT_2_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.PROCEDUREDIV, '0')='1' and W3.SEX='1' THEN '1' END) AS NYU_2SIGN_CNT_4_MAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.PROCEDUREDIV, '0')='1' and W3.SEX='2' THEN '1' END) AS NYU_2SIGN_CNT_4_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE='1' or W1.EXAM_TYPE='2' or W1.EXAM_TYPE='3') and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.PROCEDUREDIV, '0')='1' THEN '1' END) AS NYU_2SIGN_CNT_TOTAL,");
		// 一次辞退者数
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W2.PROCEDUREDIV1, '0') = '1' and VALUE(W3.PROCEDUREDIV, '0') <> '1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='1' THEN '1' END) AS ZITAI1_CNT_2_MAN,");
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W2.PROCEDUREDIV1, '0') = '1' and VALUE(W3.PROCEDUREDIV, '0') <> '1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='2' THEN '1' END) AS ZITAI1_CNT_2_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W2.PROCEDUREDIV1, '0') = '1' and VALUE(W3.PROCEDUREDIV, '0') <> '1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='1' THEN '1' END) AS ZITAI1_CNT_4_MAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W2.PROCEDUREDIV1, '0') = '1' and VALUE(W3.PROCEDUREDIV, '0') <> '1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='2' THEN '1' END) AS ZITAI1_CNT_4_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE='1' or W1.EXAM_TYPE='2' or W1.EXAM_TYPE='3') and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W2.PROCEDUREDIV1, '0') = '1' and VALUE(W3.PROCEDUREDIV, '0') <> '1' and VALUE(W3.ENTDIV, '0')='2' THEN '1' END) AS ZITAI1_CNT_TOTAL,");
		// 二次辞退者数
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.PROCEDUREDIV, '0') = '1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='1' THEN '1' END) AS ZITAI2_CNT_2_MAN,");
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.PROCEDUREDIV, '0') = '1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='2' THEN '1' END) AS ZITAI2_CNT_2_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.PROCEDUREDIV, '0') = '1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='1' THEN '1' END) AS ZITAI2_CNT_4_MAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.PROCEDUREDIV, '0') = '1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='2' THEN '1' END) AS ZITAI2_CNT_4_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE='1' or W1.EXAM_TYPE='2' or W1.EXAM_TYPE='3') and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.PROCEDUREDIV, '0') = '1' and VALUE(W3.ENTDIV, '0')='2' THEN '1' END) AS ZITAI2_CNT_TOTAL,");
        // 総辞退者数
        stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='1' THEN '1' END) AS ZITAI_ALL_CNT_2_MAN,");
        stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='2' THEN '1' END) AS ZITAI_ALL_CNT_2_WOMAN,");
        stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='1' THEN '1' END) AS ZITAI_ALL_CNT_4_MAN,");
        stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.ENTDIV, '0')='2' and W3.SEX='2' THEN '1' END) AS ZITAI_ALL_CNT_4_WOMAN,");
        stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE='1' or W1.EXAM_TYPE='2' or W1.EXAM_TYPE='3') and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.ENTDIV, '0')='2' THEN '1' END) AS ZITAI_ALL_CNT_TOTAL,");
		// 入学者数
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.ENTDIV, '0')='1' and W3.SEX='1' THEN '1' END) AS NYU_CNT_2_MAN,");
		stb.append("    COUNT(CASE WHEN W1.EXAM_TYPE='1' and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.ENTDIV, '0')='1' and W3.SEX='2' THEN '1' END) AS NYU_CNT_2_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.ENTDIV, '0')='1' and W3.SEX='1' THEN '1' END) AS NYU_CNT_4_MAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE in "+examType+") and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.ENTDIV, '0')='1' and W3.SEX='2' THEN '1' END) AS NYU_CNT_4_WOMAN,");
		stb.append("    COUNT(CASE WHEN (W1.EXAM_TYPE='1' or W1.EXAM_TYPE='2' or W1.EXAM_TYPE='3') and VALUE(W2.JUDGEDIV, '0')='1' and VALUE(W3.ENTDIV, '0')='1' THEN '1' END) AS NYU_CNT_TOTAL");
		
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
        if (isRealApplicant) {
            stb.append("      left outer join T_JUDGEDIV_BEFORE W4 ON ");
            stb.append("         W1.ENTEXAMYEAR  = W4.ENTEXAMYEAR and ");       // 入試年度
            stb.append("         W1.APPLICANTDIV = W4.APPLICANTDIV and ");      // 入試制度
            stb.append("         W1.EXAMNO       = W4.EXAMNO ");                // 受験番号
        }
        stb.append(" where");
        stb.append("    W1.ENTEXAMYEAR='"+_param._year+"' ");                   // 入試年度
        if (! test_div.equals("0")) { // 合計計算時は年度の指定をしない
            stb.append("    and W1.TESTDIV = '"+test_div+"' ");
        }
        if (isRealApplicant) {  // 実志願者は前回の入試区分までに合格した人を省いた数
            stb.append("    and (W4.JUDGEDIV_BEFORE is null or W4.JUDGEDIV_BEFORE <> '1' )");
        }
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
        ResultSet rs = null;
        try {
            String sql = getTotalSql();
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                final student totalrec = getStudent(rs, false);
                rtnList.add(totalrec);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /** 合計数取得処理 */
    private String getTotalSql() throws SQLException {
        return getStudentSql("0", false);
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
            + "    COURSECD = '1' and"						// 課程コード
            + "    MAJORCD = '001' and"						// 学科コード
            + "    EXAMCOURSECD = '0001' "					// コースコード
            ;
        return rtn;
    }
    
    /** 名称マスタ抽出処理 */
    public HashMap getTestDate(DB2UDB db2) throws SQLException, Exception {
        final String sql = getTestDateSql();
        HashMap rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String code = rs.getString("NAMECD2");
                final String val = rs.getString("TEST_DATE");
                rtn.put(code, val);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    /** 入学試験日取得処理 */
    private String getTestDateSql() throws SQLException {
        final String rtn;

		rtn = " select"
            + "    NAMECD2,"
            + "    NAMESPARE1 AS TEST_DATE"
            + " from NAME_MST"
            + " where"
            + "    NAMECD1 = 'L004' "
            ;
        return rtn;
    }
    
    /** 生徒クラス */
    private class student {
        final String _sigan_cnt_2_man;
        final String _sigan_cnt_2_woman;
        final String _sigan_cnt_4_man;
        final String _sigan_cnt_4_woman;
        final String _sigan_cnt_total;
        final String _exam_cnt_2_man;
        final String _exam_cnt_2_woman;
        final String _exam_cnt_4_man;
        final String _exam_cnt_4_woman;
        final String _exam_cnt_total;
        final String _pass_cnt_2_man;
        final String _pass_cnt_2_woman;
        final String _pass_cnt_4_man;
        final String _pass_cnt_4_woman;
        final String _pass_cnt_total;

        final String _nyu_1sign_cnt_2_man;
        final String _nyu_1sign_cnt_2_woman;
        final String _nyu_1sign_cnt_4_man;
        final String _nyu_1sign_cnt_4_woman;
        final String _nyu_1sign_cnt_total;
        final String _nyu_2sign_cnt_2_man;
        final String _nyu_2sign_cnt_2_woman;
        final String _nyu_2sign_cnt_4_man;
        final String _nyu_2sign_cnt_4_woman;
        final String _nyu_2sign_cnt_total;
        final String _zitai1_cnt_2_man;
        final String _zitai1_cnt_2_woman;
        final String _zitai1_cnt_4_man;
        final String _zitai1_cnt_4_woman;
        final String _zitai1_cnt_total;
        final String _zitai2_cnt_2_man;
        final String _zitai2_cnt_2_woman;
        final String _zitai2_cnt_4_man;
        final String _zitai2_cnt_4_woman;
        final String _zitai2_cnt_total;
        final String _zitai_all_cnt_2_man;
        final String _zitai_all_cnt_2_woman;
        final String _zitai_all_cnt_4_man;
        final String _zitai_all_cnt_4_woman;
        final String _zitai_all_cnt_total;

        final String _nyu_cnt_2_man;
        final String _nyu_cnt_2_woman;
        final String _nyu_cnt_4_man;
        final String _nyu_cnt_4_woman;
        final String _nyu_cnt_total;
        
        final boolean _isRealApplicant;

        student(
                final String sigan_cnt_2_man,
                final String sigan_cnt_2_woman,
                final String sigan_cnt_4_man,
                final String sigan_cnt_4_woman,
                final String sigan_cnt_total,
                final String exam_cnt_2_man,
                final String exam_cnt_2_woman,
                final String exam_cnt_4_man,
                final String exam_cnt_4_woman,
                final String exam_cnt_total,
                final String pass_cnt_2_man,
                final String pass_cnt_2_woman,
                final String pass_cnt_4_man,
                final String pass_cnt_4_woman,
                final String pass_cnt_total,

                final String nyu_1sign_cnt_2_man,
                final String nyu_1sign_cnt_2_woman,
                final String nyu_1sign_cnt_4_man,
                final String nyu_1sign_cnt_4_woman,
                final String nyu_1sign_cnt_total,
                final String nyu_2sign_cnt_2_man,
                final String nyu_2sign_cnt_2_woman,
                final String nyu_2sign_cnt_4_man,
                final String nyu_2sign_cnt_4_woman,
                final String nyu_2sign_cnt_total,
                final String zitai1_cnt_2_man,
                final String zitai1_cnt_2_woman,
                final String zitai1_cnt_4_man,
                final String zitai1_cnt_4_woman,
                final String zitai1_cnt_total,
                final String zitai2_cnt_2_man,
                final String zitai2_cnt_2_woman,
                final String zitai2_cnt_4_man,
                final String zitai2_cnt_4_woman,
                final String zitai2_cnt_total,
                final String zitai_all_cnt_2_man,
                final String zitai_all_cnt_2_woman,
                final String zitai_all_cnt_4_man,
                final String zitai_all_cnt_4_woman,
                final String zitai_all_cnt_total,

                final String nyu_cnt_2_man,
                final String nyu_cnt_2_woman,
                final String nyu_cnt_4_man,
                final String nyu_cnt_4_woman,
                final String nyu_cnt_total,
                final boolean isRealApplicant
        ) {
			_sigan_cnt_2_man   = sigan_cnt_2_man;
			_sigan_cnt_2_woman = sigan_cnt_2_woman;
			_sigan_cnt_4_man   = sigan_cnt_4_man;
			_sigan_cnt_4_woman = sigan_cnt_4_woman;
			_sigan_cnt_total   = sigan_cnt_total;
			_exam_cnt_2_man    = exam_cnt_2_man;
			_exam_cnt_2_woman  = exam_cnt_2_woman;
			_exam_cnt_4_man    = exam_cnt_4_man;
			_exam_cnt_4_woman  = exam_cnt_4_woman;
			_exam_cnt_total    = exam_cnt_total;
			_pass_cnt_2_man    = pass_cnt_2_man;
			_pass_cnt_2_woman  = pass_cnt_2_woman;
			_pass_cnt_4_man    = pass_cnt_4_man;
			_pass_cnt_4_woman  = pass_cnt_4_woman;
			_pass_cnt_total    = pass_cnt_total;
			_nyu_1sign_cnt_2_man   = nyu_1sign_cnt_2_man;
			_nyu_1sign_cnt_2_woman   = nyu_1sign_cnt_2_woman;
			_nyu_1sign_cnt_4_man   = nyu_1sign_cnt_4_man;
			_nyu_1sign_cnt_4_woman   = nyu_1sign_cnt_4_woman;
			_nyu_1sign_cnt_total   = nyu_1sign_cnt_total;
			_nyu_2sign_cnt_2_man   = nyu_2sign_cnt_2_man;
			_nyu_2sign_cnt_2_woman   = nyu_2sign_cnt_2_woman;
			_nyu_2sign_cnt_4_man   = nyu_2sign_cnt_4_man;
			_nyu_2sign_cnt_4_woman   = nyu_2sign_cnt_4_woman;
			_nyu_2sign_cnt_total   = nyu_2sign_cnt_total;
			_zitai1_cnt_2_man   = zitai1_cnt_2_man;
			_zitai1_cnt_2_woman   = zitai1_cnt_2_woman;
			_zitai1_cnt_4_man   = zitai1_cnt_4_man;
			_zitai1_cnt_4_woman   = zitai1_cnt_4_woman;
			_zitai1_cnt_total   = zitai1_cnt_total;
			_zitai2_cnt_2_man   = zitai2_cnt_2_man;
			_zitai2_cnt_2_woman   = zitai2_cnt_2_woman;
			_zitai2_cnt_4_man   = zitai2_cnt_4_man;
			_zitai2_cnt_4_woman   = zitai2_cnt_4_woman;
			_zitai2_cnt_total   = zitai2_cnt_total;
            _zitai_all_cnt_2_man   = zitai_all_cnt_2_man;
            _zitai_all_cnt_2_woman   = zitai_all_cnt_2_woman;
            _zitai_all_cnt_4_man   = zitai_all_cnt_4_man;
            _zitai_all_cnt_4_woman   = zitai_all_cnt_4_woman;
            _zitai_all_cnt_total   = zitai_all_cnt_total;
			_nyu_cnt_2_man        = nyu_cnt_2_man;
			_nyu_cnt_2_woman      = nyu_cnt_2_woman;
			_nyu_cnt_4_man        = nyu_cnt_4_man;
			_nyu_cnt_4_woman      = nyu_cnt_4_woman;
			_nyu_cnt_total        = nyu_cnt_total;
            _isRealApplicant      = isRealApplicant;
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
    	private final String _print;
    	private final String _nendo;
    	private final String _date;
        private HashMap _hcapacity = new HashMap();
        private HashMap _testDate = new HashMap();
    	private int _siganTotalCount = 0;
    	private int _examTotalCount = 0;
    	private int _passTotalCount = 0;
    	private int _nyu_sign_TotalCount = 0;
    	private int _zitai_TotalCount = 0;
    	private int _nyu_TotalCount = 0;
    	private int _recordTotalCount = 0;
        private List _realApplicantList;
        private boolean _isInfluence;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _print = nvlT(request.getParameter("PRINT"));
            _nendo = KenjaProperties.gengou(Integer.parseInt(_year))+"年度";
            _date = KNJ_EditDate.h_format_JP(_loginDate);
            _isInfluence = isInfluence(db2);
        }


        private boolean isInfluence(DB2UDB db2) throws SQLException {
            boolean isInfluence = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            ps = db2.prepareStatement("SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '01' ");
            rs = ps.executeQuery();
            if (rs.next()) {
                isInfluence = "1".equals(rs.getString("NAMESPARE1"));
            }
            DbUtils.closeQuietly(null, ps, rs);
            return isInfluence;
        }
        
        /**
         * testdivより前に実施された試験区分のリストをSQLに整形して返す
         *     testdiv=5 -> testdivBefore= "('')"
         *     testdiv=1 -> testdivBefore= "('5')"
         *     testdiv=2 -> testdivBefore= "('5','1')"
         *     testdiv=3 -> testdivBefore= "('5','1','2')"
         *     testdiv=4 -> testdivBefore= "('5','1','2','3')"
         */
        private String getTestdivBeforeIn(String testdiv) {
            StringBuffer testDivBefore = new StringBuffer();
            testDivBefore.append("(");
            if ("5".equals(testdiv)) {
                testDivBefore.append("''");
            } else {
                testDivBefore.append("'5'");
                for(int i=1; i<=5; i++) {
                    if (testdiv.equals(String.valueOf(i))) {
                        break;
                    }
                    testDivBefore.append(",'"+String.valueOf(i)+"'");
                }
            }
            testDivBefore.append(")");
            return testDivBefore.toString();
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
     * 日付を指定されたフォーマットに設定し文字列にして返す
     * @param s
     * @return
     */
    private String fomatSakuseiDate(String cnvDate, String sfmt, String chgfmt) {

    	String retDate = "";
    	try {
			DateFormat foramt = new SimpleDateFormat(sfmt); 
			//文字列よりDate型へ変換
			Date date1 = foramt.parse(cnvDate); 
			// 年月日のフォーマットを指定
			SimpleDateFormat sdf1 = new SimpleDateFormat(chgfmt);
			// Date型より文字列へ変換
			retDate = sdf1.format(date1);
		} catch( Exception e ){
			log.error("setHeader set error!");
		}
		return retDate;
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

}
