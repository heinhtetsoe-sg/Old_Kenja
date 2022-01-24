package servletpack.KNJL;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
 * 出願・受験結果・手続一覧表
 * 
 * @author nakasone
 *
 */
public class KNJL351J {
    private static final String FORM_NAME = "KNJL351J.frm";
    private static final String FORM_NAME2 = "KNJL351J_2.frm";
    private static final String JUDGE_GOKAKU = "◎";
    private static final String JUDGE_FUGOKAKU = "×";
    private static final String JUDGE_MI = "-";
    private static final String JUDGE_KURIAGE = "◇";
    private static final int PAGE_MAXLINE = 50;
	private	Calendar calw = Calendar.getInstance();
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL351J.class);
    
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
        
    	boolean refflg = false;
    	String breakExamNo = null;
    	int line = 0;
    	int page_cnt = 1;


        if (_param._isInfluence) {
            svf.VrSetForm(FORM_NAME2, 1);
        } else {
            svf.VrSetForm(FORM_NAME, 1);
        }

        // 出願回数取得処理
	    _param._examCount = getExamCount(db2);
        // 生徒情報を取得を取得
   		final List student = createStudents(db2);
        // 総ページ数を取得を取得
   		getTotalPage(db2);
   		
   		// 見出し出力
   		printSvfHead(svf,page_cnt);

        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();
        
            // 受験番号のブレイク
            if(breakExamNo == null  || !breakExamNo.equals(sudent._examno)){
                if( PAGE_MAXLINE == line ){
                	++page_cnt;
            		svf.VrEndPage();
            		line = 0;
                }
                if(line == 0){
               		// 見出し出力
               		printSvfHead(svf,page_cnt);
                }
                ++line;
            	// 受験生別の出力
            	outPutPrint1(svf, sudent, line);

            	breakExamNo = sudent._examno;
            	// 合計値加算
            	if(sudent._sex.equals("1")){
            		++_param._toralMan;
            	}
            	if(sudent._sex.equals("2")){
            		++_param._toralWoMan;
            	}
            }
            
            // 入試区分別得点の出力
        	outPutPrint2(db2, svf, sudent, line);
        }
        

		if (breakExamNo != null) {
			// 合計数を設定
			int total_count = _param._toralMan + _param._toralWoMan;
			svf.VrsOut("NOTE"	,"男"+String.valueOf(_param._toralMan)+"名,女"+String.valueOf(_param._toralWoMan)+"名,合計"
						+String.valueOf(total_count)+"名");
    		svf.VrEndPage();
    		refflg = true;
		}
		
		return 	refflg;
    }
    
    /**
     * 受験生別の帳票出力処理
     * @param svf			帳票オブジェクト
     * @param student		帳票出力対象クラスオブジェクト
     * @param line			出力行
     */
    private void outPutPrint1(final Vrw32alp svf, final student sudent, int line) throws Exception {
    	
		//*================*
		//* 明細           *
		//*================*
    	++_param._meisaiNo;
		svf.VrsOutn("NO" 		, line,		String.valueOf(_param._meisaiNo));	// NO
		svf.VrsOutn("EXAMNO1" 	, line,		sudent._examno);		// 受験番号
		svf.VrsOutn(setformatArea("NAME", sudent._name, 14, "1", "2")	, line,		sudent._name);	// 氏名
		// 性別
		if(sudent._sex.equals("1")){
			svf.VrsOutn("SEX" 		, line,		"男");				// 性別
		} else {
			svf.VrsOutn("SEX" 		, line,		"女");				// 性別
		}
		// 出願回数
		svf.VrsOutn("COUNT"	, line,(String)_param._examCount.get(sudent._examno));
		// 手続き：２次
		if(sudent._procedurediv.equals("1")){
			if(!sudent._proceduredate.equals("")){
				svf.VrsOutn("PROCEDUREDIVDATE2"	, line,	fomatSakuseiDate(sudent._proceduredate, "yyyy-MM-dd", "M/d"));
			}
		}
		// 辞退者
		if(sudent._entdiv.equals("2")){
			svf.VrsOutn("MARK6"	, line,	"レ");
		}
		// 志願者備考
		svf.VrsOutn(setformatArea("REMARK", sudent._remark2, 20, "1_1", "1_2"), line,	sudent._remark2);
		// 塾名/塾兄弟情報
		if(!sudent._remark1.equals("")){
			svf.VrsOutn(setformatArea("PRISCHOOL_NAME", sudent._prischool_name+"／"+sudent._remark1, 13, "1", "2_1"),
			line,	sudent._prischool_name+"／"+sudent._remark1);
		} else {
			svf.VrsOutn(setformatArea("PRISCHOOL_NAME", sudent._prischool_name, 13, "1", "2_1"),
			line,	sudent._prischool_name);
		}

    }

    /**
     * 各入試区分毎の得点出力処理
     * @param db2			ＤＢ接続オブジェクト
     * @param svf			帳票オブジェクト
     * @param student		帳票出力対象クラスオブジェクト
     * @param line			出力行
     */
    private void outPutPrint2(final DB2UDB db2, final Vrw32alp svf, final student sudent, int line) throws Exception {
   	
        final String i;
        if ("6".equals(sudent._testdiv)) {
            i = "7";
        } else {
            i = sudent._testdiv;
        }

        // 手続き：１次
		if(sudent._procedurediv1.equals("1")){
			if(!sudent._proceduredate1.equals("")){
				svf.VrsOutn("PROCEDUREDIVDATE1"	, line,	fomatSakuseiDate(sudent._proceduredate1, "yyyy-MM-dd", "M/d"));
			}
		}
		// 合否
		if(sudent._examinee_div.equals("1") && sudent._judgediv.equals("1")){
			svf.VrsOutn("MARK"+i	, line,	JUDGE_GOKAKU);	// 合格
		}
		if(sudent._examinee_div.equals("1") && sudent._judgediv.equals("2")){
			svf.VrsOutn("MARK"+i	, line,	JUDGE_FUGOKAKU);	// 不合格
		}
		if(sudent._examinee_div.equals("2")){
			svf.VrsOutn("MARK"+i	, line,	JUDGE_MI);	// 未受験
		}
		if(sudent._specialmeasures.equals("1") && sudent._judgediv.equals("2")){
			svf.VrsOutn("MARK"+i	, line,	JUDGE_KURIAGE);	// 繰上合格
		}

		// 得点データ取得
   		final List scoreData = createscoreData(db2, sudent);
   		
		for (Iterator it = scoreData.iterator(); it.hasNext();) {
            final scoreData score = (scoreData) it.next();
        
            if(score._reccount.equals("0")){
            	return;
            }
            // Ａ１・Ａ２・Ｂ・Ｃ・Ｄ	
       		if(sudent._testdiv.equals("1") || sudent._testdiv.equals("2") ||
       			sudent._testdiv.equals("3") || sudent._testdiv.equals("4") || sudent._testdiv.equals("6")){
       			svf.VrsOutn("SCORE"+i+"_1"	, line, score._score2);	// 国語
       			svf.VrsOutn("SCORE"+i+"_2"	, line, score._score3);	// 算数
       			svf.VrsOutn("SCORE"+i+"_3"	, line, score._score5);	// 理科
       			svf.VrsOutn("SCORE"+i+"_4"	, line, score._score4);	// 社会
       		}
            // 帰国生	
       		if(sudent._testdiv.equals("5")){
       			svf.VrsOutn("SCORE"+i+"_1"	, line, score._score2);	// 国語
       			svf.VrsOutn("SCORE"+i+"_2"	, line, score._score3);	// 算数
       			svf.VrsOutn("SCORE"+i+"_3"	, line, score._score1);	// 英語
       		}
        }
    }
    
    /**
     * 帳票出力処理
     * @param svf			帳票オブジェクト
     */
    private void printSvfHead(final Vrw32alp svf, int page_cnt){
		//* ヘッダ
		svf.VrsOut("NENDO" 		,_param._nendo);	// 対象年度
		svf.VrsOut("DATE"	 	,_param._date);		// 作成日
		if(_param._sort.equals("1")){
			svf.VrsOut("SUBTITLE" 	,"受験番号順");	// サブタイトル
		} else {
			svf.VrsOut("SUBTITLE" 	,"塾名順");		// サブタイトル
		}
 		svf.VrsOut( "PAGE",  String.valueOf( page_cnt )  );    //ページ
 		svf.VrsOut( "TOTAL_PAGE",  String.valueOf( _param._totalPage )  );    //総ページ
		
    }
    
    /**
     * @param db2				ＤＢ接続オブジェクト
     * @param test_div			入試区分
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
                		rs.getString("ENTEXAMYEAR"),
                		rs.getString("APPLICANTDIV"),
                		rs.getString("TESTDIV"),
                		rs.getString("EXAM_TYPE"),
                		rs.getString("EXAMNO"),
                		nvlT(rs.getString("RECEPTNO")),
                		nvlT(rs.getString("EXAMINEE_DIV")),
                		nvlT(rs.getString("SEX")),
                		nvlT(rs.getString("NAME")),
                		nvlT(rs.getString("SPECIAL_MEASURES")),
             			nvlT(rs.getString("ENTDIV")),
             			nvlT(rs.getString("REMARK1")),
             			nvlT(rs.getString("REMARK2")),
             			nvlT(rs.getString("PROCEDUREDIV")),
             			nvlT(rs.getString("PROCEDUREDATE")),
             			nvlT(rs.getString("JUDGEDIV")),
             			nvlT(rs.getString("PROCEDUREDIV1")),
             			nvlT(rs.getString("PROCEDUREDATE1")),
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
     * @param test_div		入試区分
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql(){
        StringBuffer stb = new StringBuffer();
		
        stb.append(" select W1.ENTEXAMYEAR,");
        stb.append("        W1.APPLICANTDIV,");
        stb.append("        W1.TESTDIV,");
        stb.append("        W1.EXAM_TYPE,");
        stb.append("        W1.EXAMNO,");
        stb.append("        W1.EXAMINEE_DIV,");
        stb.append("        W2.SEX,");
        stb.append("        W2.NAME,");
        stb.append("        W2.SPECIAL_MEASURES,");
        stb.append("        W2.ENTDIV,");
        stb.append("        W2.REMARK1,");
        stb.append("        W2.REMARK2,");
        stb.append("        W2.PROCEDUREDIV,");
        stb.append("        W2.PROCEDUREDATE,");
        stb.append("        W3.RECEPTNO,");
        stb.append("        W3.JUDGEDIV,");
        stb.append("        W3.PROCEDUREDIV1,");
        stb.append("        W3.PROCEDUREDATE1,");
        stb.append("        W4.PRISCHOOL_NAME");
        stb.append(" from   ENTEXAM_DESIRE_DAT W1");
        if (!_param._testDiv.equals("0")) {
            stb.append(" inner join ENTEXAM_DESIRE_DAT W0 ON");
            stb.append("            W0.ENTEXAMYEAR  = W1.ENTEXAMYEAR  and");
            stb.append("            W0.TESTDIV      = '"+_param._testDiv+"' and");
            stb.append("            W0.EXAMNO       = W1.EXAMNO");
        }
        stb.append(" left  join ENTEXAM_APPLICANTBASE_DAT W2 ON");
        stb.append("            W1.ENTEXAMYEAR  = W2.ENTEXAMYEAR  and");
        stb.append("            W1.APPLICANTDIV = W2.APPLICANTDIV and");
        stb.append("            W1.EXAMNO       = W2.EXAMNO");
        stb.append(" left  join ENTEXAM_RECEPT_DAT W3 ON");
        stb.append("            W1.ENTEXAMYEAR  = W3.ENTEXAMYEAR  and");
        stb.append("            W1.APPLICANTDIV = W3.APPLICANTDIV and");
        stb.append("            W1.TESTDIV      = W3.TESTDIV      and");
        stb.append("            W1.EXAM_TYPE    = W3.EXAM_TYPE    and");
        stb.append("            W1.EXAMNO       = W3.EXAMNO");
        stb.append(" left  join PRISCHOOL_MST W4 ON");
        stb.append("            W2.PRISCHOOLCD  = W4.PRISCHOOLCD");
        stb.append(" where");
        stb.append("    W1.ENTEXAMYEAR='"+_param._year+"' ");
        stb.append(" order by ");
        if(_param._sort.equals("1")){
            stb.append(" W1.EXAMNO, W4.PRISCHOOL_NAME");
        } else {
            stb.append(" W4.PRISCHOOL_NAME, W1.EXAMNO");
        }

        return stb.toString();
    }
    
    /** 出願回数抽出処理 */
    public HashMap getExamCount(DB2UDB db2) throws SQLException, Exception {
        final String sql = getExamCountSql();
        HashMap rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String code = rs.getString("EXAMNO");
                final String val = rs.getString("CNT_TOTAL");
                rtn.put(code, val);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    /** 出願回数取得SQL生成 */
    private String getExamCountSql(){
        final String rtn;

		rtn = " select"
            + "    T1.EXAMNO,"
            + "    COUNT(*) AS CNT_TOTAL"
            + " from ("
            + "    select"
            + "       ENTEXAMYEAR,APPLICANTDIV,EXAMNO"
            + "    from"
            + "    ENTEXAM_DESIRE_DAT"
            + "    where"
            + "        ENTEXAMYEAR = '"+_param._year+"' "
            + "    ) T1"
            + "    inner join ENTEXAM_APPLICANTBASE_DAT W1 ON"
            + "        T1.ENTEXAMYEAR  = W1.ENTEXAMYEAR and"			// 入試年度
            + "        T1.APPLICANTDIV = W1.APPLICANTDIV and"			// 入試制度
            + "        T1.EXAMNO       = W1.EXAMNO "					// 受験番号
            + " group by T1.EXAMNO"
            ;
        return rtn;
   }
    
    /**
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
                		nvlT(rs.getString("SCORE1")),
                		nvlT(rs.getString("SCORE2")),
                		nvlT(rs.getString("SCORE3")),
                		nvlT(rs.getString("SCORE4")),
                		nvlT(rs.getString("SCORE5"))
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
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '1' THEN SCORE ELSE null END) AS SCORE1,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '2' THEN SCORE ELSE null END) AS SCORE2,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '3' THEN SCORE ELSE null END) AS SCORE3,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '4' THEN SCORE ELSE null END) AS SCORE4,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '5' THEN SCORE ELSE null END) AS SCORE5"
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
     * @return					帳票出力対象データリスト
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
        stb.append("    select CASE WHEN 0 < MOD(COUNT(DISTINCT W1.EXAMNO),50) THEN COUNT(DISTINCT W1.EXAMNO)/50 + 1 ELSE COUNT(DISTINCT W1.EXAMNO)/50 END AS COUNT ");
        stb.append("    from   ENTEXAM_DESIRE_DAT W1");
        stb.append("    where");
        stb.append("    W1.ENTEXAMYEAR = '"+_param._year+"' ");
        if (!_param._testDiv.equals("0")) {
            stb.append(" AND W1.TESTDIV = '"+_param._testDiv+"' ");
        }
        stb.append(" ) T1");

        return stb.toString();
   }

    /** 生徒クラス */
    private class student {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _exam_type;
        final String _examno;
        final String _receptno;
        final String _examinee_div;
        final String _sex;
        final String _name;
        final String _specialmeasures;
        final String _entdiv;
        final String _remark1;
        final String _remark2;
        final String _procedurediv;
        final String _proceduredate;
        final String _judgediv;
        final String _procedurediv1;
        final String _proceduredate1;
        final String _prischool_name;

        student(
                final String entexamyear,
                final String applicantdiv,
                final String testdiv,
                final String exam_type,
                final String examno,
                final String receptno,
                final String examinee_div,
                final String sex,
                final String name,
                final String specialmeasures,
                final String entdiv,
                final String remark1,
                final String remark2,
                final String procedurediv,
                final String proceduredate,
                final String judgediv,
                final String procedurediv1,
                final String proceduredate1,
                final String prischool_name
        ) {
			_entexamyear   = entexamyear;
			_applicantdiv   = applicantdiv;
			_testdiv   = testdiv;
			_exam_type   = exam_type;
			_examno   = examno;
			_receptno   = receptno;
			_examinee_div   = examinee_div;
			_sex   = sex;
			_name   = name;
			_specialmeasures   = specialmeasures;
			_entdiv   = entdiv;
			_remark1   = remark1;
			_remark2   = remark2;
			_procedurediv   = procedurediv;
			_proceduredate   = proceduredate;
			_judgediv   = judgediv;
			_procedurediv1   = procedurediv1;
			_proceduredate1   = proceduredate1;
			_prischool_name   = prischool_name;
        }
    }

    /** 得点クラス */
    private class scoreData {
        final String _reccount;
        final String _score1;
        final String _score2;
        final String _score3;
        final String _score4;
        final String _score5;

        scoreData(
                final String reccount,
                final String score1,
                final String score2,
                final String score3,
                final String score4,
                final String score5
        ) {
        	_reccount = reccount;
        	_score1   = score1;
        	_score2   = score2;
        	_score3   = score3;
        	_score4   = score4;
        	_score5   = score5;
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
    	private final String _sort;
    	private final String _nendo;
    	private final String _date;
        private final String _testDiv;
        private HashMap _examCount = new HashMap();
        int _toralMan = 0;
        int _toralWoMan = 0;
        int _totalPage = 0;
        int _meisaiNo = 0;
        boolean _isInfluence;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _sort = nvlT(request.getParameter("SORT"));
            _nendo = KenjaProperties.gengou(Integer.parseInt(_year))+"年度";
            _date = KNJ_EditDate.h_format_JP(_loginDate);
            _testDiv = request.getParameter("TESTDIV");
            setInfluence(db2);
        }
        
        private void setInfluence(DB2UDB db2) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;
            ps = db2.prepareStatement("SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '01' ");
            rs = ps.executeQuery();
            if (rs.next()) {
                _isInfluence = "1".equals(rs.getString("NAMESPARE1"));
            }
            DbUtils.closeQuietly(null, ps, rs);
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
     * 日付を指定されたフォーマットに設定し文字列にして返す
     * @param s
     * @return
     */
    private String fomatSakuseiDate(String cnvDate, String sfmt, String chgfmt) {

    	String retDate = "";
    	try {
			DateFormat foramt = new SimpleDateFormat(sfmt); 
			//文字列よりDate型へ変換
			Date date1 = foramt.parse(cnvDate); 
			// 年月日のフォーマットを指定
			SimpleDateFormat sdf1 = new SimpleDateFormat(chgfmt);
			// Date型より文字列へ変換
			retDate = sdf1.format(date1);
		} catch( Exception e ){
			log.error("setHeader set error!");
		}
		return retDate;
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
