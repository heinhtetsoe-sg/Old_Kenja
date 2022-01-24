package servletpack.KNJD;

import java.io.IOException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Get_Info;


/**
 * 学力試験 クラス別得点分布表
 * 
 * @author nakasone
 *
 */

public class KNJD647 {


    private static final Log log = LogFactory.getLog(KNJD647.class);
    
    private static final String FORM_NAME = "KNJD647.frm";
    private boolean _hasData;
    Param _param;
    /** 開始得点(得点分布表1枚目) */
    private static final int PAGE1_START = 500;
    /** 終了得点(得点分布表1枚目) */
    private static final int PAGE1_END = 351;
    /** 開始得点(得点分布表2枚目) */
    private static final int PAGE2_START = 350;
    /** 終了得点(得点分布表2枚目) */
    private static final int PAGE2_END = 201;
    /** 開始得点(得点分布表3枚目) */
    private static final int PAGE3_START = 200;
    /** 終了得点(得点分布表3枚目) */
    private static final int PAGE3_END = 51;
    /** 開始得点(得点分布表4枚目) */
    private static final int PAGE4_START = 50;
    /** 終了得点(得点分布表4枚目) */
    private static final int PAGE4_END = 0;
    /** 得点分布表(最大行数) */
    private static final int maxgyo = 50;

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
        boolean rtnFlg = false;
        
		// クラス別成績分布表出力データ取得
		final List student = createStudents(db2);
			
