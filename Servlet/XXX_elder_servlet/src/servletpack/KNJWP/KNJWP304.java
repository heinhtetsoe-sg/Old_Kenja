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
 * 入金売上実績管理表
 * 
 * @author nakasone
 *
 */
public class KNJWP304 {
    private static final String FORM_FILE = "KNJWP304.frm";
    private static final int PAGE_MAX_LINE = 30;
    // タイトル：入金
    private static final String TITLE_NYUKIN = "入金";
    // タイトル：売上
    private static final String TITLE_URI = "売上";
    // タイトル：新規
    private static final String TITLE_SINKI = "新規";
    // タイトル：進級
    private static final String TITLE_SINKYU = "進級";
    // タイトル：小計
    private static final String TITLE_SYOKEI = "小計";
    // タイトル：合計
    private static final String TITLE_GOKEI = "合計";
    // 入金データ：データ区分(新規)
    private static final String SINKI_DATA_DIV = "1";
    // 入金データ：データ区分(進級)
    private static final String SINKYU_DATA_DIV = "2";
    // 学生区分(通学生)
    private static final String STUDENT_DIV_1 = "1";
    // 学生区分(サポート生)
    private static final String STUDENT_DIV_2 = "2";
    // 学生区分(のみ生)
    private static final String STUDENT_DIV_3 = "3";
    // 学生区分(提携先生)
    private static final String STUDENT_DIV_4 = "4";
    // 学生区分(科目履修生)
    private static final String STUDENT_DIV_5 = "5";
    
