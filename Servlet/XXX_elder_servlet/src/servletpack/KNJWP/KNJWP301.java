// kanji=漢字
/*
 * $Id: 01a63449034c72fef3fe3bbfd8a2b8c58cb7205d $
 *
 * 作成日: 2008/01/24 1:33:59 - JST
 * 作成者: nakasone
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * 生徒別前受金管理台帳
 * 
 * @author nakasone
 *
 */
public class KNJWP301 {
    private static final String FORM_FILE = "KNJWP301.frm";
    private static final int PAGE_MAX_LINE = 30;
    
    private boolean _hasData;
    private Form _form;
    private Vrw32alp _svf;
    private DB2UDB db2;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJWP301.class);
    
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
    	// 表題：学校名称取得
		final List school = createSchool(db2);
		String titleSchoolName = getSchoolName(school);
		// 生徒別前受金管理台帳データ取得
		final List student = createStudents(db2);
		// 帳票出力のメソッド
		retflg = outPutPrint(db2, student, titleSchoolName);
		return retflg;
			
    }
    
    /**
     * 帳票出力処理
     * @param db2					ＤＢ接続オブジェクト
     * @param obj					帳票orCSVの出力インターフェースオブジェクト
     * @param student				帳票出力対象クラスオブジェクト
     * @param titleSchoolName	    ヘッダ出力用学校名称
     * return						対象データ存在フラグ
     */
    private boolean outPutPrint(final DB2UDB db2, final List student, String titleSchoolName)	throws SQLException {
		
    	boolean dataflg = false;		// 対象データ存在フラグ
    	int gyo = 1;					// 現在ページ数の判断用（行）
		int schoolcnt = 0;				// 小計印字判定用
		int reccnt = 0;					// 合計レコード数
		String gradeNew = "";			// 小計印字用ブレイクキー
		String gradeOld = "";			// 小計印字用ブレイクキー
		String sYearMonth = "";			// 期間（From)格納用
		String eYearMonth = "";			// 期間（To)格納用
		boolean endflg = true;

		int schregnocnt = 0;				// 合計レコード数
		String applicantno_new = "";			// 罫線非表示の為の学籍番号ブレイクキー
		String applicantno_old = "";			// 罫線非表示の為の学籍番号ブレイクキー 
        String schregno_new = "";            // 罫線非表示の為の学籍番号ブレイクキー
        String schregno_old = "";            // 罫線非表示の為の学籍番号ブレイクキー 
		String schregno_name = "";			// 改ページ時の生徒氏名設定用
		int iMeisai_Sai = 0;

        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();

			//30行超えた場合、行カウントに１を設定
			if (gyo > PAGE_MAX_LINE) {
				gyo = 1;
			}

            // 小計印字用ブレイクキー設定
            gradeNew = sudent._grade;
            applicantno_new = sudent._applicantno;
            schregno_new = sudent._schregno;
			if (reccnt > 0){
		        _form._svf.VrEndRecord();

				if (gyo == 1) {
					schregnocnt = 0;
					// 学籍番号
					_form._svf.VrsOut("SCHREGNO" , schregno_old);
					// 生徒氏名
					_form._svf.VrsOut(setformatArea("NAME", schregno_name, 20, "1", "2") , schregno_name);
					// 学籍番号欄の罫線非表示用
					_form._svf.VrsOut("FLG1", String.valueOf(schregnocnt));
					// 生徒氏名欄の罫線非表示用
					_form._svf.VrsOut("FLG2", String.valueOf(schregnocnt));
				}
		        
				// 学習センターが前回出力データと異なる場合 且つ
				// 同一学習センター使用者が2名以上の場合
				if((!gradeNew.equals(gradeOld)) && (schoolcnt >= 2)){
					// 小計の編集・出力
					int syokei_cnt = setSyokeiRecord(db2, gradeOld,schregnocnt);
					++schregnocnt;
					// 小計値のクリア
					_param.setSyokeiinit();
					schoolcnt = 0;
					gyo += syokei_cnt;
					// 小計印字用ブレイクキー設定
					gradeOld = gradeNew;
				} else {
					if(!gradeNew.equals(gradeOld) && schoolcnt == 1){
						// 学習センター使用者が1名の場合
						if(schoolcnt == 1){
							// 小計値のクリア
							_param.setSyokeiinit();
							schoolcnt = 0;
						}
						// 小計印字用ブレイクキー設定
						gradeOld = gradeNew;
					}
				}
			} else {
				// 小計印字用ブレイクキー設定
				gradeOld = gradeNew;
			}
			
			// 学籍番号が変わった場合
			if(!applicantno_new.equals(applicantno_old)){
				// 学籍番号
				_form._svf.VrsOut("SCHREGNO" , sudent._schregno);
				// 生徒氏名
				_form._svf.VrsOut(setformatArea("NAME", sudent._name, 20, "1", "2") , sudent._name);
				++schregnocnt;
				applicantno_old = applicantno_new;
                schregno_old = schregno_new;
				schregno_name = sudent._name;
				++schoolcnt;
			}
			
			//*================*
			//* ヘッダ         *
			//*================*
			_form._svf.VrsOut("YEAR_MONTH1"	,_param._nengetu);	// 対象年月
			_form._svf.VrsOut("TITLE"		, titleSchoolName);	// 学校名称
			_form._svf.VrsOut("LOGINDATE"	,_param._date);		// 作成日
			//*================*
			//* 明細           *
			//*================*
			// 学籍番号欄の罫線非表示用
			_form._svf.VrsOut("FLG1", String.valueOf(schregnocnt));
			// 生徒氏名欄の罫線非表示用
			_form._svf.VrsOut("FLG2", String.valueOf(schregnocnt));
			// 学習センター
			_form._svf.VrsOut("SCHOOLNAME1" , sudent._schoolname1);
			// 売上予定収益明細
			_form._svf.VrsOut(setformatArea("COMMODITY_ABBV", sudent._commodityAbbv, 20, "1", "2") , sudent._commodityAbbv);
			// 期間(FROM)
			if(!sudent._sYearMonth.equals("")){
	            sYearMonth = fomatDate(sudent._sYearMonth+"01", "yyyyMMdd", "yyyy'年'M'月'");
			}
			// 期間(TO)
			if(!sudent._eYearMonth.equals("")){
	            eYearMonth = fomatDate(sudent._eYearMonth+"01", "yyyyMMdd", "yyyy'年'M'月'");
			}
            if(sudent._sYearMonth.equals(sudent._eYearMonth)){
    			// 期間(FROM)と期間(TO)が等しい場合は、期間(FROM)のみ印字
    			_form._svf.VrsOut("YEAR_MONTH2" , sYearMonth);
            } else {
    			_form._svf.VrsOut("YEAR_MONTH2" , sYearMonth + "\uFF5E" + eYearMonth);
            }
			// 売上予定額（税込）
			_form._svf.VrsOut("SALES_SCHEDULE_MONEY" , sudent._salesScheduleMoney);
			// 金額がマイナスの場合
			if(Integer.parseInt(sudent._keepingMoney) < 0){
				_form._svf.VrsOut("KEEPING_MONEY" , "0");	// 前受残
				iMeisai_Sai = 0;
				iMeisai_Sai = Integer.parseInt(sudent._difference) + (Math.abs(Integer.parseInt(sudent._keepingMoney)));	// 差異
				_form._svf.VrsOut("DIFFERENCE" , String.valueOf(iMeisai_Sai));
			} else {
				_form._svf.VrsOut("KEEPING_MONEY" , sudent._keepingMoney);	// 前受残
				_form._svf.VrsOut("DIFFERENCE" , sudent._difference);		// 差異
			}
			// 差異
			if(Integer.parseInt(sudent._difference) < 0){
				
			} else {
				_form._svf.VrsOut("DIFFERENCE" , sudent._difference);
			}
			
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
				schregnocnt = 0;
			}
	        _form._svf.VrEndRecord();	// 明細レコードを出力
			if(schoolcnt >= 2){
				int syokei_cnt = setSyokeiRecord(db2, gradeOld,schregnocnt);
				++schregnocnt;
				gyo += syokei_cnt;
			}
			// 最終行に達していない場合
			if (gyo < PAGE_MAX_LINE) {
	    		while(endflg){
	        		if(gyo >= PAGE_MAX_LINE){
    					//合計を編集・出力
	        			setTotalRecord(db2,schregnocnt);
	        			++schregnocnt;
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
    			setTotalRecord(db2,schregnocnt);
	            endflg = false;
	    	}
		}
		
		return dataflg;
    }

    /**
     * 小計エリアの出力
     * @param db2		ＤＢ接続オブジェクト
     * @param grade	小計対象の所属
     */
    private int setSyokeiRecord(final DB2UDB db2, String grade, int shregcnt)	throws SQLException {

    	boolean dataflg = false;
    	int salesScheduleMoney = 0;
    	int keepingMoney = 0;
    	int difference = 0;
    	int iSyokei_Sai = 0;
    	int iSyokei_Total_Sai = 0;
    	int line = 0;
    	
    	String schoolName = "";
    	
    	
    	// 学籍番号、生徒氏名の罫線非表示用項目の設定値
    	++shregcnt;
    	
    	// 小計データ取得
		final List syokei = createSyokeiData(db2, grade);
    	
		_form._svf.VrsOut("NAME1" , "小計");
		
        for (Iterator it = syokei.iterator(); it.hasNext();) {
            final syokei syokeiData = (syokei) it.next();
            schoolName = syokeiData._schoolname1;
			_form._svf.VrsOut("SCHOOLNAME1" , syokeiData._schoolname1);
			_form._svf.VrsOut("FLG1", String.valueOf(shregcnt));
			_form._svf.VrsOut("FLG2", String.valueOf(shregcnt));
    		// 売上予定収益名
			_form._svf.VrsOut(setformatArea("COMMODITY_ABBV", syokeiData._commodityAbbv, 20, "1", "2") , syokeiData._commodityAbbv);
    		// 売上予定額（税込）
        	salesScheduleMoney += Integer.parseInt(syokeiData._salesScheduleMoney);
        	_form._svf.VrsOut("SALES_SCHEDULE_MONEY" , syokeiData._salesScheduleMoney);
    		// 前受残
        	keepingMoney += Integer.parseInt(syokeiData._keepingMoney);
    		// 差異
        	difference += Integer.parseInt(syokeiData._difference);
			if(Integer.parseInt(syokeiData._keepingMoney) < 0){
	        	_form._svf.VrsOut("KEEPING_MONEY" , "0");				// 前受残
	        	iSyokei_Sai = 0;
	        	iSyokei_Sai = Integer.parseInt(syokeiData._difference) +
	        	(Math.abs(Integer.parseInt(syokeiData._keepingMoney)));
				_form._svf.VrsOut("DIFFERENCE" , String.valueOf(iSyokei_Sai));		// 差異
			} else {
	        	_form._svf.VrsOut("KEEPING_MONEY"	, syokeiData._keepingMoney);	// 前受残
	        	_form._svf.VrsOut("DIFFERENCE" 		, syokeiData._difference);		// 差異
			}

        	_form._svf.VrEndRecord();
        	dataflg = true;
        	++line;
        }

        // 学習センター小計
        if(dataflg){
			_form._svf.VrsOut("SCHOOLNAME1" , schoolName);
			_form._svf.VrsOut("FLG1", String.valueOf(shregcnt));
			_form._svf.VrsOut("FLG2", String.valueOf(shregcnt));
        	_form._svf.VrsOut("COMMODITY_ABBV1" , "学習センター小計");
    		// 売上予定額（税込）
        	_form._svf.VrsOut("SALES_SCHEDULE_MONEY" , String.valueOf(salesScheduleMoney));

			if(keepingMoney < 0){
	        	_form._svf.VrsOut("KEEPING_MONEY" , "0");    			// 前受残
	        	iSyokei_Total_Sai = 0;
	        	iSyokei_Total_Sai = difference + (Math.abs(keepingMoney));
				_form._svf.VrsOut("DIFFERENCE" , String.valueOf(iSyokei_Total_Sai));	// 差異
			} else {
	        	_form._svf.VrsOut("KEEPING_MONEY" , String.valueOf(keepingMoney));
	        	_form._svf.VrsOut("DIFFERENCE" , String.valueOf(difference));
			}

        	_form._svf.VrEndRecord();
        	++line;
        }
        return line;
    }
    
    /**
     * 合計エリアの出力
     * @param db2		ＤＢ接続オブジェクト
     */
    private void setTotalRecord(final DB2UDB db2, int schregnocnt)	throws SQLException {

    	boolean dataflg = false;
    	int t_salesScheduleMoney = 0;
    	int t_keepingMoney = 0;
    	int t_difference = 0;
    	int iGokei_Sai = 0;
    	int iGokei_Total_Sai = 0;
    	int line = 0;

    	// 学籍番号、生徒氏名の罫線非表示用項目の設定値
    	++schregnocnt;

    	// 合計データ取得
    	final List gokei = createGokeiData(db2);
    	
        for (Iterator it = gokei.iterator(); it.hasNext();) {
            final gokei gokeiData = (gokei) it.next();
			_form._svf.VrsOut("FLG1", String.valueOf(schregnocnt));
			_form._svf.VrsOut("FLG2", String.valueOf(schregnocnt));
    		// タイトル
    		_form._svf.VrsOut("SCHOOLNAME1" , "合計");
    		// 売上予定収益名
			_form._svf.VrsOut(setformatArea("COMMODITY_ABBV", gokeiData._commodityAbbv, 20, "1", "2") , gokeiData._commodityAbbv);
    		// 売上予定額（税込）
        	t_salesScheduleMoney += Integer.parseInt(gokeiData._salesScheduleMoney);
        	_form._svf.VrsOut("SALES_SCHEDULE_MONEY" , gokeiData._salesScheduleMoney);
    		// 前受残
        	t_keepingMoney += Integer.parseInt(gokeiData._keepingMoney);
    		// 差異
        	t_difference += Integer.parseInt(gokeiData._difference);

        	if(Integer.parseInt(gokeiData._keepingMoney) < 0){
	        	_form._svf.VrsOut("KEEPING_MONEY" , "0");	// 前受残
	        	iGokei_Sai = 0;
				iGokei_Sai = Integer.parseInt(gokeiData._difference) + (Math.abs(Integer.parseInt(gokeiData._keepingMoney)));
				_form._svf.VrsOut("DIFFERENCE" , String.valueOf(iGokei_Sai));	// 差異
			} else {
	        	_form._svf.VrsOut("KEEPING_MONEY"	, gokeiData._keepingMoney);	// 前受残
	        	_form._svf.VrsOut("DIFFERENCE" 		, gokeiData._difference);	// 差異
			}

        	_form._svf.VrEndRecord();
        	dataflg = true;
        	++line;
        }

        // 学習センター合計
        if(dataflg){
			_form._svf.VrsOut("FLG1", String.valueOf(schregnocnt));
			_form._svf.VrsOut("FLG2", String.valueOf(schregnocnt));
    		_form._svf.VrsOut("SCHOOLNAME1" , "合計");
    		// 売上予定収益名
        	_form._svf.VrsOut("COMMODITY_ABBV1" , "合計");
    		// 売上予定額（税込）
        	_form._svf.VrsOut("SALES_SCHEDULE_MONEY" , String.valueOf(t_salesScheduleMoney));

			if(t_keepingMoney < 0){
	        	_form._svf.VrsOut("KEEPING_MONEY" , "0");    			// 前受残
	        	iGokei_Total_Sai = 0;
				iGokei_Total_Sai = t_difference + (Math.abs(t_keepingMoney));
				_form._svf.VrsOut("DIFFERENCE" , String.valueOf(iGokei_Total_Sai));		// 差異
			} else {
	        	_form._svf.VrsOut("KEEPING_MONEY" , String.valueOf(t_keepingMoney));	// 前受残
	        	_form._svf.VrsOut("DIFFERENCE" , String.valueOf(t_difference));			// 差異
			}

        	_form._svf.VrEndRecord();
        }

    }

    /**
     * 帳票出力対象小計データ抽出処理
     * @param db2		ＤＢ接続オブジェクト
     * @param grade	小計対象の所属
     * @return			帳票出力対象データリスト
     * @throws Exception
     */
    private List createSyokeiData(final DB2UDB db2, String grade)	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getSyokeiDataSql(grade);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final syokei syokei = new syokei(rs.getString("GRADE"),
                	nvlT(rs.getString("COMMODITY_CD")),
                	nvlT(rs.getString("SCHOOLNAME1")),
                	nvlT(rs.getString("COMMODITY_ABBV")),
                	rs.getString("SALES_SCHEDULE_MONEY"),
                	rs.getString("KEEPING_MONEY"),
                	rs.getString("DIFFERENCE")
                );
                rtnList.add(syokei);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /**
     * 帳票出力対象小計データ抽出ＳＱＬ生成処理
     * @param grade		小計対象の所属
     * @return				SQL文字列
     * @throws Exception
     */
    private String getSyokeiDataSql(String grade){
        final String rtn;
        rtn = "WITH MAIN_T AS ( "
            + "select"
            + "    CASE WHEN W3.SCHREGNO IS NOT NULL"
            + "         THEN W3.GRADE"
            + "         ELSE W6.BELONGING_DIV"
            + "    END AS GRADE,"
            + "    W1.COMMODITY_CD,"
            + "    CASE WHEN W3.SCHREGNO IS NOT NULL"
            + "         THEN W4.SCHOOLNAME1"
            + "         ELSE W7.SCHOOLNAME1"
            + "    END AS SCHOOLNAME1,"
            + "    W5.COMMODITY_ABBV,"
            + "    SUM(VALUE(W1.SALES_SCHEDULE_MONEY,0)) AS SALES_SCHEDULE_MONEY,"
            + "    SUM(CASE WHEN VALUE(W1.KEEPING_MONEY,0) < 0"
            + "             THEN 0"
            + "             ELSE VALUE(W1.KEEPING_MONEY,0)"
            + "        END"
            + "    ) AS KEEPING_MONEY,"
            + "    SUM(CASE WHEN VALUE(W1.KEEPING_MONEY,0) < 0"
            + "             THEN VALUE(W1.DIFFERENCE,0) + ABS(VALUE(W1.KEEPING_MONEY,0))"
            + "             ELSE VALUE(W1.DIFFERENCE,0)"
            + "        END"
            + "    ) AS DIFFERENCE"
            + " from"
            + "    MONTH_KEEPING_MONEY_DAT W1"
            + "    LEFT JOIN SCHREG_BASE_MST W2 ON"
            + "        W1.APPLICANTNO = W2.APPLICANTNO"
            + "    LEFT JOIN SCHREG_REGD_DAT W3 ON"
            + "        W2.SCHREGNO = W3.SCHREGNO and"
            + "        W3.YEAR = '" + _param._year + "' and"			// 対象年度
            + "        W3.SEMESTER = '" + _param._semester + "'"		// 学期
            + "    LEFT JOIN BELONGING_MST W4 ON"
            + "        W3.GRADE = W4.BELONGING_DIV "					// 所属
            + "    LEFT JOIN COMMODITY_MST W5 ON"
            + "        W1.COMMODITY_CD = W5.COMMODITY_CD"
            + "    INNER JOIN APPLICANT_BASE_MST W6 ON"
            + "        W1.APPLICANTNO = W6.APPLICANTNO "            // 志願者基礎
            + "    LEFT JOIN BELONGING_MST W7 ON"
            + "        W6.BELONGING_DIV = W7.BELONGING_DIV "        // 所属(志願者)
            + "    where"
            + "    W1.YEAR_MONTH = '" + _param._serch_yyyymm + "' "	// 年月
            + "    group by"
            + "    CASE WHEN W3.SCHREGNO IS NOT NULL"
            + "         THEN W3.GRADE"
            + "         ELSE W6.BELONGING_DIV"
            + "    END,"
            + "    W1.COMMODITY_CD,"
            + "    CASE WHEN W3.SCHREGNO IS NOT NULL"
            + "         THEN W4.SCHOOLNAME1"
            + "         ELSE W7.SCHOOLNAME1"
            + "    END,"
            + "    W5.COMMODITY_ABBV"
            + "    order by"
            + "    CASE WHEN W3.SCHREGNO IS NOT NULL"
            + "         THEN W3.GRADE"
            + "         ELSE W6.BELONGING_DIV"
            + "    END,"
            + "    W1.COMMODITY_CD,SCHOOLNAME1,W5.COMMODITY_ABBV"
            + " ) "
            + "SELECT * FROM MAIN_T WHERE GRADE = '" + grade + "' ";
            ;
        log.debug("帳票出力対象小計データ抽出 の SQL=" + rtn);
        return rtn;
    }

    /** 小計データクラス */
    private class syokei {
        final String _grade;
        final String _commodityCd;
        final String _schoolname1;
        final String _commodityAbbv;
        final String _salesScheduleMoney;
        final String _keepingMoney;
        final String _difference;

        syokei(
                final String grade,
                final String commodityCd,
                final String schoolname1,
                final String commodityAbbv,
                final String salesScheduleMoney,
                final String keepingMoney,
                final String difference
        ) {
        	_grade = grade;
        	_commodityCd = commodityCd;
        	_schoolname1 = schoolname1;
        	_commodityAbbv = commodityAbbv;
        	_salesScheduleMoney = salesScheduleMoney;
        	_keepingMoney = keepingMoney;
        	_difference = difference;
        }
    }

    /**
     * 帳票出力対象合計データ抽出処理
     * @param db2		ＤＢ接続オブジェクト
     * @return			帳票出力対象データリスト
     * @throws Exception
     */
    private List createGokeiData(final DB2UDB db2)	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getGokeiDataSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final gokei gokei = new gokei(rs.getString("COMMODITY_CD"),
                	nvlT(rs.getString("COMMODITY_ABBV")),
                	rs.getString("SALES_SCHEDULE_MONEY"),
                	rs.getString("KEEPING_MONEY"),
                	rs.getString("DIFFERENCE")
                );
                rtnList.add(gokei);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /**
     * 帳票出力対象合計データ抽出ＳＱＬ生成処理
     * @return			SQL文字列
     * @throws Exception
     */
    private String getGokeiDataSql(){
        final String rtn;
        rtn = " select"
            + "    W1.COMMODITY_CD,"
            + "    W4.COMMODITY_ABBV,"
            + "    SUM(VALUE(W1.SALES_SCHEDULE_MONEY,0)) AS SALES_SCHEDULE_MONEY,"
            + "    SUM(CASE WHEN VALUE(W1.KEEPING_MONEY,0) < 0"
            + "             THEN 0"
            + "             ELSE VALUE(W1.KEEPING_MONEY,0)"
            + "        END"
            + "    ) AS KEEPING_MONEY,"
            + "    SUM(CASE WHEN VALUE(W1.KEEPING_MONEY,0) < 0"
            + "             THEN VALUE(W1.DIFFERENCE,0) + ABS(VALUE(W1.KEEPING_MONEY,0))"
            + "             ELSE VALUE(W1.DIFFERENCE,0)"
            + "        END"
            + "    ) AS DIFFERENCE"
            + " from"
            + "    MONTH_KEEPING_MONEY_DAT W1"
            + "    LEFT JOIN SCHREG_BASE_MST W2 ON"
            + "        W1.APPLICANTNO = W2.APPLICANTNO "            // 志願者番号
            + "    LEFT JOIN COMMODITY_MST W4 ON"
            + "    W1.COMMODITY_CD = W4.COMMODITY_CD"
            + "    INNER JOIN APPLICANT_BASE_MST W6 ON"
            + "        W1.APPLICANTNO = W6.APPLICANTNO "            // 志願者基礎
            + "    where"
            + "    W1.YEAR_MONTH = '" + _param._serch_yyyymm + "' "	// 年月
            + "    group by"
            + "    W1.COMMODITY_CD,W4.COMMODITY_ABBV"
            + "    order by"
            + "    W1.COMMODITY_CD,W4.COMMODITY_ABBV"
            ;
        log.debug("帳票出力対象合計データ抽出 の SQL=" + rtn);
        return rtn;
    }

    /** 合計データクラス */
    private class gokei {
        final String _commodityCd;
        final String _commodityAbbv;
        final String _salesScheduleMoney;
        final String _keepingMoney;
        final String _difference;

        gokei(
                final String commodityCd,
                final String commodityAbbv,
                final String salesScheduleMoney,
                final String keepingMoney,
                final String difference
        ) {
        	_commodityCd = commodityCd;
        	_commodityAbbv = commodityAbbv;
        	_salesScheduleMoney = salesScheduleMoney;
        	_keepingMoney = keepingMoney;
        	_difference = difference;
        }
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
                final student student = new student(rs.getString("S_YEAR_MONTH"),
                		rs.getString("E_YEAR_MONTH"),
                		rs.getString("SALES_SCHEDULE_MONEY"),
                		rs.getString("KEEPING_MONEY"),
                		rs.getString("DIFFERENCE"),
                        rs.getString("APPLICANTNO"),
                        rs.getString("SCHREGNO"),
                		nvlT(rs.getString("NAME")),
                		rs.getString("GRADE"),
                		nvlT(rs.getString("SCHOOLNAME1")),
                		nvlT(rs.getString("COMMODITY_ABBV"))
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
            + "    W1.S_YEAR_MONTH,"
            + "    W1.E_YEAR_MONTH,"
            + "    VALUE(W1.SALES_SCHEDULE_MONEY,0) AS SALES_SCHEDULE_MONEY,"
            + "    VALUE(W1.KEEPING_MONEY,0) AS KEEPING_MONEY,"
            + "    VALUE(W1.DIFFERENCE,0) AS DIFFERENCE,"
            + "    W1.APPLICANTNO,"
            + "    W2.SCHREGNO,"
            + "    CASE WHEN W2.SCHREGNO IS NOT NULL"
            + "         THEN W2.NAME"
            + "         ELSE W6.NAME"
            + "    END AS NAME,"
            + "    CASE WHEN W3.SCHREGNO IS NOT NULL"
            + "         THEN W3.GRADE"
            + "         ELSE W6.BELONGING_DIV"
            + "    END AS GRADE,"
            + "    CASE WHEN W3.SCHREGNO IS NOT NULL"
            + "         THEN W4.SCHOOLNAME1"
            + "         ELSE W7.SCHOOLNAME1"
            + "    END AS SCHOOLNAME1,"
            + "    W5.COMMODITY_ABBV"
            + " from"
            + "    MONTH_KEEPING_MONEY_DAT W1"
            + "    LEFT JOIN SCHREG_BASE_MST W2 ON"
            + "        W1.APPLICANTNO = W2.APPLICANTNO "			// 志願者番号
            + "    LEFT JOIN SCHREG_REGD_DAT W3 ON"
            + "        W2.SCHREGNO = W3.SCHREGNO and"				// 学籍番号
            + "        W3.YEAR = '" + _param._year + "' and"		// 対象年度
            + "        W3.SEMESTER = '" + _param._semester + "' "	// 学期
            + "    LEFT JOIN BELONGING_MST W4 ON"
            + "        W3.GRADE = W4.BELONGING_DIV "				// 所属(在籍)
            + "    LEFT JOIN COMMODITY_MST W5 ON"
            + "        W1.COMMODITY_CD = W5.COMMODITY_CD "			// 商品コード
            + "    INNER JOIN APPLICANT_BASE_MST W6 ON"
            + "        W1.APPLICANTNO = W6.APPLICANTNO "            // 志願者基礎
            + "    LEFT JOIN BELONGING_MST W7 ON"
            + "        W6.BELONGING_DIV = W7.BELONGING_DIV "        // 所属(志願者)
            + " where"
            + "    W1.YEAR_MONTH = '" + _param._serch_yyyymm + "' "	// 年月
            + " order by"
            + "    CASE WHEN W3.SCHREGNO IS NOT NULL"
            + "         THEN W3.GRADE"
            + "         ELSE W6.BELONGING_DIV"
            + "    END,"
            + "    W1.APPLICANTNO, W1.COMMODITY_CD"			// 所属、学籍番号
            ;
        log.debug("帳票出力対象データ抽出 の SQL=" + rtn);
        return rtn;
    }

    /** 生徒クラス */
    private class student {
        final String _sYearMonth;
        final String _eYearMonth;
        final String _salesScheduleMoney;
        final String _keepingMoney;
        final String _difference;
        final String _applicantno;
        final String _schregno;
        final String _name;
        final String _grade;
        final String _schoolname1;
        final String _commodityAbbv;
		
        student(
                final String sYearMonth,
                final String eYearMonth,
                final String salesScheduleMoney,
                final String keepingMoney,
                final String difference,
                final String applicantno,
                final String schregno,
                final String name,
                final String grade,
                final String schoolname1,
                final String commodityAbbv
        ) {
        	_sYearMonth = sYearMonth;
        	_eYearMonth = eYearMonth;
        	_salesScheduleMoney = salesScheduleMoney;
        	_keepingMoney = keepingMoney;
        	_difference = difference;
            _applicantno = applicantno;
        	_schregno = schregno;
        	_name = name;
        	_grade = grade;
        	_schoolname1 = schoolname1;
        	_commodityAbbv = commodityAbbv;
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

    	HashMap hSyokei = new HashMap();
    	String sYearMonth = "";
        int salesScheduleMoney = 0;		// 小計：売上予定額（税込）
        int keepingMoney = 0;			// 小計：前受残
        int difference   = 0;			// 小計：差異

        int t_salesScheduleMoney = 0;	// 合計：売上予定額（税込）
        int t_keepingMoney = 0;			// 合計：前受残
        int t_difference   = 0;			// 合計：差異

    	
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
            salesScheduleMoney = 0;		// 小計：売上予定額（税込）
            keepingMoney = 0;			// 小計：前受残
            difference   = 0;			// 小計：差異
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
    	if(area_len > sval.length()){
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
