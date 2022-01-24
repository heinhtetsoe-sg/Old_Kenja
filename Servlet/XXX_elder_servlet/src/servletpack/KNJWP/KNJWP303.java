package servletpack.KNJWP;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * 過入金リスト
 * 
 * @author nakasone
 *
 */
public class KNJWP303 {
    private static final String FORM_FILE = "KNJWP303.frm";
    private static final int PAGE_MAX_LINE = 30;
    
    private boolean _hasData;
    private Form _form;
    private Vrw32alp _svf;
    private DB2UDB db2;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJWP303.class);
    
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
        _param = new Param(db2, request);

        _form = new Form(FORM_FILE, response);

        db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _hasData = printMain(db2);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    /**
     * 印刷処理メイン
     * @param db2	ＤＢ接続オブジェクト
     * @return		対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private boolean printMain(final DB2UDB db2) throws Exception {
        
    	boolean retflg = false;
    	
		final List school = createSchool(db2);
		String titleSchoolName = getSchoolName(school);
		// 過入金リストデータ取得
		final List student = createStudents(db2);
		// 帳票出力のメソッド
		retflg = outPutPrint(student, titleSchoolName);
		return retflg;
			
    }
    
    /**
     * 帳票出力処理
     * @param obj					帳票orCSVの出力インターフェースオブジェクト
     * @param student				帳票出力対象クラスオブジェクト
     * @param titleSchoolName	    ヘッダ出力用学校名称
     * return						対象データ存在フラグ
     */
    private boolean outPutPrint(final List student, String titleSchoolName) {
		
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
		        _form._svf.VrEndRecord();

				// 学習センターが前回出力データと異なる場合 且つ
				// 同一学習センター使用者が2名以上の場合
				if((!gradeNew.equals(gradeOld)) && (schoolcnt >= 2)){
					// 小計の編集・出力
					setSyokeiRecord();
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
			_form._svf.VrsOut("YEAR_MONTH"	,_param._nengetu);	// 対象年月
			_form._svf.VrsOut("TITLE"		, titleSchoolName);	// 学校名称
			_form._svf.VrsOut("LOGINDATE"	,_param._date);		// 作成日
			//*================*
			//* 明細           *
			//*================*
			// 学習センター
			_form._svf.VrsOut("SCHOOLNAME1" , sudent._schoolname1);
			// 学籍番号
			_form._svf.VrsOut("SCHREGNO" , sudent._schregno);
			// 生徒氏名
			_form._svf.VrsOut(setformatArea("NAME", sudent._name, 10, "1", "2") , sudent._name);
			// 過入金前月末残
	    	if(sudent._lastMonthOverRemain != null){
				_form._svf.VrsOut("LAST_MONTH_OVER_REMAIN1" , sudent._lastMonthOverRemain);
	    	}
			// 当月過入金
	    	if(sudent._monthOverMoney != null){
				_form._svf.VrsOut("MONTH_OVER_MONEY1" , sudent._monthOverMoney);
	    	}
			// 過入金返金
	    	if(sudent._rePayMoney != null){
				_form._svf.VrsOut("RE_PAY_MONEY1" , sudent._rePayMoney);
	    	}
			// 過入金当月末残
	    	if(sudent._monthOverMoneyRemain != null){
				_form._svf.VrsOut("MONTH_OVER_MONEY_REMAIN1" , sudent._monthOverMoneyRemain);
	    	}
			
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
	        _form._svf.VrEndRecord();	// 明細レコードを出力
			if(schoolcnt >= 2){
				setSyokeiRecord();	// 小計レコード編集・出力
				++gyo;
			}
			// 最終行に達していない場合
			if (gyo < PAGE_MAX_LINE) {
	    		while(endflg){
	        		if(gyo >= PAGE_MAX_LINE){
    					//合計を編集・出力
	        			setTotalRecord();
	    	            endflg = false;
	    			} else {
	    				// 空行印字の為に設定(帳票上で項目の表示は行われません)
	    				_form._svf.VrsOut("KARA" , "空行");
	    		        _form._svf.VrEndRecord();	// 明細レコードを出力
	        			++gyo;
	    			}
	            }
	    	} else {
				//合計を編集・出力
    			setTotalRecord();
	            endflg = false;
	    	}
		}
		
		return dataflg;
    }

    /**
     * 小計エリアの出力
     * @param obj		帳票orCSVの出力インターフェースオブジェクト
     */
    private void setSyokeiRecord() {

    	//*================*
		//* 小計           *
		//*================*
    	// タイトル
    	_form._svf.VrsOut("ITEM" , "学習センター小計");
		// 過入金前月末残
    	_form._svf.VrsOut("LAST_MONTH_OVER_REMAIN2" , String.valueOf(_param.lastMonthOverRemain));
		// 当月過入金
    	_form._svf.VrsOut("MONTH_OVER_MONEY2" , String.valueOf(_param.monthOverMoney));
		// 過入金返金
    	_form._svf.VrsOut("RE_PAY_MONEY2" , String.valueOf(_param.rePayMoney));
		// 過入金当月末残
    	_form._svf.VrsOut("MONTH_OVER_MONEY_REMAIN2" , String.valueOf(_param.monthOverMoneyRemain));
		
        _form._svf.VrEndRecord();

    }

    /**
     * 合計エリアの出力
     * @param obj		帳票orCSVの出力インターフェースオブジェクト
     */
    private void setTotalRecord() {

    	//*================*
		//* 合計           *
		//*================*
    	// タイトル
    	_form._svf.VrsOut("ITEM" , "合　計");
		// 過入金前月末残
    	_form._svf.VrsOut("LAST_MONTH_OVER_REMAIN2" , String.valueOf(_param.t_lastMonthOverRemain));
		// 当月過入金
    	_form._svf.VrsOut("MONTH_OVER_MONEY2" , String.valueOf(_param.t_monthOverMoney));
		// 過入金返金
    	_form._svf.VrsOut("RE_PAY_MONEY2" , String.valueOf(_param.t_rePayMoney));
		// 過入金当月末残
    	_form._svf.VrsOut("MONTH_OVER_MONEY_REMAIN2" , String.valueOf(_param.t_monthOverMoneyRemain));
		
        _form._svf.VrEndRecord();

    }

    /**
     * 小計・合計エリアの加算
     * @param student	帳票出力対象クラスオブジェクト
     */
    private void sumSyokeiTotal(final student sudent) {

    	//*================*
		//* 小計値加算     *
		//*================*
		// 過入金前月末残
    	if(sudent._lastMonthOverRemain != null){
        	_param.lastMonthOverRemain  += Integer.parseInt(sudent._lastMonthOverRemain);
    	}
		// 当月過入金
    	if(sudent._monthOverMoney != null){
        	_param.monthOverMoney       += Integer.parseInt(sudent._monthOverMoney);
    	}
		// 過入金返金
    	if(sudent._rePayMoney != null){
        	_param.rePayMoney           += Integer.parseInt(sudent._rePayMoney);
    	}
		// 過入金当月末残
    	if(sudent._monthOverMoneyRemain != null){
        	_param.monthOverMoneyRemain += Integer.parseInt(sudent._monthOverMoneyRemain);
    	}
    	//*================*
		//* 合計値加算     *
		//*================*
		// 過入金前月末残
    	_param.t_lastMonthOverRemain  += Integer.parseInt(sudent._lastMonthOverRemain);
		// 当月過入金
    	_param.t_monthOverMoney       += Integer.parseInt(sudent._monthOverMoney);
		// 過入金返金への入金
    	_param.t_rePayMoney           += Integer.parseInt(sudent._rePayMoney);
		// 過入金当月末残
    	_param.t_monthOverMoneyRemain += Integer.parseInt(sudent._monthOverMoneyRemain);
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
                final student student = new student(rs.getString("LAST_MONTH_OVER_REMAIN"),
                		rs.getString("MONTH_OVER_MONEY"),
                		rs.getString("RE_PAY_MONEY"),
                		rs.getString("MONTH_OVER_MONEY_REMAIN"),
                		nvlT(rs.getString("SCHREGNO")),
                		nvlT(rs.getString("NAME")),
                		nvlT(rs.getString("GRADE")),
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
            + "    W1.LAST_MONTH_OVER_REMAIN,"
            + "    W1.MONTH_OVER_MONEY,"
            + "    W1.RE_PAY_MONEY,"
            + "    W1.MONTH_OVER_MONEY_REMAIN,"
            + "    W2.SCHREGNO,"
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
            + "    MONTH_OVER_PAYMENT_DAT W1"
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

    /** 生徒クラス */
    private class student {
        final String _lastMonthOverRemain;
        final String _monthOverMoney;
        final String _rePayMoney;
        final String _monthOverMoneyRemain;
        final String _schregno;
        final String _name;
        final String _grade;
        final String _schoolname1;
		
        student(
                final String lastMonthOverRemain,
                final String monthOverMoney,
                final String rePayMoney,
                final String monthOverMoneyRemain,
                final String schregno,
                final String name,
                final String grade,
                final String schoolname1
        ) {
        	_lastMonthOverRemain = lastMonthOverRemain;
        	_monthOverMoney = monthOverMoney;
        	_rePayMoney = rePayMoney;
        	_monthOverMoneyRemain = monthOverMoneyRemain;
			_schregno = schregno;
			_name = name;
			_grade = grade;
			_schoolname1 = schoolname1;
        }
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
    	private final String _nengetu;
    	private final String _date;
    	private final String _serch_yyyymm;

    	String sYearMonth = "";
        long lastMonthOverRemain = 0;	// 小計：過入金前月末残
        long monthOverMoney = 0;			// 小計：当月過入金
        long rePayMoney   = 0;			// 小計：過入金返金
        long monthOverMoneyRemain = 0;	// 小計：過入金当月末残

        int t_lastMonthOverRemain = 0;	// 小計：過入金前月末残
        int t_monthOverMoney = 0;		// 小計：当月過入金
        int t_rePayMoney   = 0;			// 小計：過入金返金
        int t_monthOverMoneyRemain = 0;	// 小計：過入金当月末残

    	
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");

            _year_month = request.getParameter("YEAR_MONTH");
            sYearMonth = _year_month+"01";
            sYearMonth = fomatDate(sYearMonth, "yyyyMMdd", "yyyy-MM-dd");
            _nengetu = KNJ_EditDate.h_format_JP_M(sYearMonth);
            _serch_yyyymm = _year_month;
            _date = KNJ_EditDate.h_format_JP(_loginDate);
        }

        /** 小計金額クリア */
        private void setSyokeiinit() {
            lastMonthOverRemain = 0;	// 小計：過入金前月末残
            monthOverMoney = 0;			// 小計：当月過入金
            rePayMoney   = 0;			// 小計：過入金返金
            monthOverMoneyRemain = 0;	// 小計：過入金当月末残
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

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    // ======================================================================
    private class Form {
        private Vrw32alp _svf;

        public Form(final String file,final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(FORM_FILE, 4);
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }


    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

}
