package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
 * 塾別成績一覧表
 * 
 * @author nakasone
 *
 */
public class KNJL352J {
    private static final String FORM_NAME = "KNJL352J.frm";
    private static final int PAGE_MAXLINE = 50;
	private	Calendar calw = Calendar.getInstance();
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL352J.class);
    
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
        
    	boolean retflg = false;

    	svf.VrSetForm(FORM_NAME, 1);
    	
    	// 入試区分名称を取得
        _param._testDivName = getNameMst(db2);

        // 帳票出力処理
		retflg = printSeisekiTestDiv(db2, svf);

    	// 最終ページ出力
    	if(retflg){
    		// 合計行を出力
    		printSvfTotal(svf);
    		svf.VrEndPage();
    	}

		return 	retflg;
    }

    /**
     * 指示画面にて入試区分が指定されている場合の帳票出力処理
     * @param svf			帳票オブジェクト
     * @param student		帳票出力対象クラスオブジェクト
     */
    private boolean printSeisekiTestDiv(final DB2UDB db2, final Vrw32alp svf) throws Exception {

    	boolean retflg = false;
    	String oldExamNoTestDiv = null;
    	String newExamNoTestDiv = null;
    	String priSchoolCd = null;
    	int line = 0;
    	int page_cnt = 1;
    	
    	//*-----------------------------------*
    	//\*   入試区分が指定されている場合   *
    	//*-----------------------------------*
		// 塾別成績一覧表データ取得
		final List student = createStudents(db2, _param._testDiv);
        
		for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();
        	if(line == 0){
            	priSchoolCd = sudent._prischoolcd;
        	}
            
            // 受験番号・入試区分のブレイク
            newExamNoTestDiv = sudent._examno+sudent._testdiv;
            if(oldExamNoTestDiv == null  || !oldExamNoTestDiv.equals(newExamNoTestDiv)){
                if( PAGE_MAXLINE == line || !priSchoolCd.equals(sudent._prischoolcd)){
                	// 塾コードは変わらずにページのMAX行を超えた場合
                	if(PAGE_MAXLINE == line && priSchoolCd.equals(sudent._prischoolcd)){
                    	++page_cnt;
                	}
                	// 塾コードがブレイクした場合
                	if(!priSchoolCd.equals(sudent._prischoolcd)){
                		// 合計行を出力
                		printSvfTotal(svf);
                		page_cnt = 1;
                	}
            		svf.VrEndPage();
            		line = 0;
                }
                if(line == 0){
                    // 総ページ数を取得を取得
               		getTotalPage(db2, _param._testDiv, sudent._prischoolcd);
               		// 見出し出力
               		printSvfHead(svf, page_cnt, sudent);
                }
                ++line;
            	// 受験生別の出力
            	outPutPrint(svf, sudent, line);

            	oldExamNoTestDiv = newExamNoTestDiv;
            	priSchoolCd = sudent._prischoolcd;
            	// 合計値加算
            	if(sudent._sex.equals("1")){
            		++_param._toralMan;
            	}
            	if(sudent._sex.equals("2")){
            		++_param._toralWoMan;
            	}
            }
            // 入試区分別得点の出力
            outPutScorePrint(db2, svf, sudent, line);
            
            retflg = true;
        }
		return retflg;
    }
    
    /**
     * 受験生別の帳票出力処理
     * @param svf			帳票オブジェクト
     * @param student		帳票出力対象クラスオブジェクト
     * @param line			出力行
     */
    private void outPutPrint(final Vrw32alp svf, final student sudent, int line) throws Exception {
    	
		//*================*
		//* 明細           *
		//*================*
    	++_param._meisaiNo;
		svf.VrsOutn("NO" 		, line,		String.valueOf(_param._meisaiNo));	// NO
		svf.VrsOutn("EXAMNO" 	, line,		sudent._examno);		// 受験番号
		svf.VrsOutn("NAME", line,		sudent._name);	// 氏名
		// 性別
		if(sudent._sex.equals("1")){
			svf.VrsOutn("SEX" 		, line,		"男");				// 性別
		} else {
			svf.VrsOutn("SEX" 		, line,		"女");				// 性別
		}
		// 合否
		if(sudent._judgediv.equals("1")){
			svf.VrsOutn("JUDGEDIV"	, line,	"合");	// 合格
		}
		if(sudent._judgediv.equals("2")){
			svf.VrsOutn("JUDGEDIV"	, line,	"否");	// 不合格
		}
		// 入試区分
		svf.VrsOutn("TESTDIV2"	, line,	(String)_param._testDivName.get(sudent._testdiv));	// 不合格
		// 受験型
		if(sudent._exam_type.equals("1")){
			svf.VrsOutn("EXAM_TYPE"	, line,	"２");
		}
		if(sudent._exam_type.equals("2")){
			svf.VrsOutn("EXAM_TYPE"	, line,	"４");
		}
		if(sudent._exam_type.equals("3")){
			svf.VrsOutn("EXAM_TYPE"	, line,	"３");
		}

    }

    /**
     * 各入試区分毎の得点出力処理
     * @param db2			ＤＢ接続オブジェクト
     * @param svf			帳票オブジェクト
     * @param student		帳票出力対象クラスオブジェクト
     * @param line			出力行
     */
    private void outPutScorePrint(final DB2UDB db2, final Vrw32alp svf, final student sudent, int line) throws Exception {
   		
		int waru=2;
    	 
		// 得点データ取得
   		final List scoreData = createscoreData(db2, sudent);
   		
		for (Iterator it = scoreData.iterator(); it.hasNext();) {
            final scoreData score = (scoreData) it.next();
        
            if(score._reccount.equals("0")){
            	return;
            }
            
            // 得点表示時
            if(_param._outPut.equals("1")){
       			svf.VrsOutn("SCORE1"	, line, score._score2);	// 国語
       			svf.VrsOutn("SCORE2"	, line, score._score3);	// 算数
       			svf.VrsOutn("SCORE3"	, line, score._score5);	// 理科
       			svf.VrsOutn("SCORE4"	, line, score._score4);	// 社会
       			
       			// ２科合計
       			if(sudent._exam_type.equals("1")){
       	   			int toTal2ka = Integer.parseInt(score._score2)+Integer.parseInt(score._score3);
       	   			svf.VrsOutn("SCORE5"	, line, String.valueOf(toTal2ka));
       			}
       			// ４科合計
       			if(sudent._exam_type.equals("2")){
       	   			int toTal4ka = Integer.parseInt(score._score2) + Integer.parseInt(score._score3) + 
       	   							Integer.parseInt(score._score4)+Integer.parseInt(score._score5);
       	   			svf.VrsOutn("SCORE6"	, line, String.valueOf(toTal4ka));
       			}
       		// 偏差値表示時
            } else {
            	 
            	// 国語
            	if(!score._stdscore2.equals("")){
           			svf.VrsOutn("SCORE1", line, String.valueOf(KNJServletUtils.roundHalfUp(Double.valueOf(score._stdscore2).doubleValue(), 1)));
            	}
       			// 算数
            	if(!score._stdscore3.equals("")){
           			svf.VrsOutn("SCORE2", line, String.valueOf(KNJServletUtils.roundHalfUp(Double.valueOf(score._stdscore3).doubleValue(), 1)));
            	}
       			// 社会
            	if(!score._stdscore5.equals("")){
           			svf.VrsOutn("SCORE3", line, String.valueOf(KNJServletUtils.roundHalfUp(Double.valueOf(score._stdscore5).doubleValue(), 1)));
            	}
       			// 理科
            	if(!score._stdscore4.equals("")){
           			svf.VrsOutn("SCORE4", line, String.valueOf(KNJServletUtils.roundHalfUp(Double.valueOf(score._stdscore4).doubleValue(), 1)));
            	}
       			// ２科合計
       			if(sudent._exam_type.equals("1")){
       				double total2ka_hensati = Double.valueOf(score._stdscore2).doubleValue() + Double.valueOf(score._stdscore3).doubleValue();
       				svf.VrsOutn("SCORE5",   line,
       	           		String.valueOf((float)Math.round( (float)total2ka_hensati / (float)waru * 10) / 10));
       			}
       			// ４科合計
       			if(sudent._exam_type.equals("2")){
       	   			svf.VrsOutn("SCORE6"	, line, sudent._judgeDeviation);
       			}
            }
        }
    }
    
    /**
     * ヘッダ部の帳票出力処理
     * @param svf			帳票オブジェクト
     */
    private void printSvfHead(final Vrw32alp svf, int page_cnt, final student sudent){
		//* ヘッダ
		svf.VrsOut("NENDO" 		,_param._nendo);	// 対象年度
		svf.VrsOut("DATE"	 	,_param._date);		// 作成日
		// 入試区分
		if(_param._testDiv.equals("0")){
			svf.VrsOut("TESTDIV1" 	,	"全て");
		} else {
			svf.VrsOut("TESTDIV1"	,	(String)_param._testDivName.get(_param._testDiv));
		}
		// 塾コード
		svf.VrsOut("PRISCHOOLCD", 	sudent._prischoolcd);
		// 塾名
		svf.VrsOut("PRISCHOOL_NAME", sudent._prischool_name);
		
 		svf.VrsOut( "PAGE",  String.valueOf( page_cnt )  );    //ページ
 		svf.VrsOut( "TOTAL_PAGE",  String.valueOf( _param._totalPage )  );    //総ページ
 		// 明細タイトルの設定
 		if(_param._outPut.equals("1")){
 			//得点表示時
 			svf.VrsOut("ITEM1_1", "２科");
 			svf.VrsOut("ITEM1_2", "合計");
 			svf.VrsOut("ITEM2_1", "４科");
 			svf.VrsOut("ITEM2_2", "合計");
 		} else {
 			svf.VrsOut("ITEM1_1", "２科");
 			svf.VrsOut("ITEM1_2", "平均");
 			svf.VrsOut("ITEM2_1", "判定");
 			svf.VrsOut("ITEM2_2", "値");
 		}
    }
    
    /**
     * フッタ部の帳票出力処理
     * @param svf			帳票オブジェクト
     */
    private void printSvfTotal(final Vrw32alp svf){

    	// 合計数を設定
		int total_count = _param._toralMan + _param._toralWoMan;
		svf.VrsOut("NOTE"	,"男"+String.valueOf(_param._toralMan)+"名,女"+String.valueOf(_param._toralWoMan)+"名,合計"
					+String.valueOf(total_count)+"名");
		_param._toralMan = 0;
		_param._toralWoMan = 0;

    }
    
    /**
     * @param db2				ＤＢ接続オブジェクト
     * @param testDiv			入試区分
     * @return					帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudents(final DB2UDB db2, String testDiv)
    	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getStudentSql(testDiv);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final student student = new student(
                		rs.getString("ENTEXAMYEAR"),
                		rs.getString("APPLICANTDIV"),
                		rs.getString("TESTDIV"),
                		rs.getString("EXAM_TYPE"),
                		rs.getString("EXAMNO"),
                		nvlT(rs.getString("RECEPTNO")),
                		nvlT(rs.getString("JUDGEDIV")),
                		nvlT(rs.getString("JUDGE_DEVIATION")),
                		nvlT(rs.getString("SEX")),
                		nvlT(rs.getString("NAME")),
                		nvlT(rs.getString("PRISCHOOLCD")),
                		nvlT(rs.getString("PRISCHOOL_NAME"))
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
     * @param testDiv		入試区分
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql(String testDiv){
		StringBuffer stb = new StringBuffer();
		
        stb.append(" select W1.ENTEXAMYEAR,");
        stb.append("        W1.APPLICANTDIV,");
        stb.append("        W1.TESTDIV,");
        stb.append("        W1.EXAM_TYPE,");
        stb.append("        W1.EXAMNO,");
        stb.append("        W1.RECEPTNO,");
        stb.append("        W1.JUDGEDIV,");
        stb.append("        W1.JUDGE_DEVIATION,");
        stb.append("        W2.SEX,");
        stb.append("        W2.NAME,");
        stb.append("        W2.PRISCHOOLCD,");
        stb.append("        W3.PRISCHOOL_NAME");
        stb.append(" from   ENTEXAM_RECEPT_DAT W1");
        stb.append(" inner  join ENTEXAM_APPLICANTBASE_DAT W2 ON");
        stb.append("            W1.ENTEXAMYEAR  = W2.ENTEXAMYEAR  and");
        stb.append("            W1.APPLICANTDIV = W2.APPLICANTDIV and");
        stb.append("            W1.EXAMNO       = W2.EXAMNO");
        stb.append(" inner  join PRISCHOOL_MST W3 ON");
        stb.append("            W2.PRISCHOOLCD  = W3.PRISCHOOLCD");
        stb.append(" where");
        stb.append("    W1.ENTEXAMYEAR='"+_param._year+"'");
        if(!_param._testDiv.equals("0")){
            stb.append("   and W1.TESTDIV='"+testDiv+"'");
        }
        stb.append(" order by W2.PRISCHOOLCD,W1.EXAMNO,W1.TESTDIV");
        return stb.toString();
    }
    
    /**
     * 得点取得処理
     * @param db2				ＤＢ接続オブジェクト
     * @param student			帳票出力対象クラスオブジェクト
     * @return					帳票出力対象データリスト
     * @throws Exception
     */
    private List createscoreData(final DB2UDB db2,	 final student sudent)
    	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getScoreDataSql(sudent);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final scoreData scoreData = new scoreData(
                		rs.getString("REC_COUNT"),
                		nvlT(rs.getString("SCORE2")),
                		nvlT(rs.getString("STD_SCORE2")),
                		nvlT(rs.getString("SCORE3")),
                		nvlT(rs.getString("STD_SCORE3")),
                		nvlT(rs.getString("SCORE4")),
                		nvlT(rs.getString("STD_SCORE4")),
                		nvlT(rs.getString("SCORE5")),
                		nvlT(rs.getString("STD_SCORE5"))
                );
                rtnList.add(scoreData);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /**
     * 得点取得SQL生成
     * @param student			帳票出力対象クラスオブジェクト
     */
    private String getScoreDataSql(final student sudent){
        final String rtn;

		rtn = " select"
            + "    COUNT(*) AS REC_COUNT,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '2' THEN SCORE ELSE 0 END) AS SCORE2,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '2' THEN STD_SCORE ELSE 0 END) AS STD_SCORE2,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '3' THEN SCORE ELSE 0 END) AS SCORE3,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '3' THEN STD_SCORE ELSE 0 END) AS STD_SCORE3,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '4' THEN SCORE ELSE 0 END) AS SCORE4,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '4' THEN STD_SCORE ELSE 0 END) AS STD_SCORE4,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '5' THEN SCORE ELSE 0 END) AS SCORE5,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '5' THEN STD_SCORE ELSE 0 END) AS STD_SCORE5"
            + " from ENTEXAM_SCORE_DAT"
            + " where "
            + "    ENTEXAMYEAR = '"+sudent._entexamyear+"' and"
            + "    APPLICANTDIV = '"+sudent._applicantdiv+"' and"
            + "    TESTDIV = '"+sudent._testdiv+"' and"
            + "    EXAM_TYPE = '"+sudent._exam_type+"' and"
            + "    RECEPTNO = '"+sudent._receptno+"' "
            ;
        return rtn;
   }

    /**
     * 総ページ数取得処理
     * @param db2				ＤＢ接続オブジェクト
     * @param testDiv			入試区分
     * @param priSchoolCd		塾コード
     * @return					帳票出力対象データリスト
     * @throws Exception
     */
    private void getTotalPage(final DB2UDB db2, String testDiv, String priSchoolCd)
    	throws SQLException {
    	
        final String sql = getTotalPageSql(testDiv, priSchoolCd);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                _param._totalPage = Integer.parseInt(rs.getString("COUNT"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
    }

    /**
     * 総ページ数取得SQL生成
     */
    private String getTotalPageSql(String testDiv, String priSchoolCd){
		StringBuffer stb = new StringBuffer();
		
        stb.append(" select");
        stb.append("    SUM(T1.COUNT) COUNT");
        stb.append(" from (");
        stb.append("    select CASE WHEN 0 < MOD(COUNT(W1.EXAMNO),50) THEN COUNT(W1.EXAMNO)/50 + 1 ELSE COUNT(W1.EXAMNO)/50 END AS COUNT ");
        stb.append("    from   ENTEXAM_RECEPT_DAT W1");
        stb.append("       inner  join ENTEXAM_APPLICANTBASE_DAT W2 ON");
        stb.append("                  W1.ENTEXAMYEAR  = W2.ENTEXAMYEAR  and");
        stb.append("                  W1.APPLICANTDIV = W2.APPLICANTDIV and");
        stb.append("                  W1.EXAMNO       = W2.EXAMNO");
        stb.append("    where");
        stb.append("        W1.ENTEXAMYEAR='"+_param._year+"' and");
        if(!_param._testDiv.equals("0")){
            stb.append("        W1.TESTDIV='"+testDiv+"' and");
        }
        stb.append("        W2.PRISCHOOLCD='"+priSchoolCd+"' ");
        stb.append(" ) T1");
        return stb.toString();
   }

    /** 名称マスタの入試区分名称取得処理 */
    public HashMap getNameMst(DB2UDB db2) throws SQLException, Exception {
        final String sql = sqlgetCapacityMst();
        HashMap rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String code = rs.getString("NAMECD2");
                final String name = rs.getString("NAME1");
                rtn.put(code, name);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    /** 名称マスタの入試区分名称取得SQL生成 */
    private String sqlgetCapacityMst(){
        final String rtn;

		rtn = " select"
            + "    NAMECD2,"
            + "    NAME1"
            + " from"
            + "    NAME_MST"
            + " where"
            + "    NAMECD1 = 'L004' "
            ;
        return rtn;
    }
    
    /** 生徒クラス */
    private class student {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _exam_type;
        final String _examno;
        final String _receptno;
        final String _judgediv;
        final String _judgeDeviation;
        final String _sex;
        final String _name;
        final String _prischoolcd;
        final String _prischool_name;

        student(
                final String entexamyear,
                final String applicantdiv,
                final String testdiv,
                final String exam_type,
                final String examno,
                final String receptno,
                final String judgediv,
                final String judgeDeviation,
                final String sex,
                final String name,
                final String prischoolcd,
                final String prischool_name
        ) {
			_entexamyear   = entexamyear;
			_applicantdiv   = applicantdiv;
			_testdiv   = testdiv;
			_exam_type   = exam_type;
			_examno   = examno;
			_receptno   = receptno;
			_judgediv   = judgediv;
			_judgeDeviation   = judgeDeviation;
			_sex   = sex;
			_name   = name;
			_prischoolcd   = prischoolcd;
			_prischool_name   = prischool_name;
        }
    }

    /** 得点クラス */
    private class scoreData {
        final String _reccount;
        final String _score2;
        final String _stdscore2;
        final String _score3;
        final String _stdscore3;
        final String _score4;
        final String _stdscore4;
        final String _score5;
        final String _stdscore5;

        scoreData(
                final String reccount,
                final String score2,
                final String stdscore2,
                final String score3,
                final String stdscore3,
                final String score4,
                final String stdscore4,
                final String score5,
                final String stdscore5
        ) {
        	_reccount = reccount;
        	_score2   = score2;
        	_stdscore2   = stdscore2;
        	_score3   = score3;
        	_stdscore3   = stdscore3;
        	_score4   = score4;
        	_stdscore4   = stdscore4;
        	_score5   = score5;
        	_stdscore5   = stdscore5;
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
    	private final String _testDiv;
    	private final String _outPut;
    	private final String _nendo;
    	private final String _date;
        private HashMap _testDivName = new HashMap();
        int _toralMan = 0;
        int _toralWoMan = 0;
        int _totalPage = 0;
        int _meisaiNo = 0;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testDiv = nvlT(request.getParameter("TESTDIV"));
            _outPut = nvlT(request.getParameter("OUTPUT"));
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