    private boolean _hasData;
    private Form _form;
    private Vrw32alp _svf;
    private DB2UDB db2;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJWP304.class);
    
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
    	
		// ヘッダエリアの設定
		outPutPrintHead(db2);
		
		// 入金データ取得
		final List nyuKinData = createNyukinData(db2);
		// 入金のデータ帳票出力のメソッド
		retflg = outPutPrintNyukin(nyuKinData);
		
		// 売上データ取得
		final List uriKinData = createUriageData(db2);
		// 売上のデータ帳票出力のメソッド
		retflg = outPutPrintUriage(db2, uriKinData);
		
		return retflg;
			
    }

    /**
     * ヘッダエリアの出力
     */
    private void outPutPrintHead(final DB2UDB db2)	throws SQLException {

    	// 表題の学校名を取得
		final List school = createSchool(db2);
		String titleSchoolName = getSchoolName(school);

		//*================*
		//* ヘッダ         *
		//*================*
		_form._svf.VrsOut("YEAR_MONTH"	,_param._nengetu);	// 対象年月
		_form._svf.VrsOut("TITLE"		, titleSchoolName);	// 学校名称
		_form._svf.VrsOut("LOGINDATE"	,_param._date);		// 作成日
    }

    /**
     * 入金エリアの出力
     * @param nyuKinData		帳票出力対象クラスオブジェクト
     */
    private boolean outPutPrintNyukin(final List nyuKinData) {

    	boolean retflg = false;
    	int line = 0;					// 件数
    	String new_DataDiv = "";	// データ区分格納用
    	String old_DataDiv = "";	// データ区分格納用

    	// 入金データ出力
        for (Iterator it = nyuKinData.iterator(); it.hasNext();) {
            final NyuKinData nyukin = (NyuKinData) it.next();

            // データ区分を設定
            new_DataDiv = nyukin._data_div;

            // データ区分が変わった場合、小計を設定
            if(line > 0 && !new_DataDiv.equals(old_DataDiv)){
            	outPutNyukinSyokei(old_DataDiv);
            }

            // タイトル
        	_form._svf.VrsOut("ITEM1" , TITLE_NYUKIN);
            // データ区分
        	if(nyukin._data_div.equals(SINKI_DATA_DIV)){
            	_form._svf.VrsOut("ITEM2" , TITLE_SINKI);
        	} else if(nyukin._data_div.equals(SINKYU_DATA_DIV)){
            	_form._svf.VrsOut("ITEM2" , TITLE_SINKYU);
        	}
        	// 学生区分名称
        	_form._svf.VrsOut("ITEM3" , nyukin._name);
            // 延べ人数
        	_param.paymentCnt += Long.parseLong(nyukin._payment_cnt);
        	_form._svf.VrsOut("COUNT" , String.valueOf(nyukin._payment_cnt));
    		// 入金額
        	_param.paymentMoney += Long.parseLong(nyukin._payment_money);
        	_form._svf.VrsOut("MONEY" , String.valueOf(nyukin._payment_money));
    		// 累計延べ人数
        	_param.totalPaymentCnt += Long.parseLong(nyukin._total_payment_cnt);
        	_form._svf.VrsOut("TOTAL_COUNT" , String.valueOf(nyukin._total_payment_cnt));
    		// 累計入金額
        	_param.totalPaymentMoney += Long.parseLong(nyukin._total_payment_money);
        	_form._svf.VrsOut("TOTAL_MONEY" , String.valueOf(nyukin._total_payment_money));

            // ブレイクキー設定
            old_DataDiv = new_DataDiv;
        	++line;
        	
        	// レコード出力
        	_form._svf.VrEndRecord();
        }

        // 最終行を出力
        if(line > 0){
        	// 小計エリア出力
        	outPutNyukinSyokei(old_DataDiv);
        	// 合計エリア出力
        	outPutNyukinGokei();
        	retflg = true;
        }
        return retflg;
    }

    /**
     * 売上エリアの出力
     * @param db2			ＤＢ接続オブジェクト
     * @param uriageData	帳票出力対象クラスオブジェクト
     */
    private boolean outPutPrintUriage(final DB2UDB db2, final List uriageData)	throws SQLException {

    	boolean retflg = false;
    	int line = 0;				// 件数
    	String new_studentDiv = "";	// 学生区分格納用
    	String old_studentDiv = "";	// 学生区分格納用
    	String studentName = "";

    	// 売上データ出力
        for (Iterator it = uriageData.iterator(); it.hasNext();) {
            final UriKinData uriage = (UriKinData) it.next();

            // 学生区分を退避
            new_studentDiv = uriage._student_div;

            // 学生区分が変わった場合、小計を設定
            if(line > 0 && !new_studentDiv.equals(old_studentDiv)){
            	outPutUriageSyokei(studentName);
            }

        	_form._svf.VrsOut("FLG" , "");
            // タイトル
        	_form._svf.VrsOut("ITEM1" , TITLE_URI);
        	// 学生区分名称
        	_form._svf.VrsOut("ITEM2" , uriage._name);
            // 商品名称
        	_form._svf.VrsOut("ITEM3" , uriage._group_name);
            // 延べ人数
        	_param.salesCnt += Long.parseLong(uriage._sales_cnt);
        	_form._svf.VrsOut("COUNT" , String.valueOf(uriage._sales_cnt));
    		// 売上額（税抜）
        	_param.salesPrice += Long.parseLong(uriage._sales_price);
        	_form._svf.VrsOut("SALES_PRICE" , String.valueOf(uriage._sales_price));
    		// 売上額（消費税額）
        	_param.salesTax += Long.parseLong(uriage._sales_tax);
        	_form._svf.VrsOut("SALES_TAX" , String.valueOf(uriage._sales_tax));
    		// 売上額（税込）
        	_param.salesMoney += Long.parseLong(uriage._sales_money);
        	_form._svf.VrsOut("MONEY" , String.valueOf(uriage._sales_money));
    		// 累計延べ人数
        	_param.totalSalesCnt += Long.parseLong(uriage._total_sales_cnt);
        	_form._svf.VrsOut("TOTAL_COUNT" , String.valueOf(uriage._total_sales_cnt));
    		// 累計入金額（税抜）
        	_param.totalSalesPrice += Long.parseLong(uriage._total_price);
        	_form._svf.VrsOut("TOTAL_PRICE" , String.valueOf(uriage._total_price));
    		// 累計入金額（消費税額）
        	_param.totalSalesTax += Long.parseLong(uriage._total_tax);
        	_form._svf.VrsOut("TOTAL_TAX" , String.valueOf(uriage._total_tax));
    		// 累計入金額（税込）
        	_param.totalSalesMoney += Long.parseLong(uriage._total_sales_money);
        	_form._svf.VrsOut("TOTAL_MONEY" , String.valueOf(uriage._total_sales_money));

            // ブレイクキー設定
        	old_studentDiv = new_studentDiv;
            // 学生名称を退避
            studentName = uriage._name;

            ++line;
        	// レコード出力
        	_form._svf.VrEndRecord();
        }

        // 最終行を出力
        if(line > 0){
        	// 小計エリア出力
        	outPutUriageSyokei(studentName);
        	// 合計エリア出力
        	outPutUriageGokei(db2);
        	retflg = true;
        }
        return retflg;
    }
    
    /**
     * 入金小計エリアの出力
     */
    private void outPutNyukinSyokei(String dataDiv) {

    	_form._svf.VrsOut("ITEM1" , TITLE_NYUKIN);
        // データ区分
    	if(dataDiv.equals(SINKI_DATA_DIV)){
        	_form._svf.VrsOut("ITEM2" , TITLE_SINKI);
    	} else if(dataDiv.equals(SINKYU_DATA_DIV)){
        	_form._svf.VrsOut("ITEM2" , TITLE_SINKYU);
    	}
    	_form._svf.VrsOut("ITEM3" , TITLE_SYOKEI);
        // 延べ人数
    	_form._svf.VrsOut("COUNT" , String.valueOf(_param.paymentCnt));
		// 入金額
    	_form._svf.VrsOut("MONEY" , String.valueOf(_param.paymentMoney));
		// 累計延べ人数
    	_form._svf.VrsOut("TOTAL_COUNT" , String.valueOf(_param.totalPaymentCnt));
		// 累計入金額
    	_form._svf.VrsOut("TOTAL_MONEY" , String.valueOf(_param.totalPaymentMoney));
    	
    	// レコード出力
    	_form._svf.VrEndRecord();
    	// 合計行加算
    	_param.sumGokei();
    	// 小計行の各金額エリアに初期値を設定
    	_param.setSyokeiinit();
    }
    
    /**
     * 入金合計エリアの出力
     */
    private void outPutNyukinGokei() {

    	// 罫線表示・非表示切り替え(1⇒非表示)
    	_form._svf.VrsOut("FLG" , "1");
    	_form._svf.VrsOut("ITEM1" , TITLE_NYUKIN);
    	_form._svf.VrsOut("ITEM2" , TITLE_GOKEI);
        // 延べ人数
    	_form._svf.VrsOut("COUNT" , String.valueOf(_param.t_paymentCnt));
		// 入金額
    	_form._svf.VrsOut("MONEY" , String.valueOf(_param.t_paymentMoney));
		// 累計延べ人数
    	_form._svf.VrsOut("TOTAL_COUNT" , String.valueOf(_param.t_totalPaymentCnt));
		// 累計入金額
    	_form._svf.VrsOut("TOTAL_MONEY" , String.valueOf(_param.t_totalPaymentMoney));
    	
    	// レコード出力
    	_form._svf.VrEndRecord();
    }
    
    /**
     * 売上小計エリアの出力
     */
    private void outPutUriageSyokei(String studentName) {

    	_form._svf.VrsOut("ITEM1" , TITLE_URI);
        // 学生区分
       	_form._svf.VrsOut("ITEM2" , studentName);
    	_form._svf.VrsOut("ITEM3" , TITLE_SYOKEI);
        // 延べ人数
    	_form._svf.VrsOut("COUNT" , String.valueOf(_param.salesCnt));
		// 売上額（税抜）
    	_form._svf.VrsOut("SALES_PRICE" , String.valueOf(_param.salesPrice));
		// 売上額（消費税額）
    	_form._svf.VrsOut("SALES_TAX" , String.valueOf(_param.salesTax));
		// 売上額（税込）
    	_form._svf.VrsOut("MONEY" , String.valueOf(_param.salesMoney));
		// 累計延べ人数
    	_form._svf.VrsOut("TOTAL_COUNT" , String.valueOf(_param.totalSalesCnt));
		// 累計売上額（税抜）
    	_form._svf.VrsOut("TOTAL_PRICE" , String.valueOf(_param.totalSalesPrice));
		// 累計売上額（消費税額）
    	_form._svf.VrsOut("TOTAL_TAX" , String.valueOf(_param.totalSalesTax));
		// 累計売上額（税込）
    	_form._svf.VrsOut("TOTAL_MONEY" , String.valueOf(_param.totalSalesMoney));
    	
    	// レコード出力
    	_form._svf.VrEndRecord();
    	// 小計行の各金額エリアに初期値を設定
    	_param.setUriSyokeiinit();
    }
    
    /**
     * 売上合計エリアの出力
     */
    private void outPutUriageGokei(final DB2UDB db2)	throws SQLException {

    	boolean dataflg = false;
    	
    	// 売上合計データ取得
		final List uriData = createUriageSummaryData(db2);
		
    	// 売上データ出力
        for (Iterator it = uriData.iterator(); it.hasNext();) {
            final UriKinSummaryData uriage = (UriKinSummaryData) it.next();

        	_form._svf.VrsOut("ITEM1" , TITLE_URI);
        	_form._svf.VrsOut("ITEM2" , TITLE_GOKEI);
        	// 商品名称
        	_form._svf.VrsOut("ITEM3" , uriage._group_name);
            // 延べ人数
        	_param.t_salesCnt += Long.parseLong(uriage._sales_cnt);
        	_form._svf.VrsOut("COUNT" , uriage._sales_cnt);
    		// 売上額（税抜）
        	_param.t_salesPrice += Long.parseLong(uriage._sales_price);
        	_form._svf.VrsOut("SALES_PRICE" , uriage._sales_price);
    		// 売上額（消費税額）
        	_param.t_salesTax += Long.parseLong(uriage._sales_tax);
        	_form._svf.VrsOut("SALES_TAX" , uriage._sales_tax);
    		// 売上額（税込）
        	_param.t_salesMoney += Long.parseLong(uriage._sales_money);
        	_form._svf.VrsOut("MONEY" , uriage._sales_money);
    		// 累計延べ人数
        	_param.t_totalSalesCnt += Long.parseLong(uriage._total_sales_cnt);
        	_form._svf.VrsOut("TOTAL_COUNT" , uriage._total_sales_cnt);
    		// 累計売上額（税抜）
        	_param.t_totalSalesPrice += Long.parseLong(uriage._total_price);
        	_form._svf.VrsOut("TOTAL_PRICE" , uriage._total_price);
    		// 累計売上額（消費税額）
        	_param.t_totalSalesTax += Long.parseLong(uriage._total_tax);
        	_form._svf.VrsOut("TOTAL_TAX" , uriage._total_tax);
    		// 累計売上額（税込）
        	_param.t_totalSalesMoney += Long.parseLong(uriage._total_sales_money);
        	_form._svf.VrsOut("TOTAL_MONEY" , uriage._total_sales_money);
        	
        	// レコード出力
        	_form._svf.VrEndRecord();
        	dataflg = true;
        }

        if(dataflg){
        	_form._svf.VrsOut("ITEM1" , TITLE_URI);
        	_form._svf.VrsOut("ITEM2" , TITLE_GOKEI);
        	_form._svf.VrsOut("ITEM3" , TITLE_GOKEI);
        	
            // 延べ人数
        	_form._svf.VrsOut("COUNT" , String.valueOf(_param.t_salesCnt));
    		// 売上額（税抜）
        	_form._svf.VrsOut("SALES_PRICE" , String.valueOf(_param.t_salesPrice));
    		// 売上額（消費税額）
        	_form._svf.VrsOut("SALES_TAX" , String.valueOf(_param.t_salesTax));
    		// 売上額（税込）
        	_form._svf.VrsOut("MONEY" , String.valueOf(_param.t_salesMoney));
    		// 累計延べ人数
        	_form._svf.VrsOut("TOTAL_COUNT" , String.valueOf(_param.t_totalSalesCnt));
    		// 累計売上額（税抜）
        	_form._svf.VrsOut("TOTAL_PRICE" , String.valueOf(_param.t_totalSalesPrice));
    		// 累計売上額（消費税額）
        	_form._svf.VrsOut("TOTAL_TAX" , String.valueOf(_param.t_totalSalesTax));
    		// 累計売上額（税込）
        	_form._svf.VrsOut("TOTAL_MONEY" , String.valueOf(_param.t_totalSalesMoney));
        	
        	// レコード出力
        	_form._svf.VrEndRecord();
        }
    }

    /**
     * 入金データ取得
     * @param db2			ＤＢ接続オブジェクト
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createNyukinData(final DB2UDB db2)	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getNyukinDataSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final NyuKinData nyukin = new NyuKinData(rs.getString("DATA_DIV"),
                		rs.getString("COMMUTING_DIV"),
                		rs.getString("NAME"),
                		rs.getString("PAYMENT_MONEY"),
                		rs.getString("PAYMENT_CNT"),
                		rs.getString("TOTAL_PAYMENT_MONEY"),
                		rs.getString("TOTAL_PAYMENT_CNT")
                );
                rtnList.add(nyukin);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /**
     * 帳票出力対象入金データ抽出ＳＱＬ生成処理
     * @return				SQL文字列
     * @throws Exception
     */
    private String getNyukinDataSql(){

    	StringBuffer stb = new StringBuffer();

		stb.append(" WITH TEST_T AS (");
		stb.append("     select");
		stb.append("        W1.DATA_DIV,");
		stb.append("        W1.COMMUTING_DIV,");
		stb.append("        SUM(VALUE(CAST(W1.PAYMENT_MONEY AS BIGINT),0)) AS PAYMENT_MONEY,");
		stb.append("        SUM(VALUE(CAST(W1.PAYMENT_CNT AS BIGINT),0)) AS PAYMENT_CNT,");
		stb.append("        SUM(VALUE(CAST(W1.TOTAL_PAYMENT_MONEY AS BIGINT),0)) AS TOTAL_PAYMENT_MONEY,");
		stb.append("        SUM(VALUE(CAST(W1.TOTAL_PAYMENT_CNT AS BIGINT),0)) AS TOTAL_PAYMENT_CNT");
		stb.append("     from");
		stb.append("        MONTH_PAYMENT_DAT W1");
		stb.append("     where");
		stb.append("        W1.YEAR_MONTH = '" + _param._serch_yyyymm + "'");
		stb.append("     group by");
		stb.append("        W1.DATA_DIV,W1.COMMUTING_DIV");
		stb.append("     order by");
		stb.append("        W1.DATA_DIV,W1.COMMUTING_DIV");
		stb.append(" ) ");
		stb.append(" select");
		stb.append("    T1.DATA_DIV,");
		stb.append("    T1.COMMUTING_DIV,");
		stb.append("    T2.NAME1 AS NAME,");
		stb.append("    T1.PAYMENT_MONEY,");
		stb.append("    T1.PAYMENT_CNT,");
		stb.append("    T1.TOTAL_PAYMENT_MONEY,");
		stb.append("    T1.TOTAL_PAYMENT_CNT");
		stb.append(" from");
		stb.append(" TEST_T T1");
		stb.append(" left outer join NAME_MST T2 on");
		stb.append("    T1.COMMUTING_DIV = T2.NAMECD2 and");
		stb.append("    T2.NAMECD1 = 'W009'");
		stb.append(" order by");
		stb.append("    T1.DATA_DIV,T1.COMMUTING_DIV");
        
        log.debug("帳票出力対象データ抽出 の SQL=" + stb.toString());
		return stb.toString();
    }

    /** 入金データクラス */
    private class NyuKinData {
        final String _data_div;
        final String _student_div;
        final String _name;
        final String _payment_money;
        final String _payment_cnt;
        final String _total_payment_money;
        final String _total_payment_cnt;
		
        NyuKinData(
                final String data_div,
                final String student_div,
                final String name,
                final String payment_money,
                final String payment_cnt,
                final String total_payment_money,
                final String total_payment_cnt
        ) {
        	_data_div = data_div;
        	_student_div = student_div;
        	_name = name;
        	_payment_money = payment_money;
        	_payment_cnt = payment_cnt;
        	_total_payment_money = total_payment_money;
        	_total_payment_cnt = total_payment_cnt;
        }
    }

    /**
     * 売上データ取得
     * @param db2			ＤＢ接続オブジェクト
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createUriageData(final DB2UDB db2)	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getUriageDataSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final UriKinData urikin = new UriKinData(
                		rs.getString("COMMUTING_DIV"),
                		rs.getString("NAME"),
                		rs.getString("GROUP_CD"),
                		rs.getString("SHOWORDER"),
                		rs.getString("GROUP_NAME"),
                		rs.getString("SALES_PRICE"),
                		rs.getString("SALES_TAX"),
                		rs.getString("SALES_MONEY"),
                		rs.getString("SALES_CNT"),
                		rs.getString("TOTAL_PRICE"),
                		rs.getString("TOTAL_TAX"),
                		rs.getString("TOTAL_SALES_MONEY"),
                		rs.getString("TOTAL_SALES_CNT")
                );
                rtnList.add(urikin);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /**
     * 帳票出力対象売上データ抽出ＳＱＬ生成処理
     * @return				SQL文字列
     * @throws Exception
     */
    private String getUriageDataSql(){

    	StringBuffer stb = new StringBuffer();

		stb.append(" WITH TEST_T AS ( ");
		stb.append("     select");
		stb.append("        W1.COMMUTING_DIV,");
		stb.append("        W1.COMMODITY_CD,");
		stb.append("        SUM(VALUE(CAST(W1.SALES_PRICE AS BIGINT),0)) AS SALES_PRICE,");
		stb.append("        SUM(VALUE(CAST(W1.SALES_TAX AS BIGINT),0)) AS SALES_TAX,");
		stb.append("        SUM(VALUE(CAST(W1.SALES_MONEY AS BIGINT),0)) AS SALES_MONEY,");
		stb.append("        SUM(VALUE(CAST(W1.SALES_CNT AS BIGINT),0)) AS SALES_CNT,");
		stb.append("        SUM(VALUE(CAST(W1.TOTAL_PRICE AS BIGINT),0)) AS TOTAL_PRICE,");
		stb.append("        SUM(VALUE(CAST(W1.TOTAL_TAX AS BIGINT),0)) AS TOTAL_TAX,");
		stb.append("        SUM(VALUE(CAST(W1.TOTAL_SALES_MONEY AS BIGINT),0)) AS TOTAL_SALES_MONEY,");
		stb.append("        SUM(VALUE(CAST(W1.TOTAL_SALES_CNT AS BIGINT),0)) AS TOTAL_SALES_CNT");
		stb.append("     from");
		stb.append("        MONTH_SALES_DAT W1");
		stb.append("     where");
		stb.append("        W1.YEAR_MONTH = '" + _param._serch_yyyymm + "'");
		stb.append("     group by");
		stb.append("        W1.COMMUTING_DIV,W1.COMMODITY_CD");
		stb.append("     order by");
		stb.append("        W1.COMMUTING_DIV,W1.COMMODITY_CD");
		stb.append(" ) ");
		stb.append(" select");
		stb.append("    T1.COMMUTING_DIV,");
		stb.append("    T2.NAME1 AS NAME,");
		stb.append("    T5.GROUP_CD,");
		stb.append("    T5.SHOWORDER,");
		stb.append("    T5.GROUP_NAME,");
		stb.append("    SUM(VALUE(CAST(T1.SALES_PRICE AS BIGINT), 0)) AS SALES_PRICE,");
		stb.append("    SUM(VALUE(CAST(T1.SALES_TAX AS BIGINT), 0)) AS SALES_TAX,");
		stb.append("    SUM(VALUE(CAST(T1.SALES_MONEY AS BIGINT), 0)) AS SALES_MONEY,");
		stb.append("    SUM(VALUE(CAST(T1.SALES_CNT AS BIGINT), 0)) AS SALES_CNT,");
		stb.append("    SUM(VALUE(CAST(T1.TOTAL_PRICE AS BIGINT), 0)) AS TOTAL_PRICE,");
		stb.append("    SUM(VALUE(CAST(T1.TOTAL_TAX AS BIGINT), 0)) AS TOTAL_TAX,");
		stb.append("    SUM(VALUE(CAST(T1.TOTAL_SALES_MONEY AS BIGINT), 0)) AS TOTAL_SALES_MONEY,");
		stb.append("    SUM(VALUE(CAST(T1.TOTAL_SALES_CNT AS BIGINT), 0)) AS TOTAL_SALES_CNT");
		stb.append(" from");
		stb.append(" TEST_T T1");
		stb.append(" LEFT OUTER JOIN NAME_MST T2 ON");
		stb.append("    T1.COMMUTING_DIV = T2.NAMECD2 AND");
		stb.append("    T2.NAMECD1 = 'W009'");
		stb.append(" INNER JOIN COMMODITY_MST T3 ON");
		stb.append("    T1.COMMODITY_CD = T3.COMMODITY_CD");
		stb.append(" INNER JOIN COMMODITY_GROUP_DAT T4 ON");
		stb.append("    T3.COMMODITY_CD = T4.COMMODITY_CD");
		stb.append(" INNER JOIN COMMODITY_GROUP_MST T5 ON");
		stb.append("    T4.GROUP_CD = T5.GROUP_CD");
		stb.append(" group by");
		stb.append("    T1.COMMUTING_DIV, T2.NAME1, T5.SHOWORDER, T5.GROUP_CD, T5.GROUP_NAME");
		stb.append(" order by");
		stb.append("    T1.COMMUTING_DIV, T2.NAME1, T5.SHOWORDER, T5.GROUP_CD, T5.GROUP_NAME");
        
        log.debug("帳票出力対象データ抽出 の SQL=" + stb.toString());
		return stb.toString();
    }

    /** 売上データクラス */
    private class UriKinData {
        final String _student_div;
        final String _name;
        final String _group_cd;
        final String _showorder;
        final String _group_name;
        final String _sales_price;
        final String _sales_tax;
        final String _sales_money;
        final String _sales_cnt;
        final String _total_price;
        final String _total_tax;
        final String _total_sales_money;
        final String _total_sales_cnt;
		
        UriKinData(
                final String student_div,
                final String name,
                final String group_cd,
                final String showorder,
                final String group_name,
                final String sales_price,
                final String sales_tax,
                final String sales_money,
                final String sales_cnt,
                final String total_price,
                final String total_tax,
                final String total_sales_money,
                final String total_sales_cnt
        ) {
        	_student_div = student_div;
        	_name = name;
        	_group_cd = group_cd;
        	_showorder = showorder;
        	_group_name = group_name;
        	_sales_price = sales_price;
        	_sales_tax = sales_tax;
        	_sales_money = sales_money;
        	_sales_cnt = sales_cnt;
        	_total_price = total_price;
        	_total_tax = total_tax;
        	_total_sales_money = total_sales_money;
        	_total_sales_cnt = total_sales_cnt;
        }
    }
    
    /**
     * 売上合計データ取得
     * @param db2			ＤＢ接続オブジェクト
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createUriageSummaryData(final DB2UDB db2)	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getUriageSummaryDataSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final UriKinSummaryData urikin = new UriKinSummaryData(
                		rs.getString("SHOWORDER"),
                		rs.getString("GROUP_CD"),
                		rs.getString("GROUP_NAME"),
                		rs.getString("SALES_PRICE"),
                		rs.getString("SALES_TAX"),
                		rs.getString("SALES_MONEY"),
                		rs.getString("SALES_CNT"),
                		rs.getString("TOTAL_PRICE"),
                		rs.getString("TOTAL_TAX"),
                		rs.getString("TOTAL_SALES_MONEY"),
                		rs.getString("TOTAL_SALES_CNT")
                );
                rtnList.add(urikin);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /**
     * 帳票出力対象売上合計データ抽出ＳＱＬ生成処理
     * @return				SQL文字列
     * @throws Exception
     */
    private String getUriageSummaryDataSql(){

    	StringBuffer stb = new StringBuffer();

		stb.append(" WITH TEST_T AS ( ");
		stb.append("     select");
		stb.append("        W1.COMMODITY_CD,");
		stb.append("        SUM(VALUE(CAST(W1.SALES_PRICE AS BIGINT),0)) AS SALES_PRICE,");
		stb.append("        SUM(VALUE(CAST(W1.SALES_TAX AS BIGINT),0)) AS SALES_TAX,");
		stb.append("        SUM(VALUE(CAST(W1.SALES_MONEY AS BIGINT),0)) AS SALES_MONEY,");
		stb.append("        SUM(VALUE(CAST(W1.SALES_CNT AS BIGINT),0)) AS SALES_CNT,");
		stb.append("        SUM(VALUE(CAST(W1.TOTAL_PRICE AS BIGINT),0)) AS TOTAL_PRICE,");
		stb.append("        SUM(VALUE(CAST(W1.TOTAL_TAX AS BIGINT),0)) AS TOTAL_TAX,");
		stb.append("        SUM(VALUE(CAST(W1.TOTAL_SALES_MONEY AS BIGINT),0)) AS TOTAL_SALES_MONEY,");
		stb.append("        SUM(VALUE(CAST(W1.TOTAL_SALES_CNT AS BIGINT),0)) AS TOTAL_SALES_CNT");
		stb.append("     from");
		stb.append("        MONTH_SALES_DAT W1");
		stb.append("     where");
		stb.append("        W1.YEAR_MONTH = '" + _param._serch_yyyymm + "'");
		stb.append("     group by");
		stb.append("        W1.COMMODITY_CD");
		stb.append("     order by");
		stb.append("        W1.COMMODITY_CD");
		stb.append(" ) ");
		stb.append(" select");
		stb.append("    T4.SHOWORDER,");
		stb.append("    T4.GROUP_CD,");
		stb.append("    T4.GROUP_NAME,");
		stb.append("    SUM(VALUE(CAST(T1.SALES_PRICE AS BIGINT), 0)) AS SALES_PRICE,");
		stb.append("    SUM(VALUE(CAST(T1.SALES_TAX AS BIGINT), 0)) AS SALES_TAX,");
		stb.append("    SUM(VALUE(CAST(T1.SALES_MONEY AS BIGINT), 0)) AS SALES_MONEY,");
		stb.append("    SUM(VALUE(CAST(T1.SALES_CNT AS BIGINT), 0)) AS SALES_CNT,");
		stb.append("    SUM(VALUE(CAST(T1.TOTAL_PRICE AS BIGINT), 0)) AS TOTAL_PRICE,");
		stb.append("    SUM(VALUE(CAST(T1.TOTAL_TAX AS BIGINT), 0)) AS TOTAL_TAX,");
		stb.append("    SUM(VALUE(CAST(T1.TOTAL_SALES_MONEY AS BIGINT), 0)) AS TOTAL_SALES_MONEY,");
		stb.append("    SUM(VALUE(CAST(T1.TOTAL_SALES_CNT AS BIGINT), 0)) AS TOTAL_SALES_CNT");
		stb.append(" from");
		stb.append(" TEST_T T1");
		stb.append(" INNER JOIN COMMODITY_MST T2 ON");
		stb.append("    T1.COMMODITY_CD = T2.COMMODITY_CD");
		stb.append(" INNER JOIN COMMODITY_GROUP_DAT T3 ON");
		stb.append("    T2.COMMODITY_CD = T3.COMMODITY_CD");
		stb.append(" INNER JOIN COMMODITY_GROUP_MST T4 ON");
		stb.append("    T3.GROUP_CD = T4.GROUP_CD");
		stb.append(" group by");
		stb.append("    T4.SHOWORDER, T4.GROUP_CD, T4.GROUP_NAME");
		stb.append(" order by");
		stb.append("    T4.SHOWORDER, T4.GROUP_CD, T4.GROUP_NAME");
        
        log.debug("帳票出力対象データ抽出 の SQL=" + stb.toString());
		return stb.toString();
    }

    /** 売上合計データクラス */
    private class UriKinSummaryData {
        final String _showorder;
        final String _group_cd;
        final String _group_name;
        final String _sales_price;
        final String _sales_tax;
        final String _sales_money;
        final String _sales_cnt;
        final String _total_price;
        final String _total_tax;
        final String _total_sales_money;
        final String _total_sales_cnt;
		
        UriKinSummaryData(
                final String showorder,
                final String group_cd,
                final String group_name,
                final String sales_price,
                final String sales_tax,
                final String sales_money,
                final String sales_cnt,
                final String total_price,
                final String total_tax,
                final String total_sales_money,
                final String total_sales_cnt
        ) {
        	_showorder = showorder;
        	_group_cd = group_cd;
        	_group_name = group_name;
        	_sales_price = sales_price;
        	_sales_tax = sales_tax;
        	_sales_money = sales_money;
        	_sales_cnt = sales_cnt;
        	_total_price = total_price;
        	_total_tax = total_tax;
        	_total_sales_money = total_sales_money;
        	_total_sales_cnt = total_sales_cnt;
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
     * タイトルエリアの出力
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

    	// 入金データ各金額
    	int paymentCnt = 0;				// 小計：延べ人数
    	int paymentMoney = 0;			// 小計：入金額
    	int totalPaymentCnt = 0;		// 小計：累計延べ人数
    	int totalPaymentMoney = 0;		// 小計：累計入金額
    	int t_paymentCnt = 0;			// 合計：延べ人数
    	int t_paymentMoney = 0;			// 合計：入金額
    	int t_totalPaymentCnt = 0;		// 合計：累計延べ人数
    	int t_totalPaymentMoney = 0;	// 合計：累計入金額

    	// 売上データ各金額
    	int salesCnt = 0;				// 小計：延べ人数
    	int salesPrice = 0;				// 小計：売上額（税抜）
    	int salesTax = 0;				// 小計：売上額（消費税額）
    	int salesMoney = 0;				// 小計：売上額（税込）
    	int totalSalesCnt = 0;			// 小計：累計延べ人数
    	int totalSalesPrice = 0;		// 小計：累計売上額（税抜）
    	int totalSalesTax = 0;			// 小計：累計売上額（消費税額）
    	int totalSalesMoney = 0;		// 小計：累計売上額（税込）
    	int t_salesCnt = 0;				// 合計：延べ人数
    	int t_salesPrice = 0;			// 合計：売上額（税抜）
    	int t_salesTax = 0;				// 合計：売上額（消費税額）
    	int t_salesMoney = 0;			// 合計：売上額（税込）
    	int t_totalSalesCnt = 0;		// 合計：累計延べ人数
    	int t_totalSalesPrice = 0;		// 合計：累計売上額（税抜）
    	int t_totalSalesTax = 0;		// 合計：累計売上額（消費税額）
    	int t_totalSalesMoney = 0;		// 合計：累計売上額（税込）
    	
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

        /** 入金合計金額加算 */
        private void sumGokei() {
        	// 小計金額を合計金額へ加算
        	t_paymentCnt        += paymentCnt;
        	t_paymentMoney      += paymentMoney;
        	t_totalPaymentCnt   += totalPaymentCnt;
        	t_totalPaymentMoney += totalPaymentMoney;
        }
        
        /** 入金小計金額クリア */
        private void setSyokeiinit() {
        	paymentCnt        = 0;			
        	paymentMoney      = 0;		
        	totalPaymentCnt   = 0;	
        	totalPaymentMoney = 0;	
        }
        
        /** 売上小計金額クリア */
        private void setUriSyokeiinit() {
        	salesCnt        = 0;			
        	salesPrice      = 0;		
        	salesTax        = 0;		
        	salesMoney      = 0;		
        	totalSalesCnt   = 0;	
        	totalSalesPrice = 0;	
        	totalSalesTax   = 0;	
        	totalSalesMoney = 0;	
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
