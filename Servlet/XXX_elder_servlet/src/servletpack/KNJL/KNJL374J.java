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
 * 合格者名簿(全体)
 * 
 * @author nakasone
 *
 */
public class KNJL374J {
    private static final String FORM_NAME = "KNJL374J.frm";
    private static final int PAGE_MAXLINE = 40;
	private	Calendar calw = Calendar.getInstance();
    private boolean _hasData;
    Param _param;

    /*
     * 試験科目コード
     */
    /** 国語： */
    private static final String TESTSUBCLASSCD_NATIONAL_LANG = "2";
    /** 算数： */
    private static final String TESTSUBCLASSCD_ARITHMETIC = "3";
    /** 理科： */
    private static final String TESTSUBCLASSCD_SCIENCE = "5";
    /** 社会： */
    private static final String TESTSUBCLASSCD_SOCIETY = "4";

    private static final Log log = LogFactory.getLog(KNJL374J.class);
    
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
        // 総ページ数を取得を取得
   		getTotalPage(db2);
        // 帳票出力処理
		retflg = printGokakuMeibo(db2, svf);

    	// 最終ページ出力
    	if(retflg){
    		// 合計行を出力
    		printSvfTotal(svf);
    		svf.VrEndPage();
    	}

		return 	retflg;
    }

    /**
     * 合格者名簿の帳票出力処理
     * @param db2			ＤＢ接続オブジェクト
     * @param svf			帳票オブジェクト
     */
    private boolean printGokakuMeibo(final DB2UDB db2, final Vrw32alp svf) throws Exception {

    	boolean retflg = false;
    	String oldExamNoTestDiv = null;
    	String newExamNoTestDiv = null;
    	int line = 0;
    	int page_cnt = 1;
    	
    	//*-----------------------------------*
    	//\*   入試区分が指定されている場合   *
    	//*-----------------------------------*
		// 合格者名簿データ取得
		final List student = createStudents(db2);
        
		for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();

            // 受験番号・入試区分のブレイク
            newExamNoTestDiv = sudent._examno+sudent._testdiv;
            if(oldExamNoTestDiv == null  || !oldExamNoTestDiv.equals(newExamNoTestDiv)){
                if( PAGE_MAXLINE == line){
            		svf.VrEndPage();
               		++page_cnt;
            		line = 0;
                }
                if(line == 0){
               		// 見出し出力
               		printSvfHead(svf, page_cnt, sudent);
                }
                ++line;
            	// 受験生別の出力
            	outPutPrint(db2, svf, sudent, line);

            	oldExamNoTestDiv = newExamNoTestDiv;
            	// 合計値加算
            	if(sudent._sex.equals("1")){
            		++_param._toralMan;
            	}
            	if(sudent._sex.equals("2")){
            		++_param._toralWoMan;
            	}
            }
            
            retflg = true;
        }
		return retflg;
    }
    
    /**
     * 帳票出力処理
     * @param svf			帳票オブジェクト
     * @param student		帳票出力対象クラスオブジェクト
     * @param line			出力行
     */
    private void outPutPrint(final DB2UDB db2, final Vrw32alp svf, final student sudent, int line) throws Exception {
    	
		//*================*
		//* 明細           *
		//*================*
    	++_param._meisaiNo;
		svf.VrsOutn("NO" 		, line,		String.valueOf(_param._meisaiNo));	// NO
		svf.VrsOutn("EXAMNO" 	, line,		sudent._examno);		// 受験番号
		svf.VrsOutn(setformatArea("NAME", sudent._name, 10, "1", "2_1")	, line,		sudent._name);	// 氏名
		// 性別
		if(sudent._sex.equals("1")){
			svf.VrsOutn("SEX" 		, line,		"男");				// 性別
		} else {
			svf.VrsOutn("SEX" 		, line,		"女");				// 性別
		}
		if(!sudent._testdiv.equals("") && sudent._judgediv.equals("1")){
			// 合否区分
			svf.VrsOutn("PASSDIV"	, line,	(String)_param._testDivName.get(sudent._testdiv));
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
		
		// 塾名・塾兄弟情報
        if(!sudent._remark1.equals("")){
    		svf.VrsOutn(setformatArea("PRISCHOOL_NAME", sudent._prischool_name+"／"+sudent._remark1, 13, "1", "2_1"),
    				line,	sudent._prischool_name+"／"+sudent._remark1);
        } else {
    		svf.VrsOutn(setformatArea("PRISCHOOL_NAME", sudent._prischool_name, 13, "1", "2_1")	, line,		sudent._prischool_name);
        }

        // 志願者備考
		svf.VrsOutn(setformatArea("REMARK1", sudent._name, 20, "_1", "_2")	, line,		sudent._remark2);

		// 志願者得点備考データ取得
		final List scoreRemark = createScoreRemarkData(db2, sudent);
		if(scoreRemark.size() > 0){
			// 試験備考
			svf.VrsOutn(setformatArea("REMARK2", getTestRemarkVal(scoreRemark), 20, "_1", "_2")	, line,	getTestRemarkVal(scoreRemark));
		}

    }

    private String getTestRemarkVal(List scoreRemark) {
        String[] scoreRemake = new String[4];

        for (Iterator it = scoreRemark.iterator(); it.hasNext();) {
            final scoreRemarkData score = (scoreRemarkData) it.next();
        	
            scoreRemake[0] = nvlT(score._score1Remark);	// 国語備考
            scoreRemake[1] = nvlT(score._score2Remark);	// 算数備考
            scoreRemake[2] = nvlT(score._score3Remark);	// 社会備考
            scoreRemake[3] = nvlT(score._score4Remark);	// 理科備考
        }
        
        final StringBuffer sb = new StringBuffer();
    	boolean hitflg = false;

        for (int idx = 0; idx < scoreRemake.length; idx++) {

        	if(!scoreRemake[idx].equals("")){
        		if(hitflg){
            		sb.append("／");
            	}
        		switch (idx) {
        		  case 0:
                	sb.append("2:");
        			break;	
        		  case 1:
                	sb.append("3:");
        			break;	
        		  case 2:
                	sb.append("4:");
        			break;	
        		  case 3:
                	sb.append("5:");
        			break;	
        		  default:
        			break;
        		}
        		sb.append(scoreRemake[idx]);
        		hitflg = true;
        	}
        }
        return sb.toString();
    }
    
    /**
     * ヘッダ部の帳票出力処理
     * @param svf			帳票オブジェクト
     */
    private void printSvfHead(final Vrw32alp svf, int page_cnt, final student sudent){

    	//* ヘッダ
		svf.VrsOut("NENDO" 	,_param._nendo);	// 対象年度
		svf.VrsOut("DATE"	,_param._date);		// 作成日
 		svf.VrsOut("PAGE"	,String.valueOf( page_cnt )  );    //ページ
 		svf.VrsOut("TOTAL_PAGE",  String.valueOf( _param._totalPage )  );    //総ページ
		
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
     * @return					帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudents(final DB2UDB db2)
    	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getStudentSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final student student = new student(
                		rs.getString("EXAMNO"),
                		rs.getString("SEX"),
                		rs.getString("NAME"),
                		nvlT(rs.getString("REMARK1")),
                		nvlT(rs.getString("REMARK2")),
                		nvlT(rs.getString("APPLICANTDIV")),
                		nvlT(rs.getString("RECEPTNO")),
                		nvlT(rs.getString("TESTDIV")),
                		nvlT(rs.getString("EXAM_TYPE")),
                		nvlT(rs.getString("JUDGEDIV")),
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
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql(){
		StringBuffer stb = new StringBuffer();
		
        stb.append(" select W1.EXAMNO,");
        stb.append("        W1.SEX,");
        stb.append("        W1.NAME,");
        stb.append("        W1.REMARK1,");
        stb.append("        W1.REMARK2,");
        stb.append("        W2.APPLICANTDIV,");
        stb.append("        W2.RECEPTNO,");
        stb.append("        W2.TESTDIV,");
        stb.append("        W2.EXAM_TYPE,");
        stb.append("        W2.JUDGEDIV,");
        stb.append("        W3.PRISCHOOL_NAME");
        stb.append(" from   ENTEXAM_APPLICANTBASE_DAT W1");
        stb.append(" left  join ENTEXAM_RECEPT_DAT W2 ON");
        stb.append("            W1.ENTEXAMYEAR  = W2.ENTEXAMYEAR  and");
        stb.append("            W1.APPLICANTDIV = W2.APPLICANTDIV and");
        stb.append("            W1.EXAMNO       = W2.EXAMNO and");
        stb.append("            W2.JUDGEDIV='1' ");
        stb.append(" left  join PRISCHOOL_MST W3 ON");
        stb.append("            W1.PRISCHOOLCD  = W3.PRISCHOOLCD");
        stb.append(" where");
        stb.append("    W1.ENTEXAMYEAR='"+_param._year+"' and");
        stb.append("    W1.JUDGEMENT='1' ");
        stb.append(" order by W1.EXAMNO,W1.TESTDIV");
        return stb.toString();
    }
    
    /**
     * 総ページ数取得処理
     * @param db2				ＤＢ接続オブジェクト
     * @throws Exception
     */
    private void getTotalPage(final DB2UDB db2)
    	throws SQLException {
    	
        final String sql = getTotalPageSql();
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
    private String getTotalPageSql(){
		StringBuffer stb = new StringBuffer();
		
        stb.append(" select");
        stb.append("    SUM(T1.COUNT) COUNT");
        stb.append(" from (");
        stb.append("    select CASE WHEN 0 < MOD(COUNT(W1.EXAMNO),40) THEN COUNT(W1.EXAMNO)/40 + 1 ELSE COUNT(W1.EXAMNO)/40 END AS COUNT ");
        stb.append("    from   ENTEXAM_APPLICANTBASE_DAT W1");
        stb.append("       left  join ENTEXAM_RECEPT_DAT W2 ON");
        stb.append("                  W1.ENTEXAMYEAR  = W2.ENTEXAMYEAR  and");
        stb.append("                  W1.APPLICANTDIV = W2.APPLICANTDIV and");
        stb.append("                  W1.EXAMNO       = W2.EXAMNO and");
        stb.append("                  W2.JUDGEDIV='1' ");
        stb.append("    where");
        stb.append("       W1.ENTEXAMYEAR='"+_param._year+"' and");
        stb.append("       W1.JUDGEMENT='1' ");
        stb.append(" ) T1");
        return stb.toString();
   }

    /**
     * 志願者得点備考データ取得処理
     * @param sudent			生徒データオブジェクト
     * @param db2				ＤＢ接続オブジェクト
     * @return					帳票出力対象データリスト
     * @throws Exception
     */
    private List createScoreRemarkData(final DB2UDB db2, final student sudent)
    	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getStudentSql(sudent);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final scoreRemarkData scoreRemarkData = new scoreRemarkData(
                		nvlT(rs.getString("SCORE1_REMARK")),
                		nvlT(rs.getString("SCORE2_REMARK")),
                		nvlT(rs.getString("SCORE3_REMARK")),
                		nvlT(rs.getString("SCORE4_REMARK"))
                );
                rtnList.add(scoreRemarkData);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    /**
     * 志願者得点備考データ抽出ＳＱＬ生成処理
     * @param sudent		生徒データオブジェクト
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql(final student sudent){
		StringBuffer stb = new StringBuffer();
		
        stb.append(" select");
        stb.append("     value(W2.REMARK, '') as SCORE1_REMARK,");
        stb.append("     value(W3.REMARK, '') as SCORE2_REMARK,");
        stb.append("     value(W4.REMARK, '') as SCORE3_REMARK,");
        stb.append("     value(W5.REMARK, '') as SCORE4_REMARK ");
        stb.append(" from   ENTEXAM_RECEPT_DAT W1");
        stb.append("     left join ENTEXAM_SCORE_REMARK_DAT W2 on (");
        stb.append("         W1.ENTEXAMYEAR      = W2.ENTEXAMYEAR");
        stb.append("         and W1.APPLICANTDIV = W2.APPLICANTDIV");
        stb.append("         and W1.TESTDIV      = W2.TESTDIV");
        stb.append("         and W1.EXAM_TYPE    = W2.EXAM_TYPE");
        stb.append("         and W1.RECEPTNO     = W2.RECEPTNO");
        stb.append("         and W2.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_NATIONAL_LANG + "')");
        stb.append("     left join ENTEXAM_SCORE_REMARK_DAT W3 on (");
        stb.append("         W1.ENTEXAMYEAR      = W3.ENTEXAMYEAR");
        stb.append("         and W1.APPLICANTDIV = W3.APPLICANTDIV");
        stb.append("         and W1.TESTDIV      = W3.TESTDIV");
        stb.append("         and W1.EXAM_TYPE    = W3.EXAM_TYPE");
        stb.append("         and W1.RECEPTNO     = W3.RECEPTNO");
        stb.append("         and W3.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_ARITHMETIC + "')");
        stb.append("     left join ENTEXAM_SCORE_REMARK_DAT W4 on (");
        stb.append("         W1.ENTEXAMYEAR      = W4.ENTEXAMYEAR");
        stb.append("         and W1.APPLICANTDIV = W4.APPLICANTDIV");
        stb.append("         and W1.TESTDIV      = W4.TESTDIV");
        stb.append("         and W1.EXAM_TYPE    = W4.EXAM_TYPE");
        stb.append("         and W1.RECEPTNO     = W4.RECEPTNO");
        stb.append("         and W4.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_SOCIETY + "')");
        stb.append("     left join ENTEXAM_SCORE_REMARK_DAT W5 on (");
        stb.append("         W1.ENTEXAMYEAR      = W5.ENTEXAMYEAR");
        stb.append("         and W1.APPLICANTDIV = W5.APPLICANTDIV");
        stb.append("         and W1.TESTDIV      = W5.TESTDIV");
        stb.append("         and W1.EXAM_TYPE    = W5.EXAM_TYPE");
        stb.append("         and W1.RECEPTNO     = W5.RECEPTNO");
        stb.append("         and W5.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_SCIENCE + "')");
        stb.append(" where");
        stb.append("    W1.ENTEXAMYEAR  ='"+_param._year +"' and");
        stb.append("    W1.APPLICANTDIV ='"+ sudent._applicantdiv +"' and");
        stb.append("    W1.TESTDIV      ='"+ sudent._testdiv +"' and");
        stb.append("    W1.EXAM_TYPE    ='"+ sudent._exam_type +"' and");
        stb.append("    W1.RECEPTNO     ='"+ sudent._receptno +"' and");
        stb.append("    W1.EXAMNO       ='"+ sudent._examno +"'");
        return stb.toString();
    }

    /** 志願者得点備考データクラス */
    private class scoreRemarkData {
        final String _score1Remark;
        final String _score2Remark;
        final String _score3Remark;
        final String _score4Remark;

        scoreRemarkData(
                final String score1Remark,
                final String score2Remark,
                final String score3Remark,
                final String score4Remark
        ) {
			_score1Remark   = score1Remark;
			_score2Remark   = score2Remark;
			_score3Remark   = score3Remark;
			_score4Remark   = score4Remark;
        }
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
        final String _examno;
        final String _sex;
        final String _name;
        final String _remark1;
        final String _remark2;
        final String _applicantdiv;
        final String _receptno;
        final String _testdiv;
        final String _exam_type;
        final String _judgediv;
        final String _prischool_name;

        student(
                final String examno,
                final String sex,
                final String name,
                final String remark1,
                final String remark2,
                final String applicantdiv,
                final String receptno,
                final String testdiv,
                final String exam_type,
                final String judgediv,
                final String prischool_name
        ) {
			_examno   = examno;
			_sex   = sex;
			_name   = name;
			_remark1   = remark1;
			_remark2   = remark2;
			_applicantdiv   = applicantdiv;
			_receptno   = receptno;
			_testdiv   = testdiv;
			_exam_type   = exam_type;
			_judgediv   = judgediv;
			_prischool_name   = prischool_name;
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

}
