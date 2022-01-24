package servletpack.KNJL;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
 * 入試出願状況推移表
 * 
 * @author nakasone
 *
 */
public class KNJL306J {
    private static final String FORM_NAME = "KNJL306J.frm";
    private static final String FORM_NAME2 = "KNJL306J_2.frm";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private String week[] = {"","日","月","火","水","木","金","土"};
	private	Calendar calw = Calendar.getInstance();
    private static final int TEST_DIV_CASE = 5;
    private static final String ZITU_TYPE = "1";
	private static final String TANGAN_TYPE = "2";
	private static final String FUKUSU_TYPE = "3";
	private static final String NOBE_TYPE = "4";
	private static final String A1_2_TYPE = "5";
	private static final String A1_4_TYPE = "6";
	private static final String A1_TOTAL_TYPE = "7";
	private static final String A2_2_TYPE = "8";
	private static final String B_2_TYPE = "9";
	private static final String B_4_TYPE = "10";
	private static final String B_TOTAL_TYPE = "11";
	private static final String C_4_TYPE = "12";
	private static final String KIKOKU_TYPE = "13";
    private static final String C_2_TYPE = "20";
    private static final String C_TOTAL_TYPE = "21";
    private static final String D_2_TYPE = "22";
    private static final String D_4_TYPE = "23";
    private static final String D_TOTAL_TYPE = "24";
	
	private static final String A1_KBN = "1";
	private static final String A2_KBN = "2";
	private static final String B_KBN = "3";
	private static final String C_KBN = "4";
	private static final String KIKOKU_KBN = "5";
    private static final String D_KBN = "6";
	private static final String A1_2KBN_NAME = "A1_2";
	private static final String A1_4KBN_NAME = "A1_4";
	private static final String A1_TOTAL_KBN_NAME = "A1";
	private static final String A2_KBN_NAME = "A2_2";
	private static final String B_2KBN_NAME = "B_2";
	private static final String B_4KBN_NAME = "B_4";
	private static final String B_TOTAL_KBN_NAME = "B";
	private static final String C_KBN_NAME = "C_4";
    private static final String C_2KBN_NAME = "C_2";
    private static final String C_4KBN_NAME = "C_4";
    private static final String C_TOTAL_KBN_NAME = "C";
    private static final String D_2KDN_NAME = "D_2";
    private static final String D_4KDN_NAME = "D_4";
    private static final String D_TOTAL_KDN_NAME = "D";
	private static final String KIKOKU_KBN_NAME = "KIKOKU";

	private static final String LIST_TYPE_STUDENT = "student";
	private static final String LIST_TYPE_DIVSTUDENT = "divstudent";

