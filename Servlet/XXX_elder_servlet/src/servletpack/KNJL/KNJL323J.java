package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
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
 * 合格者名簿
 * 
 * @author nakasone
 *
 */
public class KNJL323J {
    private static final String FORM_NAME = "KNJL323J.frm";
    private static final int PAGE_MAX_LINE = 40;
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL323J.class);
    
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
     * @return		対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        
        svf.VrSetForm(FORM_NAME, 1);

        // 入試区分が指定されている場合
    	if(!_param._testdiv.equals("0")){
    		//総ページ数取得・設定
    		String total_page = getTotalPage(db2, svf, _param._testdiv);
			// 合格者名簿データ取得
    		final List student = createStudents(db2, _param._testdiv);
    		// 帳票出力のメソッド
            outPutPrint(svf, student, total_page);
    		return;
		}
		
		// 入試区分取得
		final List testdivs = createTestDiv(db2);
		
        for (Iterator it = testdivs.iterator(); it.hasNext();) {
            final testdivs tstdiv = (testdivs) it.next();
            
    		//総ページ数取得・設定
    		String total_page = getTotalPage(db2, svf, tstdiv._testdiv);
        	// 合格者名簿データ取得
    		final List student = createStudents(db2, tstdiv._testdiv);
    		// 帳票出力のメソッド
            outPutPrint(svf, student, total_page);
        }
			
    }
    
    /**
     * 帳票出力処理
     * @param svf		帳票オブジェクト
     * @param student	帳票出力対象クラスオブジェクト
     */
    private void outPutPrint(final Vrw32alp svf, final List student, final String total_page) {

    	boolean retflg = false;	// 対象データ存在フラグ
		int gyo = 1;				//現在ページ数の判断用（行）
		int pagecnt = 1;			//現在ページ数
		String stestdiv = "0";		//現在ページ数の判断用（入試区分）
		int reccnt = 0;				//合計レコード数
		int reccnt_man 		= 0;	//男レコード数カウント用
		int reccnt_woman 	= 0;	//女レコード数カウント用

        
        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();

            if(gyo == 1){
    			//*================*
    			//* ヘッダ・フッタ *
    			//*================*
    			svf.VrsOut("NENDO" 		,_param._nendo);					// 対象年度
    			svf.VrsOut("DATE"	 	,_param._date);						// 作成日
    			svf.VrsOut("TESTDIV" 	,sudent._name1);					// 入試区分
    			svf.VrsOut("PAGE"		,String.valueOf(pagecnt));			// 現在ページ数
	        }
			//*================*
			//* 明細           *
			//*================*
            // №
			svf.VrsOutn("NO" 	, gyo, String.valueOf(reccnt+1));
			// 受験番号
			svf.VrsOutn("EXAMNO" 	, gyo, sudent._examno);
			// 氏名
			svf.VrsOutn(setformatArea("NAME", sudent._name, 10, "1", "2_1") , gyo, sudent._name);
			// 性別
			if(sudent._sex != null){
				if(sudent._sex.equals("1")){
					svf.VrsOutn("SEX" 	, gyo, "男");
				}
				if(sudent._sex.equals("2")){
					svf.VrsOutn("SEX" 	, gyo, "女");
				}
			}
			// 住所
			String saddress = sudent._address1 + sudent._address2;
			svf.VrsOutn(setformatArea("ADDRESS", saddress, 25, "1", "2_1") , gyo, saddress);

			// 塾名
			svf.VrsOutn(setformatArea("PRISCHOOL_NAME", sudent._prischool_name, 10, "1", "2_1") , gyo, sudent._prischool_name);
			// 備考
            String remark = sudent._remark1 + sudent._honordivName;
			svf.VrsOutn(setformatArea("REMARK", remark, 12, "1", "2_1") , gyo, remark);
			//レコード数カウント
			++reccnt;
			if(sudent._sex != null){
				if(sudent._sex.equals("1")){
					++reccnt_man;
				}
				if(sudent._sex.equals("2")){
					++reccnt_woman;
				}
			}
			//現在ページ数判断用
			gyo++;
			//４０行超えたとき、または、入試区分ブレイクの場合、ページ数カウント
			if ((gyo > PAGE_MAX_LINE) || (!stestdiv.equals(sudent._testdiv) && !stestdiv.equals("0"))) {
    			svf.VrsOut("TOTAL_PAGE" 	,total_page);
				svf.VrEndPage();
				gyo = 1;
				pagecnt++;
			}
			stestdiv = sudent._testdiv;
            _hasData = true;
            retflg = true;
        }

		//最終レコードを出力
		if (retflg) {
			//最終ページに男女合計を出力
			svf.VrsOut("TOTAL_PAGE" 	,total_page);
			svf.VrsOut("NOTE"	,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
            svf.VrEndPage();
		}
    }
    
    /**
     * @param db2			ＤＢ接続オブジェクト
     * @param test_div		入試区分
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudents(
    	final DB2UDB db2,
    	final String test_div)
    	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getStudentSql(test_div);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final student student = new student(rs.getString("EXAMNO"),
                		nvlT(rs.getString("TESTDIV")),
                		nvlT(rs.getString("NAME")),
                		nvlT(rs.getString("SEX")),
                		nvlT(rs.getString("REMARK1")),
                        nvlT(rs.getString("HONORDIV_NAME")),
                		nvlT(rs.getString("ADDRESS1")),
                		nvlT(rs.getString("ADDRESS2")),
                		nvlT(rs.getString("PRISCHOOL_NAME")),
                		nvlT(rs.getString("NAME1"))
                );
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /**総ページ数を取得**/
	private String getTotalPage(
    	final DB2UDB db2,
    	final Vrw32alp svf,
		String test_div)
    	throws SQLException {
		
		String total_page = "";
		
		// 総ページ数を取得
        final String sql = getTotalPageSql(test_div);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
            	total_page = rs.getString("TOTAL_PAGE");
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return total_page;

	}

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @param test_div		入試区分
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql(final String test_div){
		StringBuffer stb = new StringBuffer();
		stb.append(" select");
		stb.append("    W1.EXAMNO,");
		stb.append("    W1.TESTDIV,");
		stb.append("    W2.NAME,");
		stb.append("    W2.SEX,");
		stb.append("    W2.REMARK1,");
        stb.append("    case when W1.HONORDIV = '1' and W2.REMARK1 is null     then '特待合格'");
        stb.append("         when W1.HONORDIV = '1' and W2.REMARK1 is not null then '　特待合格' end as HONORDIV_NAME,");
		stb.append("    W3.ADDRESS1,");
		stb.append("    W3.ADDRESS2,");
		stb.append("    T1.PRISCHOOL_NAME,");
		stb.append("    T2.NAME1");
		stb.append(" from");
		stb.append("    ENTEXAM_RECEPT_DAT W1");
		stb.append("    INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ON");
		stb.append("        W1.ENTEXAMYEAR = W2.ENTEXAMYEAR and");				// 年度
		stb.append("        W1.EXAMNO = W2.EXAMNO ");							// 受験番号
		stb.append("    LEFT OUTER JOIN ENTEXAM_APPLICANTADDR_DAT W3 ON");
		stb.append("        W1.ENTEXAMYEAR = W3.ENTEXAMYEAR and");				// 年度
		stb.append("        W1.EXAMNO = W3.EXAMNO ");							// 受験番号
		stb.append("    LEFT OUTER JOIN PRISCHOOL_MST T1 ON");
		stb.append("        W2.PRISCHOOLCD = T1.PRISCHOOLCD");					// 塾コード
		stb.append("    LEFT OUTER JOIN NAME_MST T2 ON ");
		stb.append("         T2.NAMECD1 = 'L004' and");							// 名称区分
		stb.append("         T2.NAMECD2 = W1.TESTDIV");							// 名称コード
		stb.append(" where");
		stb.append("    W1.ENTEXAMYEAR = '" + _param._year + "' ");				// 年度
		stb.append("    and W1.TESTDIV = '" + test_div + "' ");					// 入試区分
		stb.append("    and W1.JUDGEDIV = '1' ");								// 合否区分(1:合格)
		stb.append(" order by");
		if(_param._sort.equals("1")){
			stb.append("    W1.EXAMNO");
		} else {
			stb.append("    T1.PRISCHOOL_NAME");
		}
        log.debug("帳票出力対象データ抽出 の SQL=" + stb.toString());
		return stb.toString();
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @param test_div		入試区分
     * @return				SQL文字列
     * @throws Exception
     */
    private String getTotalPageSql(final String test_div){
        final String rtn;
        rtn = " select"
            + "    SUM(T1.TEST_CNT) TOTAL_PAGE"
            + " from"
			+ "    (SELECT CASE WHEN MOD(COUNT(*),40) > 0 THEN COUNT(*)/40 + 1 ELSE COUNT(*)/40 END TEST_CNT "
			+ "     FROM   ENTEXAM_RECEPT_DAT W1, ENTEXAM_APPLICANTBASE_DAT W2 "
            + "     where"
            + "        W1.ENTEXAMYEAR = '" + _param._year + "' "				// 年度
            + "        and W1.TESTDIV = '" + test_div + "' "					// 入試区分
            + "        and W1.JUDGEDIV = '1' "									// 合否区分(1:合格)
            + "        and W1.ENTEXAMYEAR=W2.ENTEXAMYEAR "						// 入試年度
            + "        and W1.EXAMNO=W2.EXAMNO "								// 受験番号
            + "     group by"
            + "        W1.TESTDIV ) T1 "
            ;
        log.debug("帳票出力対象データ抽出 の SQL=" + rtn);
        return rtn;
    }

    /** 生徒クラス */
    private class student {
        final String _examno;
        final String _testdiv;
        final String _name;
        final String _sex;
        final String _remark1;
        final String _honordivName;
        final String _address1;
        final String _address2;
        final String _prischool_name;
        final String _name1;

        student(
                final String examno,
                final String testdiv,
                final String name,
                final String sex,
                final String remark1,
                final String honordivName,
                final String address1,
                final String address2,
                final String prischool_name,
                final String name1
        ) {
        	_examno = examno;
        	_testdiv = testdiv;
        	_name = name;
        	_sex = sex;
        	_remark1 = remark1;
            _honordivName = honordivName;
        	_address1 = address1;
        	_address2 = address2;
        	_prischool_name = prischool_name;
        	_name1 = name1;
        }
    }
    
    /** 志願者受付データ入試区分取得処理 */
    private List createTestDiv(final DB2UDB db2) throws SQLException {

        ResultSet rs = null;
        final List rtnList = new ArrayList();
        
        try {
			String sql ="";
			sql = " select"
	            + "    DISTINCT TESTDIV"
	            + " from"
	            + "    ENTEXAM_RECEPT_DAT"
	            + " where"
	            + "    ENTEXAMYEAR = '" + _param._year + "' "
	            + " order by"
	            + "    TESTDIV "
	            ;
			db2.query(sql);
			rs = db2.getResultSet();
            while (rs.next()) {
                final testdivs testdivs = new testdivs(rs.getString("TESTDIV"));
                rtnList.add(testdivs);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    
    /** 入試区分クラス */
    private class testdivs {
        final String _testdiv;

        testdivs(final String testdiv) {
        	_testdiv = testdiv;
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
    	private final String _testdiv;
    	private final String _sort;
    	private final String _nendo;
    	private final String _date;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testdiv = request.getParameter("TESTDIV");
            _sort = request.getParameter("SORT");
            _nendo = KenjaProperties.gengou(Integer.parseInt(_year))+"年度";
            _date = KNJ_EditDate.h_format_JP(_loginDate);
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
    
}