		// 帳票出力のメソッド
        rtnFlg = outPutPrint(db2, svf, student);
		return rtnFlg;

    }

    /**
     * 帳票出力処理
     * @param db2		ＤＢ接続オブジェクト
     * @param svf		帳票オブジェクト
     * @param student	帳票出力対象クラスオブジェクト
     * @return		対象データ存在フラグ(有り：TRUE、無し：FALSE)
     */
    private boolean outPutPrint(final DB2UDB db2, final Vrw32alp svf, final List student) {

    	boolean rtnFlg = false;
        int line = 0;
        int page_cnt = 1;
        int start_Row = PAGE1_START;
        boolean makeflg = true;
		int score = 0;
        int gyo = 1;			// 行数
        int icol = 1;			// 列数
        int rui_cnt1 = 0;		// 累計(男)
        int rui_cnt2 = 0;		// 累計(女)
        int rui_cnttotal = 0;	// 累計(計)

        svf.VrSetForm(FORM_NAME, 1);
        
        
        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();
            
            // ヘッダ出力
            if(line == 0){
                setHead(svf, db2);
            }
			score = Integer.valueOf(sudent._score2).intValue();		// 得点2
			while(makeflg){
				// 各ページの得点範囲を判定
				if(page_cnt == 1 && start_Row < PAGE1_END){
					svf.VrEndPage();//ページを出力
	                setHead(svf, db2);
					icol = 1;
					gyo  = 1;
					++page_cnt;
					start_Row = PAGE2_START;
				}
				if(page_cnt == 2 && start_Row < PAGE2_END){
					svf.VrEndPage();//ページを出力
	                setHead(svf, db2);
					icol = 1;
					gyo  = 1;
					++page_cnt;
					start_Row = PAGE3_START;
				}
				if(page_cnt == 3 && start_Row < PAGE3_END){
					svf.VrEndPage();//ページを出力
	                setHead(svf, db2);
					icol = 1;
					gyo  = 1;
					++page_cnt;
					start_Row = PAGE4_START;
				}
				if(start_Row == 0){
					page_cnt = 4;
				}
				// 最大行数を超えた場合
				if(gyo > maxgyo){
					++icol;
					gyo = 1;
				}
				// 点数
       			svf.VrsOutn("POINT" + icol,	gyo , String.valueOf(start_Row) );
				if(start_Row == score){
					// 1組(男)
	       			svf.VrsOutn("B_COUNT" + icol + "_1",	gyo , sudent._cnt1_1 );
					// 1組(女)
	       			svf.VrsOutn("G_COUNT" + icol + "_1",	gyo , sudent._cnt1_2 );
					// 1組(計)
	       			svf.VrsOutn("TOTAL_COUNT" + icol + "_1",gyo , sudent._cnt1_total );
	       			
					// 2組(男)
	       			svf.VrsOutn("B_COUNT" + icol + "_2",	gyo , sudent._cnt2_1 );
					// 2組(女)
	       			svf.VrsOutn("G_COUNT" + icol + "_2",	gyo , sudent._cnt2_2 );
					// 2組(計)
	       			svf.VrsOutn("TOTAL_COUNT" + icol + "_2",gyo , sudent._cnt2_total );

					// 3組(男)
	       			svf.VrsOutn("B_COUNT" + icol + "_3",	gyo , sudent._cnt3_1 );
					// 3組(女)
	       			svf.VrsOutn("G_COUNT" + icol + "_3",	gyo , sudent._cnt3_2 );
					// 3組(計)
	       			svf.VrsOutn("TOTAL_COUNT" + icol + "_3",gyo , sudent._cnt3_total );

					// 4組(男)
	       			svf.VrsOutn("B_COUNT" + icol + "_4",	gyo , sudent._cnt4_1 );
					// 4組(女)
	       			svf.VrsOutn("G_COUNT" + icol + "_4",	gyo , sudent._cnt4_2 );
					// 4組(計)
	       			svf.VrsOutn("TOTAL_COUNT" + icol + "_4",gyo , sudent._cnt4_total );

					// 5組(男)
	       			svf.VrsOutn("B_COUNT" + icol + "_5",	gyo , sudent._cnt5_1 );
					// 5組(女)
	       			svf.VrsOutn("G_COUNT" + icol + "_5",	gyo , sudent._cnt5_2 );
					// 5組(計)
	       			svf.VrsOutn("TOTAL_COUNT" + icol + "_5",gyo , sudent._cnt5_total );

					// 6組(男)
	       			svf.VrsOutn("B_COUNT" + icol + "_6",	gyo , sudent._cnt6_1 );
					// 6組(女)
	       			svf.VrsOutn("G_COUNT" + icol + "_6",	gyo , sudent._cnt6_2 );
					// 6組(計)
	       			svf.VrsOutn("TOTAL_COUNT" + icol + "_6",gyo , sudent._cnt6_total );

					// 集計(男)
	       			svf.VrsOutn("B_TCOUNT" + icol,	gyo , sudent._cnt1_class_total );
					// 集計(女)
	       			svf.VrsOutn("G_TCOUNT" + icol,	gyo , sudent._cnt2_class_total );
					// 集計(計)
	       			svf.VrsOutn("TOTAL_TCOUNT" + icol,gyo , sudent._cnt_class_total );

	       			// 累計(男)
	       			rui_cnt1 = rui_cnt1 + Integer.valueOf(sudent._cnt1_class_total).intValue();
	       			svf.VrsOutn("B_TTCOUNT" + icol,	gyo , String.valueOf(rui_cnt1) );
	       			// 累計(女)
	       			rui_cnt2 = rui_cnt2 + Integer.valueOf(sudent._cnt2_class_total).intValue();
	       			svf.VrsOutn("G_TTCOUNT" + icol,	gyo , String.valueOf(rui_cnt2) );
	       			// 累計(計)
	       			rui_cnttotal = rui_cnttotal + Integer.valueOf(sudent._cnt_class_total).intValue();
	       			svf.VrsOutn("TOTAL_TTCOUNT" + icol,	gyo , String.valueOf(rui_cnttotal) );
	       			
	       			makeflg = false;
				}
			
       			++line;			// 出力件数のカウント
				++gyo;			// 帳票出力行数のカウント
       			--start_Row;	// 点数を1点減算
			}
			
			// 最終ページ・最終行の場合
			if(page_cnt == 4 && start_Row < PAGE4_END){
				svf.VrEndPage();//ページを出力
				break;
			}
				
			makeflg = true;	
        }

        if (line > 0){
    		// 最低得点が最終ページ・最終行未満の場合
    		if((page_cnt < 4) || (page_cnt ==4  && start_Row > PAGE4_END)){
    			while(makeflg){
    				// 各ページの得点範囲を判定
    				if(page_cnt == 1 && start_Row < PAGE1_END){
    					svf.VrEndPage();//ページを出力
    	                setHead(svf, db2);
    					icol = 1;
    					gyo  = 1;
    					++page_cnt;
    					start_Row = PAGE2_START;
    				}
    				if(page_cnt == 2 && start_Row < PAGE2_END){
    					svf.VrEndPage();//ページを出力
    	                setHead(svf, db2);
    					icol = 1;
    					gyo  = 1;
    					++page_cnt;
    					start_Row = PAGE3_START;
    				}
    				if(page_cnt == 3 && start_Row < PAGE3_END){
    					svf.VrEndPage();//ページを出力
    	                setHead(svf, db2);
    					icol = 1;
    					gyo  = 1;
    					++page_cnt;
    					start_Row = PAGE4_START;
    				}
    				// 最終ページ・最終行の場合
    				if(page_cnt == 4 && start_Row < PAGE4_END){
    					svf.VrEndPage();//ページを出力
    					break;
    				}
    				if(start_Row == 0){
    					page_cnt = 4;
    				}
    				// 最大行数を超えた場合
    				if(gyo > maxgyo){
    					++icol;
    					gyo = 1;
    				}
    				// 点数
           			svf.VrsOutn("POINT" + icol,	gyo , String.valueOf(start_Row) );
    			
    				++gyo;			// 帳票出力行数のカウント
           			--start_Row;	// 点数を1点減算
    			}
    		}
        }
        
        if(line > 0){
        	rtnFlg = true;
        }
        return rtnFlg;
    }
    
    /**
     * ヘッダ部の出力を行う
     * @param svf		帳票オブジェクト
     * @param db2		ＤＢ接続オブジェクト
     */
    private void setHead(final Vrw32alp svf, final DB2UDB db2) {
		try {
	        // 年度
	    	svf.VrsOut("NENDO", _param._nendo);
	        // 学年
	    	svf.VrsOut("GRADE", convZenkakuToHankaku(_param._grade.substring(1,2)));
	        // 分類（文系）
	    	if(_param._div.equals("1")){
		    	svf.VrsOut("DIV", "(文)");
	    	}
	        // 分類（理系）
	    	if(_param._div.equals("2")){
		    	svf.VrsOut("DIV", "(理)");
	    	}
			// 作成日
            KNJ_Get_Info getinfo = new KNJ_Get_Info();		//各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;		//各情報を返すためのクラス
			returnval = getinfo.Control(db2);
			svf.VrsOut("DATE" , fomatSakuseiDate(returnval.val3));
    		getinfo = null;
    		returnval = null;
		} catch( Exception e ){
			log.warn("ctrl_date get error!",e);
		}
    }

    
    /**
     * @param db2			ＤＢ接続オブジェクト
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudents( final DB2UDB db2 ) throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getStudentSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final student student = new student(rs.getString("SCORE2"),
                		rs.getString("CNT1_1"),
                		rs.getString("CNT1_2"),
                		rs.getString("CNT1_TOTAL"),
                		rs.getString("CNT2_1"),
                		rs.getString("CNT2_2"),
                		rs.getString("CNT2_TOTAL"),
                		rs.getString("CNT3_1"),
                		rs.getString("CNT3_2"),
                		rs.getString("CNT3_TOTAL"),
                		rs.getString("CNT4_1"),
                		rs.getString("CNT4_2"),
                		rs.getString("CNT4_TOTAL"),
                		rs.getString("CNT5_1"),
                		rs.getString("CNT5_2"),
                		rs.getString("CNT5_TOTAL"),
                		rs.getString("CNT6_1"),
                		rs.getString("CNT6_2"),
                		rs.getString("CNT6_TOTAL"),
                		rs.getString("CNT1_CLASS_TOTAL"),
                		rs.getString("CNT2_CLASS_TOTAL"),
                		rs.getString("CNT_CLASS_TOTAL")
                );
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /** 生徒クラス */
    private class student {
		final String _score2;
		final String _cnt1_1;
		final String _cnt1_2;
		final String _cnt1_total;
		final String _cnt2_1;
		final String _cnt2_2;
		final String _cnt2_total;
		final String _cnt3_1;
		final String _cnt3_2;
		final String _cnt3_total;
		final String _cnt4_1;
		final String _cnt4_2;
		final String _cnt4_total;
		final String _cnt5_1;
		final String _cnt5_2;
		final String _cnt5_total;
		final String _cnt6_1;
		final String _cnt6_2;
		final String _cnt6_total;
		final String _cnt1_class_total;
		final String _cnt2_class_total;
		final String _cnt_class_total;

        student(
        		final String score2,
        		final String cnt1_1,
        		final String cnt1_2,
        		final String cnt1_total,
        		final String cnt2_1,
        		final String cnt2_2,
        		final String cnt2_total,
        		final String cnt3_1,
        		final String cnt3_2,
        		final String cnt3_total,
        		final String cnt4_1,
        		final String cnt4_2,
        		final String cnt4_total,
        		final String cnt5_1,
        		final String cnt5_2,
        		final String cnt5_total,
        		final String cnt6_1,
        		final String cnt6_2,
        		final String cnt6_total,
        		final String cnt1_class_total,
        		final String cnt2_class_total,
        		final String cnt_class_total
        ) {
    		_score2 = score2;
    		_cnt1_1 = cnt1_1;
    		_cnt1_2 = cnt1_2;
    		_cnt1_total = cnt1_total;
    		_cnt2_1 = cnt2_1;
    		_cnt2_2 = cnt2_2;
    		_cnt2_total = cnt2_total;
    		_cnt3_1 = cnt3_1;
    		_cnt3_2 = cnt3_2;
    		_cnt3_total = cnt3_total;
    		_cnt4_1 = cnt4_1;
    		_cnt4_2 = cnt4_2;
    		_cnt4_total = cnt4_total;
    		_cnt5_1 = cnt5_1;
    		_cnt5_2 = cnt5_2;
    		_cnt5_total = cnt5_total;
    		_cnt6_1 = cnt6_1;
    		_cnt6_2 = cnt6_2;
    		_cnt6_total = cnt6_total;
    		_cnt1_class_total = cnt1_class_total;
    		_cnt2_class_total = cnt2_class_total;
    		_cnt_class_total = cnt_class_total;
        }
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql() {
    	
        final String rtn;
        rtn = " select"
            + "    W2.SCORE2,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='001' and W3.SEX='1' THEN '1' END) AS CNT1_1,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='001' and W3.SEX='2' THEN '1' END) AS CNT1_2,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='001' and W3.SEX IN('1','2') THEN '1' END) AS CNT1_TOTAL,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='002' and W3.SEX='1' THEN '1' END) AS CNT2_1,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='002' and W3.SEX='2' THEN '1' END) AS CNT2_2,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='002' and W3.SEX IN('1','2') THEN '1' END) AS CNT2_TOTAL,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='003' and W3.SEX='1' THEN '1' END) AS CNT3_1,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='003' and W3.SEX='2' THEN '1' END) AS CNT3_2,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='003' and W3.SEX IN('1','2') THEN '1' END) AS CNT3_TOTAL,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='004' and W3.SEX='1' THEN '1' END) AS CNT4_1,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='004' and W3.SEX='2' THEN '1' END) AS CNT4_2,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='004' and W3.SEX IN('1','2') THEN '1' END) AS CNT4_TOTAL,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='005' and W3.SEX='1' THEN '1' END) AS CNT5_1,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='005' and W3.SEX='2' THEN '1' END) AS CNT5_2,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='005' and W3.SEX IN('1','2') THEN '1' END) AS CNT5_TOTAL,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='006' and W3.SEX='1' THEN '1' END) AS CNT6_1,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='006' and W3.SEX='2' THEN '1' END) AS CNT6_2,"
            + "    COUNT(CASE WHEN W1.HR_CLASS='006' and W3.SEX IN('1','2') THEN '1' END) AS CNT6_TOTAL,"
            + "    COUNT(CASE WHEN W3.SEX='1' THEN '1' END) AS CNT1_CLASS_TOTAL,"
            + "    COUNT(CASE WHEN W3.SEX='2' THEN '1' END) AS CNT2_CLASS_TOTAL,"
            + "    COUNT(CASE WHEN W3.SEX IN('1','2') THEN '1' END) AS CNT_CLASS_TOTAL"
            + " from"
            // 学籍在籍データ
            + "    SCHREG_REGD_DAT W1"
            // 成績A値順位データ
            + "    INNER JOIN RECORD_MOCK_RANK_DAT W2 ON"
            + "          W1.YEAR = W2.YEAR and"
            + "          W1.SCHREGNO = W2.SCHREGNO"
            // 学籍基礎マスタ
            + "    INNER JOIN SCHREG_BASE_MST W3 ON"
            + "          W2.SCHREGNO = W3.SCHREGNO"
           //抽出条件
            + " where"
            + "    W1.YEAR = '" + _param._year + "' and"
            + "    W1.SEMESTER = '" + _param._semester + "' and"
            + "    W1.GRADE = '" + _param._grade + "' and"
            + "    W2.DATA_DIV = '0' and"
            + "    W2.COURSE_DIV = '" + _param._div + "' and"
            + "    W2.SUBCLASSCD = '333333' and"
            + "    W2.SCORE2 IS NOT NULL"
            + " group by"
            + "    W2.SCORE2"
            + " order by"
            + "    W2.SCORE2 DESC"
            ;

        log.debug("帳票出力対象データ抽出 の SQL=" + rtn);
        return rtn;
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
    	private final String _year;
    	private final String _semester;
    	private final String _programid;
    	private final String _loginDate;
    	private final String _grade;
    	private final String _div;
    	private final String _nendo;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _programid = request.getParameter("PRGID");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
            _div = request.getParameter("DIV");
    		String sNENDO = convZenkakuToHankaku(_year);
            _nendo = sNENDO + "年度";
        };
        
    }
    
    /**
     * 半角数字を全角数字に変換する
     */
    private String convZenkakuToHankaku(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c >= '0' && c <= '9') {
            sb.setCharAt(i, (char) (c - '0' + 0xff10));
          }
        }
        return sb.toString();
    }

    /**
     * 日付をフォーマットYYYY年MM月DD日に設定する
     */
    private String fomatSakuseiDate(String cnvDate) {

    	String retDate = "";
    	try {
			DateFormat foramt = new SimpleDateFormat("yyyy-MM-dd"); 
			//文字列よりDate型へ変換
			Date date1 = foramt.parse(cnvDate); 
			// 年月日のフォーマットを指定
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'M'月'd'日'");
			// Date型より文字列へ変換
			retDate = sdf1.format(date1);
		} catch( Exception e ){
			log.error("setHeader set error!");
		}
		return retDate;
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
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

}
