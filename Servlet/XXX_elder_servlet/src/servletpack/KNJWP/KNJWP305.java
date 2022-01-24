package servletpack.KNJWP;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
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
 * 生徒納付金徴収台帳
 * 
 * @author nakasone
 *
 */
public class KNJWP305 {
    
	private static final String FORM_FILE = "KNJWP305.frm";
    private static final int PAGE_MAX_LINE = 50;
    private static final String ZERO_KINGAKU = "0";
    private HashMap hMeisaiTitle = new HashMap();
    
    
    private boolean _hasData;
    private Form _form;
    private Vrw32alp _svf;
    private DB2UDB db2;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJWP305.class);
    
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
    	
    	// 表題の学校名を取得
		final List school = createSchool(db2);
		String titleSchoolName = getSchoolName(school);

    	// 明細タイトルの商品略称を取得
		final List titleName = createMeisaiTitle(db2);
		
		if(titleName.size() > 0){
			// ヘッダエリアの設定
			outPutPrintHead(db2, titleSchoolName, titleName);
			
	    	// 明細データを取得
			final List meisaiData = createMeisaiData(db2);
			
			// 明細エリアの設定
			retflg = outPutPrintMeisai(db2, titleSchoolName, titleName, meisaiData);
		}
    	// 最終ページ出力
    	if(retflg){
        	_form._svf.VrEndPage();
    	}
    	
		return retflg;
			
    }

    /**
     * ヘッダエリアの出力
     */
    private void outPutPrintHead(final DB2UDB db2, String titleSchoolName, List titleName)	throws Exception {

    	int line = 1;

    	_form._svf.VrsOut("TITLE"		, titleSchoolName);	// 学校名称
		_form._svf.VrsOut("LOGINDATE"	,_param._date);		// 作成日
    	
		// 明細タイトルを設定
        for (Iterator it = titleName.iterator(); it.hasNext();) {
            final meisaiTitle name = (meisaiTitle) it.next();
            // 明細タイトル設定
			_form._svf.VrsOutn("ITEM1"	, line,	name._groupName);
			// グループの表示順をハッシュテーブルに格納(金額を設定する際に使用)
			hMeisaiTitle.put(name._groupCd, Integer.toString(line));
			
			++line;
		}
		
    }
    
    /**
     * 明細エリアの出力
     */
    private boolean outPutPrintMeisai(final DB2UDB db2, String titleSchoolName, List titleName,
    		List meisaiData)	throws Exception {
    	
    	boolean refflg = false;
    	boolean syohinflg = false;
    	String breakKey = null;			// 志願者番号＋年度
    	String KeyApplicantNo = null;	// 志願者番号
    	int line = 0;
    	int page_cnt = 1;
    	int zenKurikosi = 0;		// 前年度繰越金額
    	int nyukinMoney = 0;		// 入金額
    	
        for (Iterator it = meisaiData.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();

            // 志願者番号+年度のブレイク
            if(breakKey == null  || !breakKey.equals(sudent._applicantno + sudent._year)){
            	if(breakKey != null){
                    // 商品が存在する場合
            		if(syohinflg){
                		// 納付金額の設定
                		_form._svf.VrsOutn("TOTAL_CLAIM_MONEY"	, line,	String.valueOf(_param.totalMoney));
                    }
        			// 前年度繰越金額の設定
            		zenKurikosi = (zenKurikosi + nyukinMoney) - _param.totalMoney;
               		_form._svf.VrsOutn("CARRY_OVER_MONEY"	, line,	String.valueOf(zenKurikosi));
               		
            		// 納付金額をクリア
            		_param.totalMoneyinit();
            		syohinflg = false;
            	}
        		
                if( PAGE_MAX_LINE == line ){
                	++page_cnt;
                	_form._svf.VrEndPage();
            		line = 0;
                }
                if(line == 0){
        			// ヘッダエリアの設定
        			outPutPrintHead(db2, titleSchoolName, titleName);
                }
                ++line;
            	// 生徒別年度別の出力
            	outPutPrintStudent(db2, sudent, line);

            	breakKey = sudent._applicantno + sudent._year;
            }
            
    		// 志願者番号のブレイク
    		if(KeyApplicantNo == null || !KeyApplicantNo.equals(sudent._applicantno)){
            	if(KeyApplicantNo != null){
               		zenKurikosi = 0;
            	}
            	KeyApplicantNo = sudent._applicantno;
    		}
    		
            // 商品金額の出力
        	outPutPrintItem(sudent, line);

        	if(!sudent._nyuMoney.equals("")){
            	nyukinMoney = Integer.parseInt(sudent._nyuMoney);
        	} else {
        		nyukinMoney = 0;
        	}

        	if(!sudent._groupCd.equals("")){
        		syohinflg = true;
        	}
        	
        }
        
		if (breakKey != null) {
            // 商品が存在する場合
    		if(syohinflg){
        		// 納付金額の設定
        		_form._svf.VrsOutn("TOTAL_CLAIM_MONEY"	, line,	String.valueOf(_param.totalMoney));
            }
			// 前年度繰越金額の設定
       		_form._svf.VrsOutn("CARRY_OVER_MONEY"	, line,	String.valueOf((zenKurikosi + nyukinMoney) - _param.totalMoney));
    		refflg = true;
		}
		
		return 	refflg;
		
	}

    /**
     * 生徒情報の設定
     * @param db2
     * @param sudent
     * @param line
     * @throws SQLException
     */
    private void outPutPrintStudent(final DB2UDB db2, student sudent, int line)	throws Exception {
    	
        // 年度
		_form._svf.VrsOutn("NENDO"	, line,	sudent._year);
        // 入金日は対象生徒の最終入金年月を設定
        _form._svf.VrsOutn("PAYMENT_DATE"	, line,
        		fomatDate(getNyukinDate(db2, sudent._applicantno, sudent._year), "yyyyMM", "yyyy'年'M'月'"));
    	// 志願者番号
    	_form._svf.VrsOutn("APPLICANTNO"	, line,	sudent._applicantno);
        // 入金額
		_form._svf.VrsOutn("PAYMENT_MONEY"	, line,	sudent._nyuMoney);
    	
    	// 生徒データ取得
		final List studentInfo = createStudentInfoData(db2, sudent._applicantno, sudent._year);
    	
		for (Iterator it = studentInfo.iterator(); it.hasNext();) {
            final studentInfo sudentInfo = (studentInfo) it.next();
            
            if(sudentInfo._schregno.equals("")){
                // 学籍番号が志願者基礎マスタに設定されていない場合
                // 氏名
    			_form._svf.VrsOutn("NAME"	, line,	sudentInfo._applicant_name);
    			// 学生区分
    			_form._svf.VrsOutn("STUDENT_DIV"	, line,	sudentInfo._applicant_student_name);
    			// 入学日
                _form._svf.VrsOutn("ENT_DATE"	, line,	fomatDate(sudentInfo._ent_schedule_date, "yyyy-MM-dd", "yyyy'年'M'月'd'日'"));
    			// 所属
    			_form._svf.VrsOutn("BELONGING"	, line,	sudentInfo._applicant_schoolname1);
            } else {
                // 学籍番号が志願者基礎マスタに設定されている場合
            	// 学籍番号
    			_form._svf.VrsOutn("SCHREGNO"	, line,	sudentInfo._schregno);
                // 氏名
    			_form._svf.VrsOutn("NAME"	, line,	sudentInfo._schreg_name);
    			// 入学日
                _form._svf.VrsOutn("ENT_DATE"	, line,	fomatDate(sudentInfo._ent_date, "yyyy-MM-dd", "yyyy'年'M'月'd'日'"));
                
                if(!sudentInfo._schreg_schregno.equals("")){
        			// 学生区分
        			_form._svf.VrsOutn("STUDENT_DIV"	, line,	sudentInfo._schreg_student_name);
        			// 所属
        			_form._svf.VrsOutn("BELONGING"	, line,	sudentInfo._schreg_schoolname1);
                } else if(!sudentInfo._schreg_schregno_yokunen.equals("")){
        			// 学生区分
        			_form._svf.VrsOutn("STUDENT_DIV"	, line,	sudentInfo._schreg_student_name_yokunen);
        			// 所属
        			_form._svf.VrsOutn("BELONGING"	, line,	sudentInfo._schreg_schoolname1_yokunen);
                } else {
        			// 学生区分
        			_form._svf.VrsOutn("STUDENT_DIV"	, line,	sudentInfo._applicant_student_name);
        			// 所属
        			_form._svf.VrsOutn("BELONGING"	, line,	sudentInfo._applicant_schoolname1);
                }
            }
        }
		
    }
    
    /** 商品金額の出力
     * @param db2
     * @param sudent
     * @param line
     * @throws Exception
     */
    private void outPutPrintItem(student sudent, int line)	throws Exception {
    	
		// 売上額
		_form._svf.VrsOutn("ITEM_MONEY"+hMeisaiTitle.get(sudent._groupCd)	, line,	sudent._uriMoney);
		// 免除金
		_form._svf.VrsOutn("EXEMPTION_MONEY"	, line,	ZERO_KINGAKU);
		// 年度毎の納付金額算出の為、売上額を加算
		if(!sudent._uriMoney.equals("") && !sudent._groupCd.equals("")){
			_param.totalMoney += Integer.parseInt(sudent._uriMoney);
		}
    }
    
    /**
     * 学校名称取得処理
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
     * 学校名称取得処理
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
    
    /**
     * 明細項目名称(商品グループ名称)取得処理
     * @param db2			ＤＢ接続オブジェクト
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createMeisaiTitle(final DB2UDB db2)	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getMeisaiTitleSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final meisaiTitle titleName = new meisaiTitle(
                   	nvlT(rs.getString("SHOWORDER")),
                	nvlT(rs.getString("GROUP_CD")),
                	nvlT(rs.getString("GROUP_NAME"))
                	);
                rtnList.add(titleName);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    
    /**
     * 明細項目名称(商品略称)抽出ＳＱＬ生成処理
     * @return				SQL文字列
     * @throws Exception
     */
    private String getMeisaiTitleSql(){

    	StringBuffer stb = new StringBuffer();
		
		stb.append(" select");
		stb.append("     W2.SHOWORDER,");
		stb.append("     W2.GROUP_CD,");
		stb.append("     W2.GROUP_NAME");
		stb.append(" from");
		stb.append(" COMMODITY_GROUP_DAT W1");
		stb.append(" inner join COMMODITY_GROUP_MST W2 on");
		stb.append("     W1.GROUP_CD = W2.GROUP_CD");
		stb.append(" group by");
		stb.append("     W2.SHOWORDER,W2.GROUP_CD,W2.GROUP_NAME");
		stb.append(" order by");
		stb.append("     W2.SHOWORDER,W2.GROUP_CD,W2.GROUP_NAME");

		return stb.toString();
    }

    /** 明細項目名称(商品略称) */
    private class meisaiTitle {
        final String _showorder;
        final String _groupCd;
        final String _groupName;
        
        meisaiTitle(
                final String showorder,
                final String groupCd,
                final String groupName
        ) {
        	_showorder = showorder;
        	_groupCd = groupCd;
        	_groupName = groupName;
        }
    }

    /**
     * 生徒年度別集計データ取得処理
     * @param db2			ＤＢ接続オブジェクト
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createMeisaiData(final DB2UDB db2)	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getNyukinDataSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final student student = new student(
					nvlT(rs.getString("SRT_APPLICANTNO")),
					nvlT(rs.getString("SRT_YEAR")),
					nvlT(rs.getString("NYU_MONEY")),
					nvlT(rs.getString("URI_COMMODITY_CD")),
					nvlT(rs.getString("URI_MONEY")),
					nvlT(rs.getString("GROUP_CD")),
					nvlT(rs.getString("SHOWORDER"))
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
     * 生徒年度別集計データ抽出ＳＱＬ生成処理
     * @return				SQL文字列
     * @throws Exception
     */
    private String getNyukinDataSql(){

    	StringBuffer stb = new StringBuffer();
		
		stb.append(" WITH NYU_MAIN_T AS (");
		stb.append(" select");
		stb.append(" W1.APPLICANTNO    AS APPLICANTNO, ");
		stb.append(" W1.YEAR_MONTH     AS YEAR_MONTH, ");
		stb.append(" W1.PAYMENT_MONEY  AS MONEY, ");
		stb.append(" CASE WHEN SUBSTR(W1.YEAR_MONTH,5,2) >= '01' AND SUBSTR(W1.YEAR_MONTH,5,2) <= '03'");
		stb.append(" THEN INT(SUBSTR(W1.YEAR_MONTH,1,4)) - 1 ELSE INT(SUBSTR(W1.YEAR_MONTH,1,4)) END  AS YEAR");
		stb.append(" from");
		stb.append(" MONTH_SCH_PAYMENT_DAT W1");
		stb.append(" )");
		stb.append(" , NYU_APPLICANTNO_LIST AS (");
		stb.append("     select");
		stb.append("       YEAR        AS NYU_YEAR,");
		stb.append("       APPLICANTNO AS NYU_APPLICANTNO,");
		stb.append("       SUM(MONEY)  AS NYU_MONEY");
		stb.append("     from NYU_MAIN_T");
		stb.append("     group by YEAR, APPLICANTNO");
		stb.append("     order by YEAR, APPLICANTNO");
		stb.append(" )");
		stb.append(" ,URI_MAIN_T AS (");
		stb.append(" select");
		stb.append(" W2.APPLICANTNO    AS APPLICANTNO, ");
		stb.append(" W2.YEAR_MONTH     AS YEAR_MONTH, ");
		stb.append(" W2.COMMODITY_CD   AS COMMODITY_CD, ");
		stb.append(" W2.SALES_MONEY    AS MONEY, ");
		stb.append(" CASE WHEN SUBSTR(W2.YEAR_MONTH,5,2) >= '01' AND SUBSTR(W2.YEAR_MONTH,5,2) <= '03'");
		stb.append(" THEN INT(SUBSTR(W2.YEAR_MONTH,1,4)) - 1 ELSE INT(SUBSTR(W2.YEAR_MONTH,1,4)) END  AS YEAR");
		stb.append(" from");
		stb.append(" MONTH_SCH_SALES_DAT W2");
		stb.append(" )");
		stb.append(" , URI_APPLICANTNO_LIST AS (");
		stb.append("     select");
		stb.append("       YEAR         AS URI_YEAR,");
		stb.append("       APPLICANTNO  AS URI_APPLICANTNO,");
		stb.append("       COMMODITY_CD AS URI_COMMODITY_CD,");
		stb.append("       SUM(MONEY)   AS URI_MONEY");
		stb.append("     from URI_MAIN_T");
		stb.append("     group by YEAR, APPLICANTNO, COMMODITY_CD");
		stb.append("     order by YEAR, APPLICANTNO, COMMODITY_CD");
		stb.append(" )");
		stb.append(" , MAIN_LIST AS (");
		stb.append(" select");
		stb.append("   T2.NYU_APPLICANTNO,");
		stb.append("   T2.NYU_YEAR,");
		stb.append("   T2.NYU_MONEY,");
		stb.append("   T3.URI_APPLICANTNO,");
		stb.append("   T3.URI_YEAR,");
		stb.append("   T3.URI_COMMODITY_CD,");
		stb.append("   T3.URI_MONEY,");
		stb.append("   CASE WHEN T2.NYU_YEAR IS NOT NULL AND T3.URI_YEAR IS NULL THEN T2.NYU_YEAR");
		stb.append("        WHEN T3.URI_YEAR IS NOT NULL AND T2.NYU_YEAR IS NULL THEN T3.URI_YEAR");
		stb.append("        ELSE T2.NYU_YEAR END AS SRT_YEAR");
		stb.append(" from NYU_APPLICANTNO_LIST T2");
		stb.append(" FULL JOIN URI_APPLICANTNO_LIST T3 on");
		stb.append("   T2.NYU_APPLICANTNO = T3.URI_APPLICANTNO AND");
		stb.append("   T2.NYU_YEAR = T3.URI_YEAR");
		stb.append(" order by");
		stb.append("   T2.NYU_APPLICANTNO, 8");
		stb.append(" )");
		stb.append(" select");
		stb.append("   CASE WHEN W1.NYU_APPLICANTNO IS NOT NULL THEN W1.NYU_APPLICANTNO");
		stb.append("        WHEN W1.URI_APPLICANTNO IS NOT NULL THEN W1.URI_APPLICANTNO");
		stb.append("        ELSE W1.NYU_APPLICANTNO END AS SRT_APPLICANTNO,");
		stb.append(" ");
		stb.append("   CASE WHEN W1.NYU_YEAR IS NOT NULL AND W1.URI_YEAR IS NULL THEN W1.NYU_YEAR");
		stb.append("        WHEN W1.URI_YEAR IS NOT NULL AND W1.NYU_YEAR IS NULL THEN W1.URI_YEAR");
		stb.append("        ELSE W1.NYU_YEAR END AS SRT_YEAR,");
		stb.append("   W1.NYU_MONEY,");
		stb.append("   W1.URI_COMMODITY_CD,");
		stb.append("   W1.URI_MONEY,");
		stb.append("   W3.GROUP_CD,");
		stb.append("   W3.SHOWORDER");
		stb.append(" from MAIN_LIST W1");
		stb.append(" left join COMMODITY_GROUP_DAT W2 on");
		stb.append("     W1.URI_COMMODITY_CD = W2.COMMODITY_CD");
		stb.append(" left join COMMODITY_GROUP_MST W3 on");
		stb.append("     W2.GROUP_CD = W3.GROUP_CD");
		stb.append(" order by 1,2");

		return stb.toString();
    }

    /** 生徒年度別集計データクラス */
    private class student {
        final String _applicantno;
        final String _year;
        final String _nyuMoney;
        final String _uriCommodityCd;
        final String _uriMoney;
        final String _groupCd;
        final String _showorder;
        
        student(
                final String applicantno,
                final String year,
                final String nyuMoney,
                final String uriCommodityCd,
                final String uriMoney,
                final String groupCd,
                final String showorder
        ) {
        	_applicantno = applicantno;
        	_year = year;
        	_nyuMoney = nyuMoney;
        	_uriCommodityCd = uriCommodityCd;
        	_uriMoney = uriMoney;
        	_groupCd = groupCd;
        	_showorder = showorder;
        }
    }
    
    /**
     * 生徒年度別集計売上データ取得処理
     * @param db2			ＤＢ接続オブジェクト
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudentInfoData(final DB2UDB db2, String keyApplicantNo, String keyYear)	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getStudentInfoSql(keyApplicantNo, keyYear);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final studentInfo studentInfo = new studentInfo(
					nvlT(rs.getString("SCHREGNO")),
					nvlT(rs.getString("APPLICANT_NAME")),
					nvlT(rs.getString("ENT_SCHEDULE_DATE")),
					nvlT(rs.getString("SCHREG_NAME")),
					nvlT(rs.getString("ENT_DATE")),
					nvlT(rs.getString("SCHREG_SCHREGNO")),
					nvlT(rs.getString("SCHREG_SCHOOLNAME1")),
					nvlT(rs.getString("SCHREG_STUDENT_NAME")),
					nvlT(rs.getString("APPLICANT_SCHOOLNAME1")),
					nvlT(rs.getString("APPLICANT_STUDENT_NAME")),
					nvlT(rs.getString("SCHREG_SCHREGNO_YOKUNEN")),
					nvlT(rs.getString("SCHREG_SCHOOLNAME1_YOKUNEN")),
					nvlT(rs.getString("SCHREG_STUDENT_NAME_YOKUNEN"))
				);
                rtnList.add(studentInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    
    /**
     * 生徒年度別集計売上データ抽出ＳＱＬ生成処理
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentInfoSql(String keyApplicantNo, String keyYear){

    	StringBuffer stb = new StringBuffer();
    	int iYokunen_Year = 0;
    	String sYokunen_Year = "";
    	
    	// 翌年を算出
    	iYokunen_Year = Integer.parseInt(keyYear) + 1;
    	sYokunen_Year = String.valueOf(iYokunen_Year);
		
		stb.append(" select");
		stb.append("     W1.SCHREGNO,");
		stb.append("     W1.NAME AS APPLICANT_NAME,");
		stb.append("     W1.ENT_SCHEDULE_DATE,");
		stb.append("     W2.NAME AS SCHREG_NAME,");
		stb.append("     W2.ENT_DATE,");
		stb.append("     W3.SCHREGNO AS SCHREG_SCHREGNO,");
		stb.append("     W4.SCHOOLNAME1 AS SCHREG_SCHOOLNAME1,");
		stb.append("     W5.NAME AS SCHREG_STUDENT_NAME,");
		stb.append("     W6.SCHOOLNAME1 AS APPLICANT_SCHOOLNAME1,");
		stb.append("     W7.NAME AS APPLICANT_STUDENT_NAME,");
		stb.append("     W8.SCHREGNO AS SCHREG_SCHREGNO_YOKUNEN,");
		stb.append("     W9.SCHOOLNAME1 AS SCHREG_SCHOOLNAME1_YOKUNEN,");
		stb.append("     W10.NAME AS SCHREG_STUDENT_NAME_YOKUNEN");
		stb.append(" from");
		stb.append(" APPLICANT_BASE_MST W1");
		stb.append(" left join SCHREG_BASE_MST W2 on");
		stb.append("     W1.SCHREGNO  = W2.SCHREGNO");
		stb.append(" left join SCHREG_REGD_DAT W3 on");
		stb.append("     W1.SCHREGNO  = W3.SCHREGNO and");
		stb.append("     W3.YEAR  = '" + keyYear + "'");
		stb.append(" left join BELONGING_MST W4 on");
		stb.append("     W3.GRADE  = W4.BELONGING_DIV");
		stb.append(" left join STUDENTDIV_MST W5 on");
		stb.append("     W3.STUDENT_DIV = W5.STUDENT_DIV");
		stb.append(" left join BELONGING_MST W6 on");
		stb.append("     W1.BELONGING_DIV  = W6.BELONGING_DIV");
		stb.append(" left join STUDENTDIV_MST W7 on");
		stb.append("     W1.STUDENT_DIV = W7.STUDENT_DIV");
		stb.append(" left join SCHREG_REGD_DAT W8 on");
		stb.append("     W1.SCHREGNO  = W8.SCHREGNO and");
		stb.append("     W8.YEAR  = '" + sYokunen_Year + "'");
		stb.append(" left join BELONGING_MST W9 on");
		stb.append("     W8.GRADE  = W9.BELONGING_DIV");
		stb.append(" left join STUDENTDIV_MST W10 on");
		stb.append("     W8.STUDENT_DIV = W10.STUDENT_DIV");
		stb.append(" where");
		stb.append("     W1.APPLICANTNO = '" + keyApplicantNo + "'");

		return stb.toString();
    }

    /** 生徒年度別集計売上データクラス */
    private class studentInfo {
        final String _schregno;
        final String _applicant_name;
        final String _ent_schedule_date;
        final String _schreg_name;
        final String _ent_date;
        final String _schreg_schregno;
        final String _schreg_schoolname1;
        final String _schreg_student_name;
        final String _applicant_schoolname1;
        final String _applicant_student_name;
        final String _schreg_schregno_yokunen;
        final String _schreg_schoolname1_yokunen;
        final String _schreg_student_name_yokunen;
        
        studentInfo(
                final String schregno,
                final String applicant_name,
                final String ent_schedule_date,
                final String schreg_name,
                final String ent_date,
                final String schreg_schregno,
                final String schreg_schoolname1,
                final String schreg_student_name,
                final String applicant_schoolname1,
                final String applicant_student_name,
                final String schreg_schregno_yokunen,
                final String schreg_schoolname1_yokunen,
                final String schreg_student_name_yokunen
        ) {
        	_schregno = schregno;
        	_applicant_name = applicant_name;
        	_ent_schedule_date = ent_schedule_date;
        	_schreg_name = schreg_name;
        	_ent_date = ent_date;
        	_schreg_schregno = schreg_schregno;
        	_schreg_schoolname1 = schreg_schoolname1;
        	_schreg_student_name = schreg_student_name;
        	_applicant_schoolname1 = applicant_schoolname1;
        	_applicant_student_name = applicant_student_name;
        	_schreg_schregno_yokunen = schreg_schregno_yokunen;
        	_schreg_schoolname1_yokunen = schreg_schoolname1_yokunen;
        	_schreg_student_name_yokunen = schreg_student_name_yokunen;
        }
    }
    
    /**
     * 最終入金年月取得処理
     * @param db2
     * @param keyApplicantNo	志願者番号
     * @param keyYear			年月
     * @return
     * @throws SQLException
     */
    private String getNyukinDate(DB2UDB db2, String keyApplicantNo, String keyYear)
    	throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String nyuKinDate = "";
        
		try{
	        ps = db2.prepareStatement(sqlNyukinDate(keyApplicantNo, keyYear));
	        rs = ps.executeQuery();
	        while (rs.next()) {
	            nyuKinDate = nvlT(rs.getString("YEAR_MONTH"));
	        }
		} catch( Exception ex ){
			log.error("getNyukinDate error!",ex);
    	} finally {
    		db2.commit();
    		DbUtils.closeQuietly(null, ps, rs);
    	}
        return nyuKinDate;
    }

    /**
     * 最終入金年月取得ＳＱＬ生成処理
     * @param keyApplicantNo	志願者番号
     * @param keyYear			年月
     * @return
     */
    private String sqlNyukinDate(String keyApplicantNo, String keyYear) {
    	StringBuffer stb = new StringBuffer();
    	
    	int iKeyYear = Integer.valueOf(keyYear).intValue();
    	
		stb.append(" WITH MAIN_T AS (");
		stb.append(" select");
		stb.append(" APPLICANTNO, ");
		stb.append(" YEAR_MONTH, ");
		stb.append(" CASE WHEN SUBSTR(YEAR_MONTH,5,2) >= '01' AND SUBSTR(YEAR_MONTH,5,2) <= '03'");
		stb.append(" THEN INT(SUBSTR(YEAR_MONTH,1,4)) - 1 ELSE INT(SUBSTR(YEAR_MONTH,1,4)) END  AS YEAR");
		stb.append(" from");
		stb.append(" MONTH_SCH_PAYMENT_DAT");
		stb.append(" where");
		stb.append(" APPLICANTNO = '" + keyApplicantNo + "'");
		stb.append(" )");
		stb.append(" select");
		stb.append("   T1.YEAR_MONTH");
		stb.append(" from MAIN_T T1");
		stb.append(" where");
		stb.append(" T1.YEAR = " + iKeyYear + "");
		stb.append(" order by ");
		stb.append(" T1.YEAR_MONTH  DESC FETCH FIRST 1 ROWS ONLY");

		return stb.toString();
    }
    
    /** パラメータクラス */
    private class Param {
    	private final String _programid;
    	private final String _year;
    	private final String _semester;
    	private final String _loginDate;
    	private final String _date;
    	
    	int totalMoney = 0;
    	String sYearMonth = "";
    	
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year      = request.getParameter("YEAR");
            _semester  = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _date = KNJ_EditDate.h_format_JP(_loginDate);
        }
        
        /** 納付金額算出用変数のクリア */
        private void totalMoneyinit() {
        	totalMoney        = 0;			
        }
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
    	
    	if(cnvDate.equals("")){
    		return retDate;
    	}
    	
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

            _svf.VrSetForm(FORM_FILE, 1);
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
