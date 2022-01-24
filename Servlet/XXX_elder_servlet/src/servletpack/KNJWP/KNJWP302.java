package servletpack.KNJWP;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * 未収者リスト
 * 
 * @author nakasone
 *
 */
public class KNJWP302 {
    private static final String FORM_NAME = "KNJWP302.frm";
    private static final String FILE_NAME = "KNJWP302.csv";
    private static final int PAGE_MAX_LINE = 30;
    private static final String PDF_OUT_STYLE = "RECORD";
    
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJWP302.class);
    
    /**
     * KNJW.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);

        CSVorPDF output = null;
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = new Param(db2, request);
        	if(_param._useCsv){
                output = new MountCSV();
                output.init(response, FILE_NAME, PDF_OUT_STYLE);
        	} else {
                output = new MountPDF();
                output.init(response, FORM_NAME, PDF_OUT_STYLE);
        	}
            
            _hasData = printMain(db2, output);
            if (!_hasData) {
               	output.notData();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            output.close();
            closeDb(db2);
        }
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    /**
     * 印刷処理メイン
     * @param db2	ＤＢ接続オブジェクト
     * @return		対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private boolean printMain(final DB2UDB db2, final CSVorPDF obj) throws Exception {
        
    	boolean retflg = false;
    	
		// 未収者リストデータ取得
		final List school = createSchool(db2);
		String titleSchoolName = getSchoolName(school);
		// 未収者リストデータ取得
		final List student = createStudents(db2);
		// 帳票出力のメソッド
		retflg = outPutPrint(obj, student, titleSchoolName);
		return retflg;
			
    }
    
    /**
     * 帳票出力処理
     * @param obj					帳票orCSVの出力インターフェースオブジェクト
     * @param student				帳票出力対象クラスオブジェクト
     * @param titleSchoolName	    ヘッダ出力用学校名称
     * return						対象データ存在フラグ
     */
    private boolean outPutPrint(final CSVorPDF obj, final List student, String titleSchoolName) {
		
    	boolean dataflg = false;		// 対象データ存在フラグ
    	int gyo = 1;					// 現在ページ数の判断用（行）
		int schoolcnt = 0;				// 小計印字判定用
		int reccnt = 0;					// 合計レコード数
		String gradeNew = "";			// 小計印字用ブレイクキー
		String gradeOld = "";			// 小計印字用ブレイクキー
		boolean endflg = true;

        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();

			//30行超えた場合、行カウントに１を設定
			if (gyo > PAGE_MAX_LINE) {
				gyo = 1;
			}

            // 小計印字用ブレイクキー設定
            gradeNew = sudent._grade;

			if (reccnt > 0){
		    	obj.flush(PDF_OUT_STYLE, false);	// 明細レコード出力

				// 学習センターが前回出力データと異なる場合 且つ
				// 同一学習センター使用者が2名以上の場合
				if((!gradeNew.equals(gradeOld)) && (schoolcnt >= 2)){
					// 小計の編集・出力
					setSyokeiRecord(obj);
					// 小計値のクリア
					_param.setSyokeiinit();
					schoolcnt = 1;
					++gyo;
					// 小計印字用ブレイクキー設定
					gradeOld = gradeNew;
				} else {
					if(gradeNew.equals(gradeOld)){
						++schoolcnt;
					} else {
						// 学習センター使用者が1名の場合
						if(schoolcnt == 1){
							// 小計値のクリア
							_param.setSyokeiinit();
						}
						// 小計印字用ブレイクキー設定
						gradeOld = gradeNew;
					}
				}
			} else {
				// 小計印字用ブレイクキー設定
				gradeOld = gradeNew;
				++schoolcnt;
			}
			
			//*================*
			//* ヘッダ         *
			//*================*
			obj.put("YEAR_MONTH"	,_param._nengetu);	// 対象年月
			obj.put("TITLE"		, titleSchoolName);	// 学校名称
			obj.put("LOGINDATE"	,_param._date);			// 作成日
			//*================*
			//* 明細           *
			//*================*
			// 学習センター
			obj.put("SCHOOLNAME1" , sudent._schoolname1);
			// 学籍番号
			obj.put("SCHREGNO" , sudent._schregno);
			// 生徒氏名
			if(_param._useCsv){
				obj.put("NAME", sudent._name);
			} else {
			    // TODO: 文字数によってFrmフィールドを変化させる場合、CSVorPDFでどう対応させるか?
				obj.put(setformatArea("NAME", sudent._name, 12, "1", "2") , sudent._name);
			}
			// 前月末未収金
			obj.put("LAST_MONTH_NO_SALES_MONEY1" , sudent._last_month_no_sales_money);
			// 入返金他
			obj.put("MONTH_PAYMENT_MONEY1" , sudent._month_payment_money);
			// 未収への入金
			obj.put("MONTH_APPROPRIATED_MONEY_DISP1" , sudent._month_appropriated_money_disp);
			// 売上高
			obj.put("MONTH_SALES_MONEY1" , sudent._month_sales_money);
			// 当月未収金分
			obj.put("MONTH_NO_SALES_MONEY_DISP1" , sudent._month_no_sales_money_disp);
			// 当月末未収金
			obj.put("TOTAL_NO_SALES_MONEY_DISP1" , sudent._total_no_sales_money_disp);
            // 備考
            obj.put("REMARK" , sudent._remark);
			
			// 各金額小計値へ加算
			sumSyokeiTotal(sudent);
			// レコード数カウント
			++reccnt;
			
			// 現在ページ数判断用
			++gyo;
			dataflg = true;            
        }

        if (dataflg) {
			//30行超えた場合、行カウントに１を設定
			if (gyo > PAGE_MAX_LINE) {
				gyo = 1;
			}
			obj.flush(PDF_OUT_STYLE, true);	// 明細レコードを出力
			if(schoolcnt >= 2){
				setSyokeiRecord(obj);		// 小計レコード編集・出力
				++gyo;
			}
			// 最終行に達していない場合
			if (gyo < PAGE_MAX_LINE) {
	    		while(endflg){
	        		if(gyo >= PAGE_MAX_LINE){
    					//合計を編集・出力
	        			setTotalRecord(obj);
	    	            endflg = false;
	    			} else {
	    				// 空行印字の為に設定(帳票上で項目の表示は行われません)
	    				obj.put("KARA" , "空行");
	    		    	obj.flush(PDF_OUT_STYLE, false);
	        			++gyo;
	    			}
	            }
	    	} else {
				//合計を編集・出力
    			setTotalRecord(obj);
	            endflg = false;
	    	}
		}
		
		return dataflg;
    }

    /**
     * 小計エリアの出力
     * @param obj		帳票orCSVの出力インターフェースオブジェクト
     */
    private void setSyokeiRecord(final CSVorPDF obj) {

    	//*================*
		//* 小計           *
		//*================*
    	// タイトル
    	obj.put("ITEM" , "学習センター小計");
		// 前月末未収金
    	obj.put("LAST_MONTH_NO_SALES_MONEY2" , String.valueOf(_param.zenmatu_misyukin));
		// 入返金他
    	obj.put("MONTH_PAYMENT_MONEY2" , String.valueOf(_param.nyuhenkin_hoka));
		// 未収への入金
    	obj.put("MONTH_APPROPRIATED_MONEY_DISP2" , String.valueOf(_param.misyu_nyukin));
		// 売上高
    	obj.put("MONTH_SALES_MONEY2" , String.valueOf(_param.uri_kin));
		// 当月未収金分
    	obj.put("MONTH_NO_SALES_MONEY_DISP2" , String.valueOf(_param.tougetu_misyukin));
		// 当月末未収金
    	obj.put("TOTAL_NO_SALES_MONEY_DISP2" , String.valueOf(_param.tougetu_matu_misyukin));
		
    	obj.flush(PDF_OUT_STYLE, false);
    	
    }

    /**
     * 合計エリアの出力
     * @param obj		帳票orCSVの出力インターフェースオブジェクト
     */
    private void setTotalRecord(final CSVorPDF obj) {

    	//*================*
		//* 合計           *
		//*================*
    	// タイトル
    	obj.put("ITEM" , "合　計");
		// 前月末未収金
    	obj.put("LAST_MONTH_NO_SALES_MONEY2" , String.valueOf(_param.t_zenmatu_misyukin));
		// 入返金他
    	obj.put("MONTH_PAYMENT_MONEY2" , String.valueOf(_param.t_nyuhenkin_hoka));
		// 未収への入金
    	obj.put("MONTH_APPROPRIATED_MONEY_DISP2" , String.valueOf(_param.t_misyu_nyukin));
		// 売上高
    	obj.put("MONTH_SALES_MONEY2" , String.valueOf(_param.t_uri_kin));
		// 当月未収金分
    	obj.put("MONTH_NO_SALES_MONEY_DISP2" , String.valueOf(_param.t_tougetu_misyukin));
		// 当月末未収金
    	obj.put("TOTAL_NO_SALES_MONEY_DISP2" , String.valueOf(_param.t_tougetu_matu_misyukin));
		
    	obj.flush(PDF_OUT_STYLE, false);

    }

    /**
     * 小計・合計エリアの加算
     * @param student	帳票出力対象クラスオブジェクト
     */
    private void sumSyokeiTotal(final student sudent) {

    	//*================*
		//* 小計値加算     *
		//*================*
		// 前月末未収金
    	_param.zenmatu_misyukin += Integer.parseInt(sudent._last_month_no_sales_money);
		// 入返金他
    	_param.nyuhenkin_hoka   += Integer.parseInt(sudent._month_payment_money);
		// 未収への入金
    	_param.misyu_nyukin     += Integer.parseInt(sudent._month_appropriated_money_disp);
		// 売上高
    	_param.uri_kin          += Integer.parseInt(sudent._month_sales_money);
		// 当月未収金分
    	_param.tougetu_misyukin += Integer.parseInt(sudent._month_no_sales_money_disp);
		// 当月末未収金
    	_param.tougetu_matu_misyukin += Integer.parseInt(sudent._total_no_sales_money_disp);
    	//*================*
		//* 合計値加算     *
		//*================*
		// 前月末未収金
    	_param.t_zenmatu_misyukin += Integer.parseInt(sudent._last_month_no_sales_money);
		// 入返金他
    	_param.t_nyuhenkin_hoka   += Integer.parseInt(sudent._month_payment_money);
		// 未収への入金
    	_param.t_misyu_nyukin     += Integer.parseInt(sudent._month_appropriated_money_disp);
		// 売上高
    	_param.t_uri_kin          += Integer.parseInt(sudent._month_sales_money);
		// 当月未収金分
    	_param.t_tougetu_misyukin += Integer.parseInt(sudent._month_no_sales_money_disp);
		// 当月末未収金
    	_param.t_tougetu_matu_misyukin += Integer.parseInt(sudent._total_no_sales_money_disp);
    }

    /**
     * @param db2			ＤＢ接続オブジェクト
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudents(final DB2UDB db2)	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getStudentSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final student student = new student(rs.getString("LAST_MONTH_NO_SALES_MONEY"),
                		rs.getString("MONTH_PAYMENT_MONEY"),
                		rs.getString("MONTH_APPROPRIATED_MONEY_DISP"),
                		rs.getString("MONTH_SALES_MONEY"),
                		rs.getString("MONTH_NO_SALES_MONEY_DISP"),
                		rs.getString("TOTAL_NO_SALES_MONEY_DISP"),
                		rs.getString("SCHREGNO"),
                		rs.getString("GRADE"),
                        rs.getString("REMARK"),
                		nvlT(rs.getString("NAME")),
                		nvlT(rs.getString("SCHOOLNAME1"))
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
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql(){
        final String rtn;
        rtn = " select"
            + "    W1.LAST_MONTH_NO_SALES_MONEY,"
            + "    W1.MONTH_PAYMENT_MONEY,"
            + "    W1.MONTH_APPROPRIATED_MONEY_DISP,"
            + "    W1.MONTH_SALES_MONEY,"
            + "    W1.MONTH_NO_SALES_MONEY_DISP,"
            + "    W1.TOTAL_NO_SALES_MONEY_DISP,"
            + "    W1.APPLICANTNO,"
            + "    W2.SCHREGNO,"
            + "    W5.REMARK,"
            + "    CASE WHEN W2.SCHREGNO IS NOT NULL"
            + "         THEN W2.NAME"
            + "         ELSE W5.NAME"
            + "    END AS NAME,"
            + "    CASE WHEN W3.SCHREGNO IS NOT NULL"
            + "         THEN W3.GRADE"
            + "         ELSE W5.BELONGING_DIV"
            + "    END AS GRADE,"
            + "    CASE WHEN W3.SCHREGNO IS NOT NULL"
            + "         THEN W4.SCHOOLNAME1"
            + "         ELSE W6.SCHOOLNAME1"
            + "    END AS SCHOOLNAME1"
            + " from"
            + "    MONTH_NO_SALES_DAT W1"
            + "    LEFT JOIN SCHREG_BASE_MST W2 ON"
            + "        W1.APPLICANTNO = W2.APPLICANTNO "			// 志願者番号
            + "    LEFT JOIN SCHREG_REGD_DAT W3 ON"
            + "        W2.SCHREGNO = W3.SCHREGNO and"				// 学籍番号
            + "        W3.YEAR = '" + _param._year + "' and"		// 対象年度
            + "        W3.SEMESTER = '" + _param._semester + "' "	// 学期
            + "    LEFT JOIN BELONGING_MST W4 ON"
            + "        W3.GRADE = W4.BELONGING_DIV "				// 所属
            + "    INNER JOIN APPLICANT_BASE_MST W5 ON"
            + "        W1.APPLICANTNO = W5.APPLICANTNO "            // 志願者基礎
            + "    LEFT JOIN BELONGING_MST W6 ON"
            + "        W5.BELONGING_DIV = W6.BELONGING_DIV "        // 所属(志願者)
            + " where"
            + "    W1.YEAR_MONTH = '" + _param._serch_yyyymm + "' "	// 年月
            + " order by"
            + "    W3.GRADE, W3.SCHREGNO"							// 所属、学籍番号
            ;
        log.debug("帳票出力対象データ抽出 の SQL=" + rtn);
        return rtn;
    }

    /**
     * @param db2			ＤＢ接続オブジェクト
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createSchool(final DB2UDB db2)	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getSchoolMstSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final school school = new school(rs.getString("TITLE_SCHOOL"));
                rtnList.add(school);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    /**
     * 学校名抽出ＳＱＬ生成処理
     * @return				SQL文字列
     * @throws Exception
     */
    private String getSchoolMstSql(){
        final String rtn;
        rtn = " select"
            + "    SCHOOLNAME1 AS TITLE_SCHOOL"
            + " from"
            + "    SCHOOL_MST "
            + " where"
            + "    YEAR = '" + _param._year + "' "
            ;
        log.debug("学校名抽出 の SQL=" + rtn);
        return rtn;
    }

    /** 生徒クラス */
    private class student {
        final String _last_month_no_sales_money;
        final String _month_payment_money;
        final String _month_appropriated_money_disp;
        final String _month_sales_money;
        final String _month_no_sales_money_disp;
        final String _total_no_sales_money_disp;
        final String _schregno;
        final String _grade;
        final String _name;
        final String _remark;
        final String _schoolname1;
		
        student(
                final String last_month_no_sales_money,
                final String month_payment_money,
                final String month_appropriated_money_disp,
                final String month_sales_money,
                final String month_no_sales_money_disp,
                final String total_no_sales_money_disp,
                final String schregno,
                final String grade,
                final String remark,
                final String name,
                final String schoolname1
        ) {
			_last_month_no_sales_money = last_month_no_sales_money;
			_month_payment_money = month_payment_money;
			_month_appropriated_money_disp = month_appropriated_money_disp;
			_month_sales_money = month_sales_money;
			_month_no_sales_money_disp = month_no_sales_money_disp;
			_total_no_sales_money_disp = total_no_sales_money_disp;
			_schregno = schregno;
			_grade = grade;
            _remark = remark;
			_name = name;
			_schoolname1 = schoolname1;
        }
    }
    
    /** 学校名称クラス */
    private class school {
        final String _title_school_name;
		
        school(
                final String title_school_name
        ) {
			_title_school_name = title_school_name;
        }
    }

    /**
     * 小計エリアの出力
     * @param school	学校名称クラスオブジェクト
     */
    private String getSchoolName(final List school) {

    	String retSchool_name = "";
    	
        for (Iterator it = school.iterator(); it.hasNext();) {
            final school title_school = (school) it.next();
            retSchool_name = title_school._title_school_name;
        }
        return retSchool_name;
    }
    
    /** パラメータクラス */
    private class Param {
    	private final String _programid;
    	private final String _year;
    	private final String _semester;
    	private final String _loginDate;
    	private String _year_month;
    	private boolean _useCsv;
    	private final String _nengetu;
    	private final String _date;
    	private final String _serch_yyyymm;

    	String sYearMonth = "";
        int zenmatu_misyukin = 0;		// 小計：前月末未収金
        int nyuhenkin_hoka = 0;			// 小計：入返金他
        int misyu_nyukin   = 0;			// 小計：未収への入金
        int uri_kin = 0;				// 小計：売上高
        int tougetu_misyukin = 0;		// 小計：当月未収金分
        int tougetu_matu_misyukin = 0;	// 小計：当月末未収金

        int t_zenmatu_misyukin = 0;		// 合計：前月末未収金
        int t_nyuhenkin_hoka = 0;		// 合計：入返金他
        int t_misyu_nyukin   = 0;		// 合計：未収への入金
        int t_uri_kin = 0;				// 合計：売上高
        int t_tougetu_misyukin = 0;		// 合計：当月未収金分
        int t_tougetu_matu_misyukin = 0;// 合計：当月末未収金

    	
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _useCsv = false;

            _year_month = request.getParameter("YEAR_MONTH");
            sYearMonth = _year_month+"01";
            sYearMonth = fomatDate(sYearMonth, "yyyyMMdd", "yyyy-MM-dd");
            _nengetu = KNJ_EditDate.h_format_JP_M(sYearMonth);
            _serch_yyyymm = _year_month;
            _date = KNJ_EditDate.h_format_JP(_loginDate);
        }

        /** 小計金額クリア */
        private void setSyokeiinit() {
        	zenmatu_misyukin = 0;		// 小計：前月末未収金
            nyuhenkin_hoka = 0;			// 小計：入返金他
            misyu_nyukin   = 0;			// 小計：未収への入金
            uri_kin = 0;				// 小計：売上高
            tougetu_misyukin = 0;		// 小計：当月未収金分
            tougetu_matu_misyukin = 0;	// 小計：当月末未収金
        }
        
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /**
     * 帳票に設定する文字数が制限文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name	帳票出力エリア
     * @param sval			値
     * @param area_len		制限文字数
     * @param hokan_Name1	制限文字以下の場合のエリア名
     * @param hokan_Name2	制限文字超の場合のエリア名
     * @return
     */
    private String setformatArea(String area_name, String sval, int area_len, String hokan_Name1, String hokan_Name2) {

    	String retAreaName = "";
		// 値がnullの場合はnullが返される
    	if (sval == null) {
			return null;
		}
    	// 設定値が制限文字超の場合、帳票設定エリアの変更を行う
    	if(area_len >= sval.length()){
   			retAreaName = area_name + hokan_Name1;
    	} else {
   			retAreaName = area_name + hokan_Name2;
    	}
        return retAreaName;
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

	/**
     * 日付をフォーマットし文字列で返す
     * @param s
     * @return
     */
    private String fomatDate(String cnvDate, String before_sfmt, String after_sfmt) {

    	String retDate = "";
    	try {
			DateFormat foramt = new SimpleDateFormat(before_sfmt); 
			//文字列よりDate型へ変換
			Date date1 = foramt.parse(cnvDate); 
			// 年月日のフォーマットを指定
			SimpleDateFormat sdf1 = new SimpleDateFormat(after_sfmt);
			// Date型より文字列へ変換
			retDate = sdf1.format(date1);
		} catch( Exception e ){
			log.error("fomatDate error!");
		}
		return retDate;
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * インターフェースクラス
     * @author nakasone
     */
    interface CSVorPDF {

    	public void init(HttpServletResponse response, String file, String mode) throws IOException;

        public void put(String field, String value);

        public void putn(String field, String value, int n);

        /**
         * バッファをフラッシュする。
         * @param formMode PDF の時のフォームのモード
         * @param outMode CSVの時、出力するか否か?
         */
    	public void flush(String formMode, boolean outMode);

    	public void notData();

    	public void close();

    }
    
    /**
     * CSV実装クラス
     * @author nakasone
     */
    class MountCSV implements CSVorPDF{

        PrintWriter out = null;
        HashMap buf = new MultiHashMap();
        
    	public void init(HttpServletResponse response, String file, String mode) throws IOException {

    		response.setContentType("text/csv; charset=Shift_JIS");
    		response.setHeader("Content-disposition", "attachment; filename=" + file + "");
    		
    		try {
                out = response.getWriter();
            } catch (final IOException e) {
                 e.printStackTrace();
            }

    	}

    	public void put(String field, String value) {
    		buf.put(field, value);
    	}

    	public void putn(String field, String value, int n) {
    		buf.put(field, value);
    	}

    	public void flush(String formMode, boolean outMode) {
    		if (!outMode) {
                return;
    		}

            Collection coll1 = (Collection) buf.get("SCHOOLNAME1");
    		Collection coll2 = (Collection) buf.get("SCHREGNO");
    		Collection coll3 = (Collection) buf.get("NAME");
    		Collection coll4 = (Collection) buf.get("LAST_MONTH_NO_SALES_MONEY1");
    		Collection coll5 = (Collection) buf.get("MONTH_PAYMENT_MONEY1");
    		Collection coll6 = (Collection) buf.get("MONTH_APPROPRIATED_MONEY_DISP1");
    		Collection coll7 = (Collection) buf.get("MONTH_SALES_MONEY1");
    		Collection coll8 = (Collection) buf.get("MONTH_NO_SALES_MONEY_DISP1");
    		Collection coll9 = (Collection) buf.get("TOTAL_NO_SALES_MONEY_DISP1");
    		for (int i = 0; i < coll2.size(); i++) {
    		    ArrayList list1 = new ArrayList(coll1);
    		    ArrayList list2 = new ArrayList(coll2);
    		    ArrayList list3 = new ArrayList(coll3);
    		    ArrayList list4 = new ArrayList(coll4);
    		    ArrayList list5 = new ArrayList(coll5);
    		    ArrayList list6 = new ArrayList(coll6);
    		    ArrayList list7 = new ArrayList(coll7);
    		    ArrayList list8 = new ArrayList(coll8);
    		    ArrayList list9 = new ArrayList(coll9);
    		    
    		    String sSCHOOLNAME1 = (String) list1.get(i);
    		    String sSCHREGNO = (String) list2.get(i);
    		    String sNAME = (String) list3.get(i);
    		    String sLAST_MONTH_NO_SALES_MONEY1 = (String) list4.get(i);
    		    String sMONTH_PAYMENT_MONEY1 = (String) list5.get(i);
    		    String sMONTH_APPROPRIATED_MONEY_DISP1 = (String) list6.get(i);
    		    String sMONTH_SALES_MONEY1 = (String) list7.get(i);
    		    String sMONTH_NO_SALES_MONEY_DISP1 = (String) list8.get(i);
    		    String sTOTAL_NO_SALES_MONEY_DISP1 = (String) list9.get(i);
    		    
    		    out.println(sSCHOOLNAME1
    		            + "," + sSCHREGNO
    		            + "," + sNAME
    		            + "," + sLAST_MONTH_NO_SALES_MONEY1
    		            + "," + sMONTH_PAYMENT_MONEY1
    		            + "," + sMONTH_APPROPRIATED_MONEY_DISP1
    		            + "," + sMONTH_SALES_MONEY1
    		            + "," + sMONTH_NO_SALES_MONEY_DISP1
    		            + "," + sTOTAL_NO_SALES_MONEY_DISP1
    		    );
    		}
    	}

    	public void notData() {
    		return;
    	}
    	
    	public void close() {
            out.close();
    	}
    }
    
    /**
     * PDF実装クラス
     * @author nakasone
     */
    class MountPDF implements CSVorPDF{

    	public Vrw32alp _svf = null;

    	public void init(HttpServletResponse response, String file, String mode) throws IOException {
            
    		_svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            if(mode.equals("RECORD")){
                _svf.VrSetForm(file, 4);
            } else {
                _svf.VrSetForm(file, 1);
            }
        
    	}

    	public void put(String field, String value) {
    		_svf.VrsOut(field, value);
    	}

    	public void putn(String field, String value, int n) {
    		_svf.VrsOutn(field, n, value);
    	}

    	public void flush(String type, boolean out_mode) {
    		if(type.equals("page")){
    			_svf.VrEndPage();	//1ページ分を出力
    		} else {
    			_svf.VrEndRecord();	//1レコード分を出力
    		}
    	}

    	public void close() {
    		_svf.VrQuit();
    	}

    	public void notData() {
    		_svf.VrSetForm("MES001.frm", 0);
    		_svf.VrsOut("note", "note");
    		_svf.VrEndPage();
    	}
    }
}