	private int reccnt_man = 0;
    private int reccnt_woman = 0;
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL306J.class);
    
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

    	sdf.applyPattern("yyyy-MM-dd");
    	boolean endflg = true;
    	int start_date;
    	int end_date;
    	int line = 1;
    	
        if (_param._isInfluence) {
            svf.VrSetForm(FORM_NAME2, 1);
        } else {
            svf.VrSetForm(FORM_NAME, 1);
        }

        // 指示画面よりの一般日付・帰国生日付の開始日付を求める
        if(_param._date1_from <= _param._date2_from){
        	start_date = _param._date1_from;
        } else {
        	start_date = _param._date2_from;
        }
        // 指示画面よりの一般日付・帰国生日付の終了日付を求める
        if(_param._date1_to >= _param._date2_to){
        	end_date = _param._date1_to;
        } else {
        	end_date = _param._date2_to;
        }
        
        // 月末日を取得
        String sStart_Date = fomatSakuseiDate(String.valueOf(start_date),"yyyymmdd", "yyyy-mm-dd");
        int last_month_date = getLastDay(sStart_Date);
        
        // 開始日・終了日を日付フォーマット(yyyy-mm-dd)に設定
        String sFrom_Date = fomatSakuseiDate(String.valueOf(start_date),"yyyymmdd", "yyyy-mm-dd");
        String sEnd_Date = fomatSakuseiDate(String.valueOf(end_date),"yyyymmdd", "yyyy-mm-dd");
        // 開始日・終了日を日付フォーマット(yyyy-mm-dd)に設定
        String sKIKOKU_From_Date = fomatSakuseiDate(String.valueOf(_param._date2_from),"yyyymmdd", "yyyy-mm-dd");
        String sKIKOKU_End_Date = fomatSakuseiDate(String.valueOf(_param._date2_to),"yyyymmdd", "yyyy-mm-dd");

		// *-------------------------------*
		// *  帳票出力データ取得処理       *
		// *-------------------------------*
		final List tangan_student  = createStudents(db2, ZITU_TYPE, 	sFrom_Date, sEnd_Date);	// 単願人数データ取得
		final List fukusu_student  = createStudents(db2, TANGAN_TYPE,	sFrom_Date, sEnd_Date);	// 複数回人数データ取得
		final List zitu_student    = createStudents(db2, FUKUSU_TYPE,	sFrom_Date, sEnd_Date);	// 実人数データ取得	
		final List a1_2_student    = createApplicantStudents(db2, sFrom_Date, sEnd_Date, A1_KBN, "1");	// 入試区分A1の2科データ
		final List a1_4_student		= createApplicantStudents(db2, sFrom_Date, sEnd_Date, A1_KBN, "2");	// 入試区分A1の4科データ
		final List a1_student		= createApplicantStudents(db2, sFrom_Date, sEnd_Date, A1_KBN, "sum");// 入試区分A1の合計データ
		final List a2_student    	= createApplicantStudents(db2, sFrom_Date, sEnd_Date, A2_KBN, "1");	// 入試区分A2の2科合計データ
		final List b_2_student    	= createApplicantStudents(db2, sFrom_Date, sEnd_Date, B_KBN, "1");	// 入試区分Bの2科データ
		final List b_4_student		= createApplicantStudents(db2, sFrom_Date, sEnd_Date, B_KBN, "2");	// 入試区分Bの4科データ
		final List b_student		= createApplicantStudents(db2, sFrom_Date, sEnd_Date, B_KBN, "sum");// 入試区分Bの合計データ
		final List kikoku_student  	= createApplicantStudents(db2, sKIKOKU_From_Date, sKIKOKU_End_Date, KIKOKU_KBN, "3");// 入試区分帰国生の3科合計データ
        final List c_2_student      = createApplicantStudents(db2, sFrom_Date, sEnd_Date, C_KBN, "1");  // 入試区分Cの2科合計データ
        final List c_4_student      = createApplicantStudents(db2, sFrom_Date, sEnd_Date, C_KBN, "2");  // 入試区分Cの4科合計データ
        final List c_student        = createApplicantStudents(db2, sFrom_Date, sEnd_Date, C_KBN, "sum");// 入試区分Cの合計データ
        final List d_2_student      = createApplicantStudents(db2, sFrom_Date, sEnd_Date, D_KBN, "1");  // 入試区分Dの2科合計データ
        final List d_4_student      = createApplicantStudents(db2, sFrom_Date, sEnd_Date, D_KBN, "2");  // 入試区分Dの4科合計データ
        final List d_student        = createApplicantStudents(db2, sFrom_Date, sEnd_Date, D_KBN, "sum");// 入試区分Dの合計データ

        
		// 対象データが存在しない場合
		if(zitu_student.size() <= 0){
			return;
		}
		
		// *-------------------------------*
		// *  帳票出力処理                 *
		// *-------------------------------*
		while(endflg){
    		// *--------------------*
			// *  改ページ          *
    		// *--------------------*
    		if(line > 26){
                if (!_param._isInfluence) {
                    setHeadTotalPrint(
                            svf,
                            tangan_student.size(),
                            fukusu_student.size(),
                            a1_2_student.size(),
                            a1_4_student.size(),
                            a1_student.size(),
                            a2_student.size(),
                            b_2_student.size(),
                            b_4_student.size(),
                            b_student.size(),
                            c_4_student.size(),
                            kikoku_student.size()
                        );
                } else {
                    setHeadTotalPrint2(
                            svf,
                            tangan_student.size(),
                            fukusu_student.size(),
                            a1_2_student.size(),
                            a1_4_student.size(),
                            a1_student.size(),
                            a2_student.size(),
                            b_2_student.size(),
                            b_4_student.size(),
                            b_student.size(),
                            c_2_student.size(),
                            c_4_student.size(),
                            c_student.size(),
                            d_2_student.size(),
                            d_4_student.size(),
                            d_student.size(),
                            kikoku_student.size()
                        );
                }
   	            svf.VrEndPage();
	            line = 1;
			}
    		// *--------------------*
			// *  受付日を設定      *
    		// *--------------------*
    		// 受付日
			svf.VrsOutn("ACCEPT_DATE", line, fomatSakuseiDate(String.valueOf(start_date),"yyyymmdd", "mm/dd"));
			// 日付を設定
		    calw.setTime(sdf.parse( fomatSakuseiDate(String.valueOf(start_date),"yyyymmdd", "yyyy-mm-dd")));
		    // 曜日
			svf.VrsOutn( "YOUBI", line, week[ calw.get( Calendar.DAY_OF_WEEK ) ] );
			
    		// *--------------------*
			// *  帳票出力          *
    		// *--------------------*
			if(zitu_student.size() > 0){	// 帳票出力：実人数データ
				outPutPrint(svf, LIST_TYPE_STUDENT, zitu_student, String.valueOf(start_date), String.valueOf(end_date), ZITU_TYPE, line);
			}
			if(tangan_student.size() > 0){	// 帳票出力：単願人数データ
				outPutPrint(svf, LIST_TYPE_STUDENT, tangan_student, String.valueOf(start_date), String.valueOf(end_date), TANGAN_TYPE, line);
			}
			if(fukusu_student.size() > 0){	// 帳票出力：複数回人数データ
				outPutPrint(svf, LIST_TYPE_STUDENT, fukusu_student, String.valueOf(start_date), String.valueOf(end_date), FUKUSU_TYPE, line);
			}
			if(a1_2_student.size() > 0){	// 帳票出力：Ａ１の2科データ
				outPutPrint(svf, LIST_TYPE_DIVSTUDENT, a1_2_student, String.valueOf(start_date), String.valueOf(end_date), A1_2_TYPE, line);
			}
			if(a1_4_student.size() > 0){	// 帳票出力：Ａ１の4科データ
				outPutPrint(svf, LIST_TYPE_DIVSTUDENT, a1_4_student, String.valueOf(start_date), String.valueOf(end_date), A1_4_TYPE, line);
			}
			if(a1_student.size() > 0){		// 帳票出力：Ａ１の合計データ
				outPutPrint(svf, LIST_TYPE_DIVSTUDENT, a1_student, String.valueOf(start_date), String.valueOf(end_date), A1_TOTAL_TYPE, line);
			}
			if(a2_student.size() > 0){		// 帳票出力：Ａ２の合計データ
				outPutPrint(svf, LIST_TYPE_DIVSTUDENT, a2_student, String.valueOf(start_date), String.valueOf(end_date), A2_2_TYPE, line);
			}
			if(b_2_student.size() > 0){	// 帳票出力：Ｂの2科データ
				outPutPrint(svf, LIST_TYPE_DIVSTUDENT, b_2_student, String.valueOf(start_date), String.valueOf(end_date), B_2_TYPE, line);
			}
			if(b_4_student.size() > 0){	// 帳票出力：Ｂの4科データ
				outPutPrint(svf, LIST_TYPE_DIVSTUDENT, b_4_student, String.valueOf(start_date), String.valueOf(end_date), B_4_TYPE, line);
			}
			if(b_student.size() > 0){		// 帳票出力：Ｂの合計データ
				outPutPrint(svf, LIST_TYPE_DIVSTUDENT, b_student, String.valueOf(start_date), String.valueOf(end_date), B_TOTAL_TYPE, line);
			}
            if(c_4_student.size() > 0){       // 帳票出力：Ｃの4科データ
                outPutPrint(svf, LIST_TYPE_DIVSTUDENT, c_4_student, String.valueOf(start_date), String.valueOf(end_date), C_4_TYPE, line);
            }
            if (_param._isInfluence) {
                if(c_2_student.size() > 0){ // 帳票出力：Ｃの2科データ
                    outPutPrint(svf, LIST_TYPE_DIVSTUDENT, c_2_student, String.valueOf(start_date), String.valueOf(end_date), C_2_TYPE, line);
                }
                if(c_student.size() > 0){       // 帳票出力：Ｃの合計データ
                    outPutPrint(svf, LIST_TYPE_DIVSTUDENT, c_student, String.valueOf(start_date), String.valueOf(end_date), C_TOTAL_TYPE, line);
                }
                if(d_2_student.size() > 0){ // 帳票出力：Ｄの2科データ
                    outPutPrint(svf, LIST_TYPE_DIVSTUDENT, d_2_student, String.valueOf(start_date), String.valueOf(end_date), D_2_TYPE, line);
                }
                if(d_4_student.size() > 0){ // 帳票出力：Ｄの4科データ
                    outPutPrint(svf, LIST_TYPE_DIVSTUDENT, d_4_student, String.valueOf(start_date), String.valueOf(end_date), D_4_TYPE, line);
                }
                if(d_student.size() > 0){       // 帳票出力：Ｄの合計データ
                    outPutPrint(svf, LIST_TYPE_DIVSTUDENT, d_student, String.valueOf(start_date), String.valueOf(end_date), D_TOTAL_TYPE, line);
                }
            }

			if(kikoku_student.size() > 0){	// 帳票出力：帰国生の合計データ
				outPutPrint(svf, LIST_TYPE_DIVSTUDENT, kikoku_student, String.valueOf(start_date), String.valueOf(end_date), KIKOKU_TYPE, line);
			}
        	// 延人数の編集を行う
			svf.VrsOutn("BOY_CNT4" 	, line, String.valueOf(_param.nobe_daytotal_man_count));	// 延人数(男)
			svf.VrsOutn("GIRL_CNT4" , line, String.valueOf(_param.nobe_daytotal_woman_count));	// 延人数(女)
    		svf.VrsOutn("TOTAL_CNT4", line, String.valueOf(_param.nobe_daytotal_count));		// 延人数(計)
    		_param.nobe_daytotal_man_count = 0;
    		_param.nobe_daytotal_woman_count = 0;
    		_param.nobe_daytotal_count = 0;
			
    		// *-------------------------------*
	        // *   年をまたがった場合          *
    		// *-------------------------------*
	        int iMMDD = Integer.parseInt(String.valueOf(start_date).substring(4,8));
	        if(iMMDD == 1231){
    	        int addYYYY = Integer.valueOf(String.valueOf(start_date).substring(0,4)).intValue();
    	        String setYYYY = String.valueOf(++addYYYY);
    	        String setMM = "01";
    	        String setDD = "01";
    	        start_date = Integer.parseInt(setYYYY + setMM + setDD);
    	        // 月の月末日を取得
    	        sStart_Date = fomatSakuseiDate(String.valueOf(start_date),"yyyymmdd", "yyyy-mm-dd");
    	        last_month_date = getLastDay(sStart_Date);
	        } else {
	    		// *-------------------------------*
		        // *   日付が月末日を越えた場合    *
	    		// *-------------------------------*
		        int iDD = Integer.parseInt(String.valueOf(start_date).substring(6,8));
		        if(iDD == last_month_date){
	    	        // 翌月の月初を設定
		        	String setYYYY = String.valueOf(start_date).substring(0,4);
	    	        int addMM   = Integer.valueOf(String.valueOf(start_date).substring(4,6)).intValue();
	    	        String setMM = String.valueOf(++addMM);
	    	        String setDD   = "01";
	    	        start_date = Integer.parseInt(setYYYY + getNumberFormat("00",setMM) + setDD);
	    	        // 月の月末日を取得
	    	        sStart_Date = fomatSakuseiDate(String.valueOf(start_date),"yyyymmdd", "yyyy-mm-dd");
	    	        last_month_date = getLastDay(sStart_Date);
	        	} else {
	    			++start_date;
	        	}
	        }
    		++line;
    		// *-------------------------------*
        	// *   最終日の場合                *
    		// *-------------------------------*
        	if(start_date > end_date){
                if (!_param._isInfluence) {
                    setHeadTotalPrint(
                            svf,
                            tangan_student.size(),
                            fukusu_student.size(),
                            a1_2_student.size(),
                            a1_4_student.size(),
                            a1_student.size(),
                            a2_student.size(),
                            b_2_student.size(),
                            b_4_student.size(),
                            b_student.size(),
                            c_student.size(),
                            kikoku_student.size()
                        );
                } else {
                    setHeadTotalPrint2(
                            svf,
                            tangan_student.size(),
                            fukusu_student.size(),
                            a1_2_student.size(),
                            a1_4_student.size(),
                            a1_student.size(),
                            a2_student.size(),
                            b_2_student.size(),
                            b_4_student.size(),
                            b_student.size(),
                            c_2_student.size(),
                            c_4_student.size(),
                            c_student.size(),
                            d_2_student.size(),
                            d_4_student.size(),
                            d_student.size(),
                            kikoku_student.size()
                        );
                }
        		endflg = false;
        	}
        }
		// *-------------------------------*
    	// *   最終行の場合                *
		// *-------------------------------*
    	if(line <= 26){
    		endflg = true;
    		while(endflg){
        		if(line > 26){
    	            endflg = false;
    			} else {
                	// 受付日にハイフンを設定(文字化けを対応するためユニコードにて設定)
        			svf.VrsOutn("ACCEPT_DATE" 	, line, "\uFF0D");
        			svf.VrsOutn("YOUBI" 	, line, "");
        			++line;
    			}
            }
    	}
        svf.VrEndPage();

        
    }
    
    /**
     * 帳票出力処理
     * @param svf			帳票オブジェクト
     * @param student		帳票出力対象クラスオブジェクト
     * @param recept_date	出力対象受付日付
     * @param end_date		出力最終日付
     * @param out_line		出力タイプ
     * @param icol			帳票出力列数
     */
    private void outPutPrint(
    	Vrw32alp svf,
    	String list_type,
    	List printstudent, 
    	String recept_date, 
    	String end_date, 
    	String out_line, 
    	int icol)
    {
    	boolean hit_flg = false;            
    	int man_count = 0;				// 男子カウント用
    	int woman_count = 0;			// 女子カウント用
    	int Total_man_count = 0;		// 合計：男子カウント用
    	int Total_woman_count = 0;		// 合計：女子カウント用
    	student sudent;
    	div_student divsudent;
        // 対象日付をフォーマット
        String sDate = fomatSakuseiDate(recept_date,"yyyymmdd", "yyyy-mm-dd");

        for (Iterator it = printstudent.iterator(); it.hasNext();) {
        	// *-------------------------------------------*
        	// * 実人数・複数回人数・単願人数の出力を行う  *
        	// *-------------------------------------------*
        	if(list_type.equals(LIST_TYPE_STUDENT)){
        		sudent = (student) it.next();

                if(sDate.equals(sudent._receptdate)){
                	if(sudent._sex.equals("1")){
                    	// 男子
            			svf.VrsOutn("BOY_CNT"+out_line 	, icol, sudent._cexamno_cnt);
            			man_count = Integer.valueOf(sudent._cexamno_cnt).intValue();
            			Total_man_count += man_count;
                	}
                	if(sudent._sex.equals("2")){
                    	// 女子
            			svf.VrsOutn("GIRL_CNT"+out_line	, icol, sudent._cexamno_cnt);
            			woman_count = Integer.valueOf(sudent._cexamno_cnt).intValue();
            			Total_woman_count += woman_count;
                	}
                	hit_flg = true;            
                }
            }
        	// *-------------------------------------*
        	// *  各入試区分毎の出力を行う           *
        	// *-------------------------------------*
        	if(list_type.equals(LIST_TYPE_DIVSTUDENT)){
        		divsudent = (div_student) it.next();
            	
                if(sDate.equals(divsudent._receptdate)){
            		
                	if(divsudent._sex.equals("1")){
                    	// 男子
            			svf.VrsOutn("BOY_CNT"+out_line 	, icol, divsudent._examno_cnt);
            			man_count = Integer.valueOf(divsudent._examno_cnt).intValue();
            			Total_man_count += man_count;
            			// 合計行の場合、延べ人数をカウント
            			if(isOutputTotalLine(out_line)){
                			_param.nobe_daytotal_man_count += man_count;
            			}
                	}
                	if(divsudent._sex.equals("2")){
                    	// 女子
            			svf.VrsOutn("GIRL_CNT"+out_line	, icol, divsudent._examno_cnt);
            			woman_count = Integer.valueOf(divsudent._examno_cnt).intValue();
            			Total_woman_count += woman_count;
            			// 合計行の場合、延べ人数をカウント
            			if(isOutputTotalLine(out_line)){
                			_param.nobe_daytotal_woman_count += woman_count;
            			}
                	}
                	hit_flg = true;            
                }
            }
        }
        
        // 対象受付日付に設定データが存在する場合
        if(hit_flg){
        	if(man_count == 0){
    			svf.VrsOutn("BOY_CNT"+out_line 	, icol, "0");
    			svf.VrsOutn("BOY_CNT4" 	, icol, "0");
        	}
        	if(woman_count == 0){
    			svf.VrsOutn("GIRL_CNT"+out_line 	, icol, "0");
    			svf.VrsOutn("GIRL_CNT4" 	, icol, "0");
        	}
        	// 各受付日の合計を設定
        	int total_count = Total_man_count + Total_woman_count;
    		svf.VrsOutn("TOTAL_CNT"+out_line 	, icol, String.valueOf(total_count));
    		// 入試区分毎の場合のみ延人数の合計数をカウントする
        	if(list_type.equals(LIST_TYPE_DIVSTUDENT)){
    			// 合計行の場合、延べ人数をカウント
    			if(isOutputTotalLine(out_line)){
        			_param.nobe_daytotal_count += total_count;
    			}
        	}
    		// 合計をカウント
    		countTotal(out_line,man_count,woman_count,total_count);

    		_hasData = true;            
        } else {
        	// 対象受付日付に対象データが存在し且つ対象最終日付以前の場合
        	if(Integer.parseInt(recept_date) <= Integer.parseInt((end_date))){
        		// 初期値設定処理
        		setiniprint(svf, out_line, icol);
        	}
        }
    }
    
    private boolean isOutputTotalLine(String out_line) {
        if (!_param._isInfluence) {
            return out_line.equals(A1_TOTAL_TYPE) || out_line.equals(A2_2_TYPE) || out_line.equals(B_TOTAL_TYPE) ||
            out_line.equals(C_4_TYPE) || out_line.equals(KIKOKU_TYPE);
        } else {
            return out_line.equals(A1_TOTAL_TYPE) || out_line.equals(A2_2_TYPE) || out_line.equals(B_TOTAL_TYPE) ||
            out_line.equals(C_TOTAL_TYPE) || out_line.equals(D_TOTAL_TYPE) || out_line.equals(KIKOKU_TYPE);
        }
    }

    /**
     * 帳票出力処理　合計列
     * @param svf			帳票オブジェクト
     * @param icol			帳票出力列数
     */
    private void setHeadTotalPrint(
    		final Vrw32alp svf,
    		final int tangan_count,
    		final int fukusu_count,
    		final int a1_2_count,
    		final int a1_4_count,
    		final int a1_count,
    		final int a2_count,
    		final int b_2_count,
    		final int b_4_count,
    		final int b_count,
    		final int c_count,
    		final int kikoku_count
    		){
		//*================*
		//* ヘッダ・フッタ *
		//*================*
		svf.VrsOut("NENDO" 		,_param._nendo);					// 対象年度
		svf.VrsOut("DATE"	 	,_param._date);						// 作成日

		//*================*
		//* 合計行         *
		//*================*
		// 実人数
		svf.VrsOutn("BOY_CNT1", 27, String.valueOf(_param.zitu_total_man_count));
		svf.VrsOutn("GIRL_CNT1", 27, String.valueOf(_param.zitu_total_woman_count));
		svf.VrsOutn("TOTAL_CNT1", 27, String.valueOf(_param.zitu_total_count));
		// 単願人数
		if(tangan_count > 0){
			svf.VrsOutn("BOY_CNT2", 27, String.valueOf(_param.tangan_total_man_count));
			svf.VrsOutn("GIRL_CNT2", 27, String.valueOf(_param.tangan_total_woman_count));
			svf.VrsOutn("TOTAL_CNT2", 27, String.valueOf(_param.tangan_total_count));
		}
		// 複数回人数
		if(fukusu_count > 0){
			svf.VrsOutn("BOY_CNT3", 27, String.valueOf(_param.fukusu_total_man_count));
			svf.VrsOutn("GIRL_CNT3", 27, String.valueOf(_param.fukusu_total_woman_count));
			svf.VrsOutn("TOTAL_CNT3", 27, String.valueOf(_param.fukusu_total_count));
		}
		// A1の2科人数
		if(a1_2_count > 0){
			svf.VrsOutn("BOY_CNT5", 27, String.valueOf(_param.a1_2_total_man_count));
			svf.VrsOutn("GIRL_CNT5", 27, String.valueOf(_param.a1_2_total_woman_count));
			svf.VrsOutn("TOTAL_CNT5", 27, String.valueOf(_param.a1_2_total_count));
		}
		// A1の4科人数
		if(a1_4_count > 0){
			svf.VrsOutn("BOY_CNT6", 27, String.valueOf(_param.a1_4_total_man_count));
			svf.VrsOutn("GIRL_CNT6", 27, String.valueOf(_param.a1_4_total_woman_count));
			svf.VrsOutn("TOTAL_CNT6", 27, String.valueOf(_param.a1_4_total_count));
		}
		// A1の合計人数
		if(a1_count > 0){
			svf.VrsOutn("BOY_CNT7", 27, String.valueOf(_param.a1_total_man_count));
			svf.VrsOutn("GIRL_CNT7", 27, String.valueOf(_param.a1_total_woman_count));
			svf.VrsOutn("TOTAL_CNT7", 27, String.valueOf(_param.a1_total_count));
		}
		// A2の2科人数
		if(a2_count > 0){
			svf.VrsOutn("BOY_CNT8", 27, String.valueOf(_param.a2_2_total_man_count));
			svf.VrsOutn("GIRL_CNT8", 27, String.valueOf(_param.a2_2_total_woman_count));
			svf.VrsOutn("TOTAL_CNT8", 27, String.valueOf(_param.a2_2_total_count));
		}
		// Bの2科人数
		if(b_2_count > 0){
			svf.VrsOutn("BOY_CNT9", 27, String.valueOf(_param.b_2_total_man_count));
			svf.VrsOutn("GIRL_CNT9", 27, String.valueOf(_param.b_2_total_woman_count));
			svf.VrsOutn("TOTAL_CNT9", 27, String.valueOf(_param.b_2_total_count));
		}
		// Bの4科人数
		if(b_4_count > 0){
			svf.VrsOutn("BOY_CNT10", 27, String.valueOf(_param.b_4_total_man_count));
			svf.VrsOutn("GIRL_CNT10", 27, String.valueOf(_param.b_4_total_woman_count));
			svf.VrsOutn("TOTAL_CNT10", 27, String.valueOf(_param.b_4_total_count));
		}
		// Bの合計人数
		if(b_count > 0){
			svf.VrsOutn("BOY_CNT11", 27, String.valueOf(_param.b_total_man_count));
			svf.VrsOutn("GIRL_CNT11", 27, String.valueOf(_param.b_total_woman_count));
			svf.VrsOutn("TOTAL_CNT11", 27, String.valueOf(_param.b_total_count));
		}
		// Cの4科人数
		if(c_count > 0){
			svf.VrsOutn("BOY_CNT12", 27, String.valueOf(_param.c_4_total_man_count));
			svf.VrsOutn("GIRL_CNT12", 27, String.valueOf(_param.c_4_total_woman_count));
			svf.VrsOutn("TOTAL_CNT12", 27, String.valueOf(_param.c_4_total_count));
		}
		// 帰国生の3科人数
		if(kikoku_count > 0){
			svf.VrsOutn("BOY_CNT13", 27, String.valueOf(_param.kikoku_total_man_count));
			svf.VrsOutn("GIRL_CNT13", 27, String.valueOf(_param.kikoku_total_woman_count));
			svf.VrsOutn("TOTAL_CNT13", 27, String.valueOf(_param.kikoku_total_count));
		}
		// 延人数
		svf.VrsOutn("BOY_CNT4", 27, String.valueOf(_param.nobe_total_man_count));
		svf.VrsOutn("GIRL_CNT4", 27, String.valueOf(_param.nobe_total_woman_count));
		svf.VrsOutn("TOTAL_CNT4", 27, String.valueOf(_param.nobe_total_count));
		
		// 合計行用のカウンターをクリア
//		_param.setCountinit();

    }
    
    
    /**
     * 帳票出力処理　合計列
     * @param svf           帳票オブジェクト
     * @param icol          帳票出力列数
     */
    private void setHeadTotalPrint2(
            final Vrw32alp svf,
            final int tangan_count,
            final int fukusu_count,
            final int a1_2_count,
            final int a1_4_count,
            final int a1_count,
            final int a2_count,
            final int b_2_count,
            final int b_4_count,
            final int b_count,
            final int c_2_count,
            final int c_4_count,
            final int c_count,
            final int d_2_count,
            final int d_4_count,
            final int d_count,
            final int kikoku_count
            ){
        //*================*
        //* ヘッダ・フッタ *
        //*================*
        svf.VrsOut("NENDO"      ,_param._nendo);                    // 対象年度
        svf.VrsOut("DATE"       ,_param._date);                     // 作成日

        //*================*
        //* 合計行         *
        //*================*
        // 実人数
        svf.VrsOutn("BOY_CNT1", 27, String.valueOf(_param.zitu_total_man_count));
        svf.VrsOutn("GIRL_CNT1", 27, String.valueOf(_param.zitu_total_woman_count));
        svf.VrsOutn("TOTAL_CNT1", 27, String.valueOf(_param.zitu_total_count));
        // 単願人数
        if(tangan_count > 0){
            svf.VrsOutn("BOY_CNT2", 27, String.valueOf(_param.tangan_total_man_count));
            svf.VrsOutn("GIRL_CNT2", 27, String.valueOf(_param.tangan_total_woman_count));
            svf.VrsOutn("TOTAL_CNT2", 27, String.valueOf(_param.tangan_total_count));
        }
        // 複数回人数
        if(fukusu_count > 0){
            svf.VrsOutn("BOY_CNT3", 27, String.valueOf(_param.fukusu_total_man_count));
            svf.VrsOutn("GIRL_CNT3", 27, String.valueOf(_param.fukusu_total_woman_count));
            svf.VrsOutn("TOTAL_CNT3", 27, String.valueOf(_param.fukusu_total_count));
        }
        // A1の2科人数
        if(a1_2_count > 0){
            svf.VrsOutn("BOY_CNT5", 27, String.valueOf(_param.a1_2_total_man_count));
            svf.VrsOutn("GIRL_CNT5", 27, String.valueOf(_param.a1_2_total_woman_count));
            svf.VrsOutn("TOTAL_CNT5", 27, String.valueOf(_param.a1_2_total_count));
        }
        // A1の4科人数
        if(a1_4_count > 0){
            svf.VrsOutn("BOY_CNT6", 27, String.valueOf(_param.a1_4_total_man_count));
            svf.VrsOutn("GIRL_CNT6", 27, String.valueOf(_param.a1_4_total_woman_count));
            svf.VrsOutn("TOTAL_CNT6", 27, String.valueOf(_param.a1_4_total_count));
        }
        // A1の合計人数
        if(a1_count > 0){
            svf.VrsOutn("BOY_CNT7", 27, String.valueOf(_param.a1_total_man_count));
            svf.VrsOutn("GIRL_CNT7", 27, String.valueOf(_param.a1_total_woman_count));
            svf.VrsOutn("TOTAL_CNT7", 27, String.valueOf(_param.a1_total_count));
        }
        // A2の2科人数
        if(a2_count > 0){
            svf.VrsOutn("BOY_CNT8", 27, String.valueOf(_param.a2_2_total_man_count));
            svf.VrsOutn("GIRL_CNT8", 27, String.valueOf(_param.a2_2_total_woman_count));
            svf.VrsOutn("TOTAL_CNT8", 27, String.valueOf(_param.a2_2_total_count));
        }
        // Bの2科人数
        if(b_2_count > 0){
            svf.VrsOutn("BOY_CNT9", 27, String.valueOf(_param.b_2_total_man_count));
            svf.VrsOutn("GIRL_CNT9", 27, String.valueOf(_param.b_2_total_woman_count));
            svf.VrsOutn("TOTAL_CNT9", 27, String.valueOf(_param.b_2_total_count));
        }
        // Bの4科人数
        if(b_4_count > 0){
            svf.VrsOutn("BOY_CNT10", 27, String.valueOf(_param.b_4_total_man_count));
            svf.VrsOutn("GIRL_CNT10", 27, String.valueOf(_param.b_4_total_woman_count));
            svf.VrsOutn("TOTAL_CNT10", 27, String.valueOf(_param.b_4_total_count));
        }
        // Bの合計人数
        if(b_count > 0){
            svf.VrsOutn("BOY_CNT11", 27, String.valueOf(_param.b_total_man_count));
            svf.VrsOutn("GIRL_CNT11", 27, String.valueOf(_param.b_total_woman_count));
            svf.VrsOutn("TOTAL_CNT11", 27, String.valueOf(_param.b_total_count));
        }
        // Cの2科人数
        if(c_2_count > 0){
            svf.VrsOutn("BOY_CNT" + C_2_TYPE, 27, String.valueOf(_param.c_2_total_man_count));
            svf.VrsOutn("GIRL_CNT" + C_2_TYPE, 27, String.valueOf(_param.c_2_total_woman_count));
            svf.VrsOutn("TOTAL_CNT" + C_2_TYPE, 27, String.valueOf(_param.c_2_total_count));
        }
        // Cの4科人数
        if(c_4_count > 0){
            svf.VrsOutn("BOY_CNT" + C_4_TYPE, 27, String.valueOf(_param.c_4_total_man_count));
            svf.VrsOutn("GIRL_CNT" + C_4_TYPE, 27, String.valueOf(_param.c_4_total_woman_count));
            svf.VrsOutn("TOTAL_CNT" + C_4_TYPE, 27, String.valueOf(_param.c_4_total_count));
        }
        // Cの合計人数
        if(c_count > 0){
            svf.VrsOutn("BOY_CNT" + C_TOTAL_TYPE, 27, String.valueOf(_param.c_total_man_count));
            svf.VrsOutn("GIRL_CNT" + C_TOTAL_TYPE, 27, String.valueOf(_param.c_total_woman_count));
            svf.VrsOutn("TOTAL_CNT" + C_TOTAL_TYPE, 27, String.valueOf(_param.c_total_count));
        }
        // Dの2科人数
        if(d_2_count > 0){
            svf.VrsOutn("BOY_CNT" + D_2_TYPE, 27, String.valueOf(_param.d_2_total_man_count));
            svf.VrsOutn("GIRL_CNT" + D_2_TYPE, 27, String.valueOf(_param.d_2_total_woman_count));
            svf.VrsOutn("TOTAL_CNT" + D_2_TYPE, 27, String.valueOf(_param.d_2_total_count));
        }
        // Dの4科人数
        if(d_4_count > 0){
            svf.VrsOutn("BOY_CNT" + D_4_TYPE, 27, String.valueOf(_param.d_4_total_man_count));
            svf.VrsOutn("GIRL_CNT" + D_4_TYPE, 27, String.valueOf(_param.d_4_total_woman_count));
            svf.VrsOutn("TOTAL_CNT" + D_4_TYPE, 27, String.valueOf(_param.d_4_total_count));
        }
        // Dの合計人数
        if(d_count > 0){
            svf.VrsOutn("BOY_CNT" + D_TOTAL_TYPE, 27, String.valueOf(_param.d_total_man_count));
            svf.VrsOutn("GIRL_CNT" + D_TOTAL_TYPE, 27, String.valueOf(_param.d_total_woman_count));
            svf.VrsOutn("TOTAL_CNT" + D_TOTAL_TYPE, 27, String.valueOf(_param.d_total_count));
        }
        // 帰国生の3科人数
        if(kikoku_count > 0){
            svf.VrsOutn("BOY_CNT13", 27, String.valueOf(_param.kikoku_total_man_count));
            svf.VrsOutn("GIRL_CNT13", 27, String.valueOf(_param.kikoku_total_woman_count));
            svf.VrsOutn("TOTAL_CNT13", 27, String.valueOf(_param.kikoku_total_count));
        }
        // 延人数
        svf.VrsOutn("BOY_CNT4", 27, String.valueOf(_param.nobe_total_man_count));
        svf.VrsOutn("GIRL_CNT4", 27, String.valueOf(_param.nobe_total_woman_count));
        svf.VrsOutn("TOTAL_CNT4", 27, String.valueOf(_param.nobe_total_count));
        // 合計行用のカウンターをクリア
//      _param.setCountinit();

    }

    /**
     * 帳票出力処理　合計カウント
     * @param man_count	男子計
     * @param woman_count	女子計
     * @param total_count	男女計
     */
    private void countTotal(
       	final String out_type, 
    	final int man_count, 
    	final int woman_count, 
    	final int total_count)
    {
    	
		if(out_type.equals(ZITU_TYPE)){	// 実人数をカウント
			_param.zitu_total_man_count += man_count;
			_param.zitu_total_woman_count += woman_count;
			_param.zitu_total_count += total_count;
		}
		if(out_type.equals(TANGAN_TYPE)){	// 単願人数をカウント
			_param.tangan_total_man_count += man_count;
			_param.tangan_total_woman_count += woman_count;
			_param.tangan_total_count += total_count;
		}
		if(out_type.equals(FUKUSU_TYPE)){	// 複数回人数をカウント
			_param.fukusu_total_man_count += man_count;
			_param.fukusu_total_woman_count += woman_count;
			_param.fukusu_total_count += total_count;
		}
		if(out_type.equals(A1_2_TYPE)){	// A1の2科人数をカウント
			_param.a1_2_total_man_count += man_count;
			_param.a1_2_total_woman_count += woman_count;
			_param.a1_2_total_count += total_count;
		}
		if(out_type.equals(A1_4_TYPE)){	// A1の4科人数をカウント
			_param.a1_4_total_man_count += man_count;
			_param.a1_4_total_woman_count += woman_count;
			_param.a1_4_total_count += total_count;
		}
		if(out_type.equals(A1_TOTAL_TYPE)){	// A1の合計人数をカウント
			_param.a1_total_man_count += man_count;
			_param.a1_total_woman_count += woman_count;
			_param.a1_total_count += total_count;
			_param.nobe_total_man_count += man_count;
			_param.nobe_total_woman_count += woman_count;
			_param.nobe_total_count += total_count;
		}
		if(out_type.equals(A2_2_TYPE)){	// A2の2科人数をカウント
			_param.a2_2_total_man_count += man_count;
			_param.a2_2_total_woman_count += woman_count;
			_param.a2_2_total_count += total_count;
			_param.nobe_total_man_count += man_count;
			_param.nobe_total_woman_count += woman_count;
			_param.nobe_total_count += total_count;
		}
		if(out_type.equals(B_2_TYPE)){	// Bの2科人数をカウント
			_param.b_2_total_man_count += man_count;
			_param.b_2_total_woman_count += woman_count;
			_param.b_2_total_count += total_count;
		}
		if(out_type.equals(B_4_TYPE)){	// Bの4科人数をカウント
			_param.b_4_total_man_count += man_count;
			_param.b_4_total_woman_count += woman_count;
			_param.b_4_total_count += total_count;
		}
		if(out_type.equals(B_TOTAL_TYPE)){	// Bの合計人数をカウント
			_param.b_total_man_count += man_count;
			_param.b_total_woman_count += woman_count;
			_param.b_total_count += total_count;
			_param.nobe_total_man_count += man_count;
			_param.nobe_total_woman_count += woman_count;
			_param.nobe_total_count += total_count;
		}
        if (!_param._isInfluence) {
            if(out_type.equals(C_4_TYPE)){  // Cの4科人数をカウント
                _param.c_4_total_man_count += man_count;
                _param.c_4_total_woman_count += woman_count;
                _param.c_4_total_count += total_count;
                _param.nobe_total_man_count += man_count;
                _param.nobe_total_woman_count += woman_count;
                _param.nobe_total_count += total_count;
            }
        } else {
            if(out_type.equals(C_2_TYPE)){  // Cの2科人数をカウント
                _param.c_2_total_man_count += man_count;
                _param.c_2_total_woman_count += woman_count;
                _param.c_2_total_count += total_count;
            }
            if(out_type.equals(C_4_TYPE)){  // Cの4科人数をカウント
                _param.c_4_total_man_count += man_count;
                _param.c_4_total_woman_count += woman_count;
                _param.c_4_total_count += total_count;
            }
            if(out_type.equals(C_TOTAL_TYPE)){  // Cの合計人数をカウント
                _param.c_total_man_count += man_count;
                _param.c_total_woman_count += woman_count;
                _param.c_total_count += total_count;
                _param.nobe_total_man_count += man_count;
                _param.nobe_total_woman_count += woman_count;
                _param.nobe_total_count += total_count;
            }
        }
        if(out_type.equals(D_2_TYPE)){  // Dの2科人数をカウント
            _param.d_2_total_man_count += man_count;
            _param.d_2_total_woman_count += woman_count;
            _param.d_2_total_count += total_count;
        }
        if(out_type.equals(D_4_TYPE)){  // Dの4科人数をカウント
            _param.d_4_total_man_count += man_count;
            _param.d_4_total_woman_count += woman_count;
            _param.d_4_total_count += total_count;
        }
        if(out_type.equals(D_TOTAL_TYPE)){  // Dの合計人数をカウント
            _param.d_total_man_count += man_count;
            _param.d_total_woman_count += woman_count;
            _param.d_total_count += total_count;
            _param.nobe_total_man_count += man_count;
            _param.nobe_total_woman_count += woman_count;
            _param.nobe_total_count += total_count;
        }

        if(out_type.equals(KIKOKU_TYPE)){   // 帰国生の人数をカウント
            _param.kikoku_total_man_count += man_count;
            _param.kikoku_total_woman_count += woman_count;
            _param.kikoku_total_count += total_count;
            _param.nobe_total_man_count += man_count;
            _param.nobe_total_woman_count += woman_count;
            _param.nobe_total_count += total_count;
        }

    }
    
    /**
     * 初期値設定処理
     * @param out_line		出力タイプ
     * @param test_div		出願タイプ(1:2科 2:4科)
     */
    private void setiniprint(final Vrw32alp svf, final String out_line, final int icol)
    {
    	// 受付日に対象データが存在しな場合
    	svf.VrsOutn("BOY_CNT"+out_line 			, icol, "0");
		svf.VrsOutn("GIRL_CNT"+out_line 		, icol, "0");
		svf.VrsOutn("TOTAL_CNT"+out_line		, icol, "0");
		svf.VrsOutn("BOY_CNT4" 			, icol, "0");
		svf.VrsOutn("GIRL_CNT4" 		, icol, "0");
		svf.VrsOutn("TOTAL_CNT4"		, icol, "0");
    }
    
    /**
     * 実人数・単願人数・複数回人数 取得処理
     * @param db2			ＤＢ接続オブジェクト
     * @param test_Type	テストタイプ(1:単願 2:複数回 3:全て)
     * @param date_from	開始日付
     * @param date_to		終了日付
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudents(final DB2UDB db2,	final String test_Type,	 final String date_from, final String date_to)
    	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getStudentSql(test_Type, date_from, date_to);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final student student = new student(
                		rs.getString("RECEPTDATE"),
                		rs.getString("SEX"),
						rs.getString("EXAMNO_CNT")
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
     * 入試区分毎の帳票出力対象データ取得処理
     * @param db2			ＤＢ接続オブジェクト
     * @param test_Type	テストタイプ(対象の出願を判定)
     * @param date_from	開始日付
     * @param date_to		終了日付
     * @param test_div		出願区分値
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createApplicantStudents(
    	final DB2UDB db2,	
    	final String date_from, 
    	final String date_to, 
    	final String test_div_name, 
    	final String test_div)
    	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getApplicantSql(date_from, date_to, test_div_name, test_div);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final div_student div_student = new div_student(
                		rs.getString("RECEPTDATE"),
                		rs.getString("SEX"),
						rs.getString("EXAMNO_CNT")
                );
                rtnList.add(div_student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @param test_Type	テストタイプ(1:単願 2:複数回 3:全て)
     * @param date_from	開始日付
     * @param date_to		終了日付
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql(final String test_Type, final String date_from, final String date_to){
		StringBuffer stb = new StringBuffer();
		stb.append("WITH BASE1 AS ( ");
		// 入試区分：Ａ１
		stb.append("  select ");
		stb.append("        EXAMNO,");
		stb.append("        '1' AS TESTDIV");
		stb.append("  from   ENTEXAM_APPLICANTBASE_DAT ");
		stb.append("  where  ENTEXAMYEAR='"+_param._year+"' and");
		stb.append("        TESTDIV1 is not null ");
		stb.append("  union all ");
		// 入試区分：Ａ２
		stb.append("  select ");
		stb.append("        EXAMNO,");
		stb.append("        '2' AS TESTDIV");
		stb.append("  from   ENTEXAM_APPLICANTBASE_DAT ");
		stb.append("  where  ENTEXAMYEAR='"+_param._year+"' and");
		stb.append("        TESTDIV2 is not null ");
		stb.append("  union all ");
		// 入試区分：Ｂ
		stb.append("  select ");
		stb.append("        EXAMNO,");
		stb.append("        '3' AS TESTDIV");
		stb.append("  from   ENTEXAM_APPLICANTBASE_DAT ");
		stb.append("  where  ENTEXAMYEAR='"+_param._year+"' and");
		stb.append("        TESTDIV3 is not null ");
		stb.append("  union all ");
		// 入試区分：Ｃ
		stb.append("  select ");
		stb.append("        EXAMNO,");
		stb.append("        '4' AS TESTDIV");
		stb.append("  from   ENTEXAM_APPLICANTBASE_DAT ");
		stb.append("  where  ENTEXAMYEAR='"+_param._year+"' and");
		stb.append("        TESTDIV4 is not null ");
		stb.append("  union all ");
        // 入試区分：Ｄ
        stb.append("  select ");
        stb.append("        EXAMNO,");
        stb.append("        '6' AS TESTDIV");
        stb.append("  from   ENTEXAM_APPLICANTBASE_DAT ");
        stb.append("  where  ENTEXAMYEAR='"+_param._year+"' and");
        stb.append("        TESTDIV6 is not null ");
        stb.append("  union all ");
		// 入試区分：帰国生
		stb.append("  select ");
		stb.append("        EXAMNO,");
		stb.append("        '5' AS TESTDIV");
		stb.append("  from   ENTEXAM_APPLICANTBASE_DAT ");
		stb.append("  where  ENTEXAMYEAR='"+_param._year+"' and");
		stb.append("        TESTDIV5 is not null ");
		stb.append(") ");
		stb.append(", EXAMNO_LIST AS ( ");
		stb.append("    select  EXAMNO  ");
		stb.append("    from    BASE1   ");
		stb.append("    group by EXAMNO ");
		// テストタイプ(1:単願 2:複数回 3:全て)
		if(test_Type.equals("1")){
			stb.append("    having 1 = count(*) ");
		}
		if(test_Type.equals("2")){
			stb.append("    having 1 < count(*) ");
		}
		stb.append("  ) ");
		stb.append("select  T1.RECEPTDATE, ");
		stb.append("        T1.SEX, ");
		stb.append("        COUNT(*) AS EXAMNO_CNT ");
		stb.append("from    ENTEXAM_APPLICANTBASE_DAT T1, ");
		stb.append("        EXAMNO_LIST T2 ");
		stb.append("where   T1.ENTEXAMYEAR='"+_param._year+"' and");
		stb.append("        T1.RECEPTDATE >='"+date_from+"' and");
		stb.append("        T1.RECEPTDATE <='"+date_to+"' and");
		stb.append("        T1.EXAMNO = T2.EXAMNO ");
		stb.append("group by T1.RECEPTDATE,T1.SEX ");
        log.debug("帳票出力対象データ抽出 の SQL=" + stb.toString());
        return stb.toString();
    }
    
    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @param date_from	 開始日付
     * @param date_to		 終了日付
     * @param test_div_name 出願区分
     * @param test_div		 出願区分値
     * @return				 SQL文字列
     * @throws Exception
     */
    private String getApplicantSql(
    	final String date_from, 
    	final String date_to, 
    	final String test_div_name, 
    	final String test_div)
    {
		StringBuffer stb = new StringBuffer();
    	
		stb.append("WITH BASE1 AS ( ");
		stb.append("  select ");
		stb.append("        EXAMNO");
		stb.append("  from   ENTEXAM_APPLICANTBASE_DAT ");
		stb.append("  where  ENTEXAMYEAR='"+_param._year+"' and");
		if(test_div.equals("sum")){
			stb.append("         TESTDIV"+ test_div_name + " is not null ");
		} else {
			stb.append("         TESTDIV"+ test_div_name + " = '"+test_div+"' ");
		}
		stb.append(") ");
		stb.append(", EXAMNO_LIST AS ( ");
		stb.append("    select  EXAMNO  ");
		stb.append("    from    BASE1   ");
		stb.append("    group by EXAMNO ");
		stb.append("  ) ");
		stb.append("select  T1.RECEPTDATE, ");
		stb.append("        T1.SEX, ");
		stb.append("        COUNT(*) AS EXAMNO_CNT ");
		stb.append("from    ENTEXAM_APPLICANTBASE_DAT T1, ");
		stb.append("        EXAMNO_LIST T2 ");
		stb.append("where   T1.ENTEXAMYEAR='"+_param._year+"' and");
		stb.append("        T1.RECEPTDATE >='"+date_from+"' and");
		stb.append("        T1.RECEPTDATE <='"+date_to+"' and");
		stb.append("        T1.EXAMNO = T2.EXAMNO ");
		if(test_div.equals("sum")){
			stb.append(" AND T1.TESTDIV"+ test_div_name + " is not null ");
		}
		stb.append("group by T1.RECEPTDATE, ");
		if(!test_div.equals("sum")){
			stb.append("    T1.TESTDIV"+ test_div_name + ", ");
		}
		stb.append("        T1.SEX ");
		
        log.debug("帳票出力対象データ抽出 の SQL=" + stb.toString());
        return stb.toString();
    }
    
    /** 生徒クラス：実人数・単願人数・複数回人数 */
    private class student {
        final String _receptdate;
        final String _sex;
        final String _cexamno_cnt;

        student(
                final String receptdate,
                final String sex,
                final String cexamno_cnt
        ) {
        	_receptdate = receptdate;
        	_sex = sex;
        	_cexamno_cnt = cexamno_cnt;
        }
    }
    
    /** 生徒クラス：入試区分毎 */
    private class div_student {
        final String _receptdate;
        final String _sex;
        final String _examno_cnt;

        div_student(
                final String receptdate,
                final String sex,
                final String examno_cnt
        ) {
        	_receptdate = receptdate;
        	_sex = sex;
        	_examno_cnt = examno_cnt;
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
    	private final String _nendo;
    	private final String _date;
    	private final String _test_date1_from;
    	private final String _test_date1_to;
    	private final String _test_date2_from;
    	private final String _test_date2_to;
    	private final int _date1_from;
    	private final int _date1_to;
    	private final int _date2_from;
    	private final int _date2_to;

    	// 帳票出力用の変数
    	private int zitu_total_man_count = 0;		// 実人数		：男子カウント用
    	private int zitu_total_woman_count = 0;	// 実人数		：女子カウント用
    	private int zitu_total_count = 0;			// 実人数		：男女合計カウント用
    	private int tangan_total_man_count = 0;	// 単願人数		：男子カウント用
    	private int tangan_total_woman_count = 0;	// 単願人数		：女子カウント用
    	private int tangan_total_count = 0;		// 単願人数		：男女合計カウント用
    	private int fukusu_total_man_count = 0;	// 複数回人数	：男子カウント用
    	private int fukusu_total_woman_count = 0;	// 複数回人数	：女子カウント用
    	private int fukusu_total_count = 0;		// 複数回人数	：男女合計カウント用
    	private int nobe_total_man_count = 0;		// 延人数		：男子カウント用
    	private int nobe_total_woman_count = 0;	// 延人数		：女子カウント用
    	private int nobe_total_count = 0;			// 延人数		：男女合計カウント用
    	private int nobe_daytotal_man_count = 0;	// 延人数(日別)	：男子カウント用
    	private int nobe_daytotal_woman_count = 0;	// 延人数(日別)	：女子カウント用
    	private int nobe_daytotal_count = 0;		// 延人数(日別)	：男女合計カウント用
    	private int a1_2_total_man_count = 0;		// Ａ１の２科	：男子カウント用
    	private int a1_2_total_woman_count = 0;	// Ａ１の２科	：女子カウント用
    	private int a1_2_total_count = 0;			// Ａ１の２科	：男女合計カウント用
    	private int a1_4_total_man_count = 0;		// Ａ１の４科	：男子カウント用
    	private int a1_4_total_woman_count = 0;	// Ａ１の４科	：女子カウント用
    	private int a1_4_total_count = 0;			// Ａ１の４科	：男女合計カウント用
    	private int a1_total_man_count = 0;		// Ａ１の合計	：男子カウント用
    	private int a1_total_woman_count = 0;		// Ａ１の合計	：女子カウント用
    	private int a1_total_count = 0;			// Ａ１の合計	：男女合計カウント用
    	private int a2_2_total_man_count = 0;		// Ａ２の２科	：男子カウント用
    	private int a2_2_total_woman_count = 0;	// Ａ２の２科	：女子カウント用
    	private int a2_2_total_count = 0;			// Ａ２の２科	：男女合計カウント用
    	private int b_2_total_man_count = 0;		// Ｂの２科		：男子カウント用
    	private int b_2_total_woman_count = 0;		// Ｂの２科		：女子カウント用
    	private int b_2_total_count = 0;			// Ｂの２科		：男女合計カウント用
    	private int b_4_total_man_count = 0;		// Ｂの４科		：男子カウント用
    	private int b_4_total_woman_count = 0;		// Ｂの４科		：女子カウント用
    	private int b_4_total_count = 0;			// Ｂの４科		：男女合計カウント用
    	private int b_total_man_count = 0;			// Ｂの合計		：男子カウント用
    	private int b_total_woman_count = 0;		// Ｂの合計		：女子カウント用
    	private int b_total_count = 0;				// Ｂの合計		：男女合計カウント用
        private int c_2_total_man_count = 0;      // Ｃの２科     ：男子カウント用
        private int c_2_total_woman_count = 0;    // Ｃの２科     ：女子カウント用
        private int c_2_total_count = 0;          // Ｃの２科     ：男女合計カウント用
    	private int c_4_total_man_count = 0;		// Ｃの４科		：男子カウント用
    	private int c_4_total_woman_count = 0;		// Ｃの４科		：女子カウント用
    	private int c_4_total_count = 0;			// Ｃの４科		：男女合計カウント用
        private int c_total_man_count = 0;        // Ｃの合計     ：男子カウント用
        private int c_total_woman_count = 0;      // Ｃの合計     ：女子カウント用
        private int c_total_count = 0;            // Ｃの合計     ：男女合計カウント用
        private int d_2_total_man_count = 0;      // Ｄの２科     ：男子カウント用
        private int d_2_total_woman_count = 0;    // Ｄの２科     ：女子カウント用
        private int d_2_total_count = 0;          // Ｄの２科     ：男女合計カウント用
        private int d_4_total_man_count = 0;      // Ｄの４科     ：男子カウント用
        private int d_4_total_woman_count = 0;    // Ｄの４科     ：女子カウント用
        private int d_4_total_count = 0;          // Ｄの４科     ：男女合計カウント用
        private int d_total_man_count = 0;        // Ｄの合計     ：男子カウント用
        private int d_total_woman_count = 0;      // Ｄの合計     ：女子カウント用
        private int d_total_count = 0;            // Ｄの合計     ：男女合計カウント用
    	private int kikoku_total_man_count = 0;	// 帰国生		：男子カウント用
    	private int kikoku_total_woman_count = 0;	// 帰国生		：女子カウント用
    	private int kikoku_total_count = 0;		// 帰国生		：男女合計カウント用

    	private boolean hit_flg = false;
        private boolean _isInfluence = false;
    	
    	
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _nendo = KenjaProperties.gengou(Integer.parseInt(_year))+"年度";
            _date = KNJ_EditDate.h_format_JP(_loginDate);
            _test_date1_from = fomatSakuseiDate(request.getParameter("TEST_DATE1_FROM"), "yyyy/mm/dd", "yyyy-mm-dd");
            _test_date1_to = fomatSakuseiDate(request.getParameter("TEST_DATE1_TO"), "yyyy/mm/dd", "yyyy-mm-dd");
            _test_date2_from = fomatSakuseiDate(request.getParameter("TEST_DATE2_FROM"), "yyyy/mm/dd", "yyyy-mm-dd");
            _test_date2_to = fomatSakuseiDate(request.getParameter("TEST_DATE2_TO"), "yyyy/mm/dd", "yyyy-mm-dd");
            _date1_from = Integer.valueOf(fomatSakuseiDate(_test_date1_from, "yyyy-mm-dd", "yyyymmdd")).intValue();
            _date1_to = Integer.valueOf(fomatSakuseiDate(_test_date1_to, "yyyy-mm-dd", "yyyymmdd")).intValue();
            _date2_from = Integer.valueOf(fomatSakuseiDate(_test_date2_from, "yyyy-mm-dd", "yyyymmdd")).intValue();
            _date2_to = Integer.valueOf(fomatSakuseiDate(_test_date2_to, "yyyy-mm-dd", "yyyymmdd")).intValue();
            setInfluence(db2);
        }
        
        /**
         * 帳票合計行用のカウンターのクリアを行う
         */
        private void setCountinit() {
			zitu_total_man_count = 0;
			zitu_total_woman_count = 0;
			zitu_total_count = 0;
			tangan_total_man_count = 0;
			tangan_total_woman_count = 0;
			tangan_total_count = 0;
			fukusu_total_man_count = 0;
			fukusu_total_woman_count = 0;
			fukusu_total_count = 0;
			nobe_total_man_count = 0;
			nobe_total_woman_count = 0;
			nobe_total_count = 0;
			a1_2_total_man_count = 0;
			a1_2_total_woman_count = 0;
			a1_2_total_count = 0;
			a1_4_total_man_count = 0;
			a1_4_total_woman_count = 0;
			a1_4_total_count = 0;
			a1_total_man_count = 0;
			a1_total_woman_count = 0;
			a1_total_count = 0;
			a2_2_total_man_count = 0;
			a2_2_total_woman_count = 0;
			a2_2_total_count = 0;
			b_2_total_man_count = 0;
			b_2_total_woman_count = 0;
			b_2_total_count = 0;
			b_4_total_man_count = 0;
			b_4_total_woman_count = 0;
			b_4_total_count = 0;
			b_total_man_count = 0;
			b_total_woman_count = 0;
			b_total_count = 0;
            c_2_total_man_count = 0;
            c_2_total_woman_count = 0;
            c_2_total_count = 0;
			c_4_total_man_count = 0;
			c_4_total_woman_count = 0;
			c_4_total_count = 0;
            d_2_total_man_count = 0;
            c_total_count = 0;
            c_total_man_count = 0;
            c_total_woman_count = 0;
            d_2_total_woman_count = 0;
            d_2_total_count = 0;
            d_4_total_man_count = 0;
            d_4_total_woman_count = 0;
            d_4_total_count = 0;
            d_total_count = 0;
            d_total_man_count = 0;
            d_total_woman_count = 0;
	    	kikoku_total_man_count = 0;
	    	kikoku_total_woman_count = 0;
	    	kikoku_total_count = 0;
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
     * 指定した日付文字列（yyyy/MM/dd or yyyy-MM-dd）
     * における月末日付を返します。
     * 
     * @param strDate 対象の日付文字列
     * @return 月末日付
     */
    public static int getLastDay(String strDate) {

    	int yyyy = Integer.parseInt(strDate.substring(0,4));
        int MM = Integer.parseInt(strDate.substring(5,7));
        int dd = Integer.parseInt(strDate.substring(8,10));
        Calendar cal = Calendar.getInstance();
        cal.set(yyyy,MM-1,dd);
        int last = cal.getActualMaximum(Calendar.DATE);
        
        return last;
    }

    /**
	 * 数値系Formatter
	 * @param sForm
	 * @param sData
	 * @return
	 */
	public String getNumberFormat(String sForm, String sData){

		try{
			DecimalFormat df = new DecimalFormat(sForm);
			double dData = Double.parseDouble(sData);
			String sVal = df.format(dData);
			return sVal;
		}
		catch(Exception e){
		}
		return sData;
	}	
	
	
}
