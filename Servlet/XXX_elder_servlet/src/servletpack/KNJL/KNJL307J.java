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
 * 入試出願状況レポート
 * 
 * @author nakasone
 *
 */
public class KNJL307J {
    private static final String TEST_DIV_2KA = "1";
    private static final String TEST_DIV_3KA = "3";
    private static final String TEST_DIV_4KA = "2";
    private static final String DIV_MARK1 = "○";
    private static final String DIV_MARK2 = "◎";
    private static final String DIV_MARK3 = "△";
    private static final int PAGE_MAX_LINE = 30;
    /** 住所 */
    private static final int ADDRESS_LENG = 25;
    
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL307J.class);
    
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
     * @return		対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        
    	boolean retflg = false;

        final String formName = (_param._infuruFlg) ? "KNJL307J_2.frm" : "KNJL307J.frm";
        svf.VrSetForm(formName, 4);

		//総ページ数取得・設定
		String total_page = getTotalPage(db2);
		// 入試出願状況レポート取得
		final List student = createStudents(db2);
		// 帳票出力のメソッド
		retflg = outPutPrint(svf, student, total_page);
		return retflg;
			
    }
    
    /**
     * 帳票出力処理
     * @param svf		帳票オブジェクト
     * @param student	帳票出力対象クラスオブジェクト
     * return           対象データ存在フラグ
     */
    private boolean outPutPrint(final Vrw32alp svf, final List student, final String total_page) {
		
    	boolean dataflg = false;	// 対象データ存在フラグ
    	int gyo = 1;				// 現在ページ数の判断用（行）
		int pagecnt = 1;			// 現在ページ数
		int reccnt = 0;				// 合計レコード数
		int reccnt_man 		= 0;	// 男レコード数カウント用
		int reccnt_woman 	= 0;	// 女レコード数カウント用
		boolean endflg = true;

        
        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();

			//レコードを出力
			if (reccnt > 0){
				svf.VrEndRecord();
			}
            
			//30行超えた場合、ページ数カウント
			if (gyo > PAGE_MAX_LINE) {
    			svf.VrsOut("TOTAL_PAGE" 	,total_page);
				gyo = 1;
				pagecnt++;
			}
			//*================*
			//* ヘッダ         *
			//*================*
			svf.VrsOut("NENDO" 		,_param._nendo);					// 対象年度
			svf.VrsOut("DATE"	 	,_param._date);						// 作成日
			svf.VrsOut("PAGE"		,String.valueOf(pagecnt));			// 現在ページ数
			svf.VrsOut("TOTAL_PAGE" 	,total_page);
			//*================*
			//* 明細           *
			//*================*
            // №
			svf.VrsOut("NO" 	, String.valueOf(reccnt+1));
			// 受験番号
			svf.VrsOut("EXAMNO" , sudent._examno);
			// 氏名
			svf.VrsOut(setformatArea("NAME", sudent._name, 12, "1", "2") , sudent._name);
			// 現住所
			String saddress = sudent._address1 + sudent._address2;
            if (saddress != null) {
                if (saddress.length() <= ADDRESS_LENG) {
                    svf.VrsOut("ADDRESS1", saddress);
                } else {
                    svf.VrsOut("ADDRESS2", saddress);
                }
            }
			//入試区分：A-1
			if(sudent._testdiv1.equals(TEST_DIV_2KA)){
				svf.VrsOut("MARK1" 	, DIV_MARK1);	// 2科
			}
			if(sudent._testdiv1.equals(TEST_DIV_4KA)){
				svf.VrsOut("MARK1" 	, DIV_MARK2);	// 4科
			}
			//入試区分：A-2
			if(sudent._testdiv2.equals(TEST_DIV_2KA)){
				svf.VrsOut("MARK2" 	, DIV_MARK1);	// 2科
			}
			//入試区分：B
			if(sudent._testdiv3.equals(TEST_DIV_2KA)){
				svf.VrsOut("MARK3" 	, DIV_MARK1);	// 2科
			}
			if(sudent._testdiv3.equals(TEST_DIV_4KA)){
				svf.VrsOut("MARK3" 	, DIV_MARK2);	// 4科
			}
			//入試区分：C
            if(sudent._testdiv4.equals(TEST_DIV_2KA)){
                svf.VrsOut("MARK4"  , DIV_MARK1);   // 2科
            }
			if(sudent._testdiv4.equals(TEST_DIV_4KA)){
				svf.VrsOut("MARK4" 	, DIV_MARK2);	// 4科
			}
			//入試区分：帰国生
			if(sudent._testdiv5.equals(TEST_DIV_3KA)){
				svf.VrsOut("MARK5" 	, DIV_MARK3);	// 3科
			}
            //入試区分：D
            if(sudent._testdiv6.equals(TEST_DIV_2KA)){
                svf.VrsOut("MARK6"  , DIV_MARK1);   // 2科
            }
            if(sudent._testdiv6.equals(TEST_DIV_4KA)){
                svf.VrsOut("MARK6"  , DIV_MARK2);   // 4科
            }
			
			//レコード数カウント
			++reccnt;
			// 合計カウント
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
			dataflg = true;            
        }

		if (dataflg) {
			// 最終行に達していない場合
			if (gyo < PAGE_MAX_LINE) {
	    		while(endflg){
	        		if(gyo > PAGE_MAX_LINE){
    					//最終ページに男女合計を出力
    					svf.VrsOut("NOTE"	,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
    					svf.VrEndRecord();	//レコードを出力
    					setSvfInt(svf);		//ブランクセット
	    	            endflg = false;
	    			} else {
	    				svf.VrEndRecord();
	        			++gyo;
	    			}
	            }
	    	} else {
				//最終ページに男女合計を出力
				svf.VrsOut("NOTE"	,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
				svf.VrEndRecord();	//レコードを出力
				setSvfInt(svf);		//ブランクセット
	            endflg = false;
	    	}
			
		}
		
		return dataflg;
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
                final student student = new student(rs.getString("EXAMNO"),
                		nvlT(rs.getString("NAME")),
                		nvlT(rs.getString("TESTDIV1")),
                		nvlT(rs.getString("TESTDIV2")),
                		nvlT(rs.getString("TESTDIV3")),
                		nvlT(rs.getString("TESTDIV4")),
                		nvlT(rs.getString("TESTDIV5")),
                        nvlT(rs.getString("TESTDIV6")),
                		nvlT(rs.getString("SEX")),
                		nvlT(rs.getString("ADDRESS1")),
                		nvlT(rs.getString("ADDRESS2"))
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
	private String getTotalPage(final DB2UDB db2) throws SQLException {
		
		String total_page = "";
		
		// 総ページ数を取得
        final String sql = getTotalPageSql();
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
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql(){
        final String rtn;
        rtn = " select"
            + "    W1.EXAMNO,"
            + "    W1.NAME,"
            + "    W1.TESTDIV1,"
            + "    W1.TESTDIV2,"
            + "    W1.TESTDIV3,"
            + "    W1.TESTDIV4,"
            + "    W1.TESTDIV5,"
            + "    W1.TESTDIV6,"
            + "    W1.SEX,"
            + "    W2.ADDRESS1,"
            + "    W2.ADDRESS2"
            + " from"
            + "    ENTEXAM_APPLICANTBASE_DAT W1"
            + "    LEFT OUTER JOIN ENTEXAM_APPLICANTADDR_DAT W2 ON"
            + "        W1.ENTEXAMYEAR = W2.ENTEXAMYEAR and"					// 年度
            + "        W1.EXAMNO = W2.EXAMNO "								// 受験番号
            + " where"
            + "    W1.ENTEXAMYEAR = '" + _param._year + "' "				// 年度
            + " order by"
            + "    W1.EXAMNO"
            ;
        log.debug("帳票出力対象データ抽出 の SQL=" + rtn);
        return rtn;
    }

    /**
     * 総ページ数取得ＳＱＬ生成処理
     * @return				SQL文字列
     * @throws Exception
     */
    private String getTotalPageSql(){
        final String rtn;
        rtn = " select"
        	+ "    SUM(T1.TEST_CNT) TOTAL_PAGE  "
        	+ " from "
        	+ "    (SELECT CASE WHEN MOD(COUNT(*),30) > 0 THEN COUNT(*)/30 + 1 ELSE COUNT(*)/30 END TEST_CNT  "
        	+ "     FROM   ENTEXAM_APPLICANTBASE_DAT "
        	+ "     WHERE  ENTEXAMYEAR='"+_param._year+"'  "
        	+ "    ) T1  "
        	;
        log.debug("総ページ数抽出 の SQL=" + rtn);
        return rtn;
    }

    /** 生徒クラス */
    private class student {
        final String _examno;
        final String _name;
        final String _testdiv1;
        final String _testdiv2;
        final String _testdiv3;
        final String _testdiv4;
        final String _testdiv5;
        final String _testdiv6;
        final String _sex;
        final String _address1;
        final String _address2;

        student(
                final String examno,
                final String name,
                final String testdiv1,
                final String testdiv2,
                final String testdiv3,
                final String testdiv4,
                final String testdiv5,
                final String testdiv6,
                final String sex,
                final String address1,
                final String address2
        ) {
			_examno = examno;
			_name = name;
			_testdiv1 = testdiv1;
			_testdiv2 = testdiv2;
			_testdiv3 = testdiv3;
			_testdiv4 = testdiv4;
			_testdiv5 = testdiv5;
            _testdiv6 = testdiv6;
			_sex = sex;
			_address1 = address1;
			_address2 = address2;
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

	/**ブランクをセット**/
	private void setSvfInt(
		Vrw32alp svf
	) {
		try {
			svf.VrsOut("NOTE"	,"");
		} catch( Exception ex ) {
			log.error("setSvfInt set error!");
		}

	}
    
}
