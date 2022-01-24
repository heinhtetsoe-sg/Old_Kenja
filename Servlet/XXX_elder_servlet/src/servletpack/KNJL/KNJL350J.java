package servletpack.KNJL;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
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
 * 総合判定資料（偏差値）
 * 
 * @author nakasone
 *
 */
public class KNJL350J {
    private static final String FORM_NAME = "KNJL350J.frm";
    private static final String FORM_NAME2 = "KNJL350J_2.frm";
    private static final String JUDGE_GOKAKU = "◎";
    private static final String JUDGE_FUGOKAKU = "×";
    private static final String JUDGE_MI = "-";
    private static final String JUDGE_KURIAGE = "◇";
    private static final String JUDGE_KI = "・";
    private static final int PAGE_MAXLINE = 50;
    private static final double LOW_VALUE = Double.MIN_VALUE;
	private	Calendar calw = Calendar.getInstance();
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL350J.class);
    
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
   		List sameStudent = createStudents(db2);

        if (_param._sort.equals("3")) { // 偏差値順/得点順でソートする
            SameStudent[] sameStudentArray = new SameStudent[sameStudent.size()];
            for(int i=0; i<sameStudent.size(); i++) {
                sameStudentArray[i] = (SameStudent) sameStudent.get(i);
            }
            if ("0".equals(_param._testDiv)) { // 画面の入試区分が '-全て-'を選択
                Arrays.sort(sameStudentArray, new SameStudentComparator2());
            } else {
                Arrays.sort(sameStudentArray, new SameStudentComparator1());
            }
            sameStudent = new ArrayList();
            for(int i=0; i<sameStudentArray.length; i++) {
                sameStudent.add(sameStudentArray[i]);
            }
        }

        // 総ページ数を取得を取得
   		getTotalPage(db2);
   		
   		// 見出し出力
   		printSvfHead(svf,page_cnt);
   		int passedApplcntAll=0, passedApplcntMan=0, passedApplcntWoman=0;
        int absentApplcntAll=0, absentApplcntMan=0, absentApplcntWoman=0;
        for (Iterator it = sameStudent.iterator(); it.hasNext();) {
            final SameStudent ssudent = (SameStudent) it.next();
            final student sudent = (student) ssudent._list.get(0);
            
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

                if("0".equals(_param._testDiv)) {
                    // ’-全て-’選択時は、志願した入試に全て欠席した志願者を欠席者とする
                    if (ssudent.isAllAbsence()) {
                        absentApplcntAll += 1;
                        if ("1".equals(sudent._sex)) absentApplcntMan += 1;
                        if ("2".equals(sudent._sex)) absentApplcntWoman += 1;
                    }
                }
            }
            
            // 入試区分別偏差値の出力
            boolean passed = false; // 前に行われた試験区分での合格フラグ
            boolean[] judgediv = new boolean[6+1];
            ssudent.sortData(); // 試験の行われた順番にソートする
            for(Iterator it2=ssudent._list.iterator(); it2.hasNext();) {
                student student = (student) it2.next();
                if (passed==false) {
                    if ("1".equals(student._testdiv)) {
                        passed = judgediv[5];
                    }else if ("2".equals(student._testdiv)){
                        passed = judgediv[5] || judgediv[1];
                    }else if ("3".equals(student._testdiv)) {
                        passed = judgediv[5] || judgediv[1] || judgediv[2];
                    }else if ("4".equals(student._testdiv)) {
                        passed = judgediv[5] || judgediv[1] || judgediv[2] || judgediv[3];
                    }else if ("6".equals(student._testdiv)) {
                        passed = judgediv[5] || judgediv[1] || judgediv[2] || judgediv[3] || judgediv[4];
                    }
                }
                if (student._testdiv.equals(_param._testDiv)) {
                    if (passed) {
                        passedApplcntAll += 1;
                        if ("1".equals(student._sex)) passedApplcntMan += 1;
                        if ("2".equals(student._sex)) passedApplcntWoman += 1;
                    }
                    if (student._examinee_div.equals("2")) {
                        absentApplcntAll += 1;
                        if ("1".equals(student._sex)) absentApplcntMan += 1;
                        if ("2".equals(student._sex)) absentApplcntWoman += 1;
                    }
                }

                outPutPrint2(db2, svf, student, passed, line);
                if (passed==false) {
                    int iTestdiv = Integer.valueOf(student._testdiv).intValue();
                    judgediv[iTestdiv] = "1".equals(student._judgediv);
                }
            }
        }
        

		if (breakExamNo != null) {
			// 合計数を設定
			int total_count = _param._toralMan + _param._toralWoMan;
            int realApplcntMan = _param._toralMan-passedApplcntMan;
            int realApplcntWoman = _param._toralWoMan-passedApplcntWoman;
            int realApplcntAll = total_count-passedApplcntAll;
            int examineeMan = _param._toralMan - absentApplcntMan;
            int examineeWoman = _param._toralWoMan - absentApplcntWoman;
            int examineeAll = total_count - absentApplcntAll;
            
            svf.VrsOut("NOTE" ,"実志願者数："+
                    "男"+String.valueOf(realApplcntMan)+"名,"+
                    "女"+String.valueOf(realApplcntWoman)+"名,"+
                    "合計"+String.valueOf(realApplcntAll)+"名 "+
                    "受験者数：男"+String.valueOf(examineeMan)+"名,女"+String.valueOf(examineeWoman)+"名,合計"
						+String.valueOf(examineeAll)+"名");
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
    	// NO
		svf.VrsOutn("NO" 		, line,		String.valueOf(_param._meisaiNo));
		// 受験番号
		svf.VrsOutn("EXAMNO1" 	, line,		sudent._examno);
		// 氏名
		svf.VrsOutn(setformatArea("NAME", sudent._name, 14, "1", "2")	, line,		sudent._name);
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
    private void outPutPrint2(final DB2UDB db2, final Vrw32alp svf, final student sudent, boolean passed, int line) throws Exception {
   	
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
            if (passed) {
                svf.VrsOutn("MARK"+i  , line, JUDGE_KI);  // 合格済
            } else {
                svf.VrsOutn("MARK"+i  , line, JUDGE_MI);  // 未受験
            }
		}
		if(sudent._specialmeasures.equals("1") && sudent._judgediv.equals("2")){
			svf.VrsOutn("MARK"+i	, line,	JUDGE_KURIAGE);	// 繰上合格
		}
		
		// 偏差値データ取得
   		final List scoreData = createscoreData(db2, sudent);
   		
		for (Iterator it = scoreData.iterator(); it.hasNext();) {
            final scoreData score = (scoreData) it.next();
            String std_score5 = "";
            String std_score4 = "";
            double score4 = 0;
            double score5 = 0;
            
            if(score._reccount.equals("0")){
            	return;
            }
            
            // 入試区分(Ａ１・Ａ２・Ｂ・Ｃ・Ｄ)	
       		if(sudent._testdiv.equals("1") || sudent._testdiv.equals("2") ||
       			sudent._testdiv.equals("3") || sudent._testdiv.equals("4") || sudent._testdiv.equals("6")){
                
       			// 国語
       			if(!score._stdscore2.equals("")){
           			svf.VrsOutn("SCORE"+i+"_1"	, line, getFormattedScoreOutput(score._stdscore2));
       			}
       			// 算数
       			if(!score._stdscore3.equals("")){
           			svf.VrsOutn("SCORE"+i+"_2"	, line, getFormattedScoreOutput(score._stdscore3));
       			}
       			// 理科
       			if(!score._stdscore5.equals("")){
  					std_score5 = getFormattedScoreOutput(score._stdscore5);
  					score5 = Double.valueOf(std_score5).doubleValue();
           			svf.VrsOutn("SCORE"+i+"_3"	, line, std_score5);
       			} else {
               		// 得点データが存在しない場合
       				score5 = LOW_VALUE;
       			}
       			// 社会
       			if(!score._stdscore4.equals("")){
  					std_score4 = getFormattedScoreOutput(score._stdscore4);
  	   				score4 = Double.valueOf(std_score4).doubleValue();
           			svf.VrsOutn("SCORE"+i+"_4"	, line, std_score4);
       			} else {
                   	// 得点データが存在しない場合
     				score4 = LOW_VALUE;
       			}
       			
       			// 選択科目が4科の場合のみ太字・アンダーラインの設定
       			if(sudent._exam_type.equals("2")){
           			if(sudent._judgeDeviationDiv.equals("1")){
       	   	       		// 理科、社会の偏差値を比較し高い科目に太字・アンダーラインの設定
       	   				if(score5 > score4){
       	  	   				svf.VrsOutn("FLG"+i+"_1"	, line, "1");
       	   				} else if(score5 < score4){
       	  	   				svf.VrsOutn("FLG"+i+"_2"	, line, "1");
       	   				} else {
    	   	  	   				svf.VrsOutn("FLG"+i+"_1"	, line, "1");
       	   				}
       	   			} else if(sudent._judgeDeviationDiv.equals("2")){
      	   				svf.VrsOutn("FLG"+i+"_1"	, line, "1");
      	   				svf.VrsOutn("FLG"+i+"_2"	, line, "1");
       	   			}
       			}
       		}
       		
            // 入試区分(帰国生)	
       		if(sudent._testdiv.equals("5")){
       			if(!score._stdscore2.equals("")){
           			// 国語
           			svf.VrsOutn("SCORE"+i+"_1"	, line, getFormattedScoreOutput(score._stdscore2));
       			}
       			if(!score._stdscore3.equals("")){
           			// 算数
           			svf.VrsOutn("SCORE"+i+"_2"	, line, getFormattedScoreOutput(score._stdscore3));
       			}
       			if(!score._stdscore1.equals("")){
           			// 英語
           			svf.VrsOutn("SCORE"+i+"_3"	, line, getFormattedScoreOutput(score._stdscore1));
       			}
       		}
    		// 判定
            if (_param._outData.equals("1")) {
                svf.VrsOutn("JUDGMENT"+i  , line, sudent._judgeDeviation);
            } else if (_param._outData.equals("2")) {
                svf.VrsOutn("JUDGMENT"+i  , line, sudent._total4);
            }
        }
    }
    
    /**
     * 偏差値/得点の文字列を出力用にフォーマットして返す
     * @return 偏差値/得点の文字列
     */
    private String getFormattedScoreOutput(String stdscore) {
        if (_param._outData.equals("1")) {
            return String.valueOf(KNJServletUtils.roundHalfUp(Double.valueOf(stdscore).doubleValue(), 1));
        } else if (_param._outData.equals("2")) {
            return Integer.valueOf(stdscore).toString();
        }
        return null;
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
		} else if(_param._sort.equals("2")){
            svf.VrsOut("SUBTITLE"   ,"塾名順");    // サブタイトル
		} else if(_param._sort.equals("3")){
            if (_param._outData.equals("1")){
                svf.VrsOut("SUBTITLE"   ,"偏差値順");    // サブタイトル
            } else if (_param._outData.equals("2")){
                svf.VrsOut("SUBTITLE"   ,"得点順");    // サブタイトル
            }
        }
        
        if (_param._outData.equals("1")) {
            svf.VrsOut( "DATA_DIV", "偏差値");
        } else if (_param._outData.equals("2")) {
            svf.VrsOut( "DATA_DIV", "得点");
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
                		nvlT(rs.getString("EXAMINEE_DIV")),
                		nvlT(rs.getString("SEX")),
                		nvlT(rs.getString("NAME")),
                		nvlT(rs.getString("SPECIAL_MEASURES")),
             			nvlT(rs.getString("ENTDIV")),
             			nvlT(rs.getString("REMARK1")),
             			nvlT(rs.getString("REMARK2")),
             			nvlT(rs.getString("PROCEDUREDIV")),
             			nvlT(rs.getString("PROCEDUREDATE")),
                		nvlT(rs.getString("RECEPTNO")),
             			nvlT(rs.getString("JUDGEDIV")),
             			nvlT(rs.getString("PROCEDUREDIV1")),
             			nvlT(rs.getString("PROCEDUREDATE1")),
             			nvlT(rs.getString("JUDGE_DEVIATION")),
             			nvlT(rs.getString("JUDGE_DEVIATION_DIV")),
                        nvlT(rs.getString("TOTAL4")),
             			nvlT(rs.getString("PRISCHOOL_NAME"))
                );
                
                SameStudent tempSameStudent = null;
                for(Iterator it=rtnList.iterator(); it.hasNext();) {
                    SameStudent ssudent = (SameStudent) it.next();
                    if (ssudent.isSame(student)) {
                        tempSameStudent = ssudent;
                    }
                }

                if (tempSameStudent == null) {
                    tempSameStudent = new SameStudent(student._entexamyear,student._applicantdiv, student._examno);
                    rtnList.add(tempSameStudent);
                }
                
                tempSameStudent._list.add(student);
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
        stb.append("        W3.JUDGE_DEVIATION,");
        stb.append("        W3.JUDGE_DEVIATION_DIV,");
        stb.append("        W3.TOTAL4,");
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
        	stb.append(" W1.EXAMNO, W4.PRISCHOOL_NAME");   // 受験番号順
        } else if(_param._sort.equals("2")){
            stb.append(" W4.PRISCHOOL_NAME, W1.EXAMNO");    // 塾名順
        } else if(_param._sort.equals("3")){               
            stb.append(" W1.EXAMNO ");   // 偏差値/得点順 (SQLではなくプログラムでソートする)
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
        String outputDataField = "";
        if (_param._outData.equals("1")) {
            outputDataField = "STD_SCORE";
        } else if (_param._outData.equals("2")) {
            outputDataField = "SCORE";
        }

        try {
            while (rs.next()) {
                final scoreData scoreData = new scoreData(
                		rs.getString("REC_COUNT"),
                		nvlT(rs.getString(outputDataField+"1")),
                		nvlT(rs.getString(outputDataField+"2")),
                		nvlT(rs.getString(outputDataField+"3")),
                		nvlT(rs.getString(outputDataField+"4")),
                		nvlT(rs.getString(outputDataField+"5"))
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
     * 偏差値取得SQL生成
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
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '5' THEN SCORE ELSE null END) AS SCORE5,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '1' THEN STD_SCORE ELSE null END) AS STD_SCORE1,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '2' THEN STD_SCORE ELSE null END) AS STD_SCORE2,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '3' THEN STD_SCORE ELSE null END) AS STD_SCORE3,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '4' THEN STD_SCORE ELSE null END) AS STD_SCORE4,"
            + "    MAX(CASE WHEN TESTSUBCLASSCD = '5' THEN STD_SCORE ELSE null END) AS STD_SCORE5"
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
    private class student implements Comparable {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _exam_type;
        final String _examno;
        final String _examinee_div;
        final String _sex;
        final String _name;
        final String _specialmeasures;
        final String _entdiv;
        final String _remark1;
        final String _remark2;
        final String _procedurediv;
        final String _proceduredate;
        final String _receptno;
        final String _judgediv;
        final String _procedurediv1;
        final String _proceduredate1;
        final String _judgeDeviation;
        final String _judgeDeviationDiv;
        final String _total4;
        final String _prischool_name;

        student(
                final String entexamyear,
                final String applicantdiv,
                final String testdiv,
                final String exam_type,
                final String examno,
                final String examinee_div,
                final String sex,
                final String name,
                final String specialmeasures,
                final String entdiv,
                final String remark1,
                final String remark2,
                final String procedurediv,
                final String proceduredate,
                final String receptno,
                final String judgediv,
                final String procedurediv1,
                final String proceduredate1,
                final String judgeDeviation,
                final String judgeDeviationDiv,
                final String total4,
                final String prischool_name
        ) {
			_entexamyear   = entexamyear;
			_applicantdiv   = applicantdiv;
			_testdiv   = testdiv;
			_exam_type   = exam_type;
			_examno   = examno;
			_examinee_div   = examinee_div;
			_sex   = sex;
			_name   = name;
			_specialmeasures   = specialmeasures;
			_entdiv   = entdiv;
			_remark1   = remark1;
			_remark2   = remark2;
			_procedurediv   = procedurediv;
			_proceduredate   = proceduredate;
			_receptno   = receptno;
			_judgediv   = judgediv;
			_procedurediv1   = procedurediv1;
			_proceduredate1   = proceduredate1;
			_judgeDeviation   = judgeDeviation;
			_judgeDeviationDiv   = judgeDeviationDiv;
            _total4 = total4;
			_prischool_name   = prischool_name;
        }
        
        /*
         * 試験の実行された順(入試区分が5, 1, 2, 3, 4の順)にソートするために使用する
         */
        public int compareTo(Object o) {
            if (!(o instanceof student))
                return -1;
            
            student other = (student) o;
            Integer zero = new Integer(0);
            Integer testdiv = Integer.valueOf(_testdiv);
            if (testdiv.intValue()==5) testdiv = zero; 
            Integer otherTestdiv = Integer.valueOf(other._testdiv);
            if (otherTestdiv.intValue()==5) otherTestdiv = zero;
            return testdiv.compareTo(otherTestdiv);
        }
        
        public Double getJudgeDeviation() {
            return (_judgeDeviation == null || "".equals(_judgeDeviation)) 
            ? new Double(0.0): Double.valueOf(_judgeDeviation);
        }
        
        public Integer getTotal4() {
            return (_total4== null || "".equals(_total4)) 
            ? new Integer(0): Integer.valueOf(_total4);
        }
    }
    
    /**
     * _param._sort.equals("3") (偏差値順/得点順でソート)のとき使用する
     */
    private class SameStudentComparator1 implements Comparator {
        public int compare(Object o1,Object o2) {
            if (!(o1 instanceof SameStudent)) return 0;
            if (!(o2 instanceof SameStudent)) return 0;
            SameStudent ss1 = (SameStudent) o1;
            SameStudent ss2 = (SameStudent) o2;

            student s1 = ss1.getStudentInTestdiv();
            if (s1==null) return 0;
            student s2 = ss2.getStudentInTestdiv();
            if (s2==null) return 0;

            if (_param._outData.equals("1")) { // 偏差値でソート
                return -s1.getJudgeDeviation().compareTo(s2.getJudgeDeviation());
            } else if(_param._outData.equals("2")) { // 点数でソート
                return -s1.getTotal4().compareTo(s2.getTotal4());
            }
            return 0;
        }
    }

    /**
     * _param._sort.equals("3") (偏差値順/得点順でソート)かつ
     * _param._testdiv.equals("0") ('-全て-'選択)のとき使用する
     */
    private class SameStudentComparator2 implements Comparator {
        public int compare(Object o1,Object o2) {
            if (!(o1 instanceof SameStudent)) return 0;
            if (!(o2 instanceof SameStudent)) return 0;
            SameStudent ss1 = (SameStudent) o1;
            SameStudent ss2 = (SameStudent) o2;

            if (_param._outData.equals("1")) { // 偏差値でソート
                return -ss1.getBestJudgeDeviation().compareTo(ss2.getBestJudgeDeviation());
            } else if(_param._outData.equals("2")) { // 点数でソート
                return -ss1.getBestTotal4().compareTo(ss2.getBestTotal4());
            }
            return 0;
        }
    }

    /** 同一のstudentをリストで保持する*/
    private class SameStudent {
        final String _entexamyear;
        final String _applicantdiv;
        final String _examno;
        ArrayList _list;
        SameStudent(String entexamyear, String applicantdiv, String examno) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _examno = examno;
            _list = new ArrayList();
        }
        /**
         * 対象となる生徒が同一の入試年度、入試制度、受験番号のときtrueを返す
         * @param sudent 対象となる生徒
         * @return 対象となる生徒が同一の入試年度、入試制度、受験番号のときtrueを返す
         */
        boolean isSame(student sudent) {
            return _entexamyear.equals(sudent._entexamyear)
            && _applicantdiv.equals(sudent._applicantdiv)
            && _examno.equals(sudent._examno);
        }
        
        public void sortData() {
            // データを試験の行われた順番にソートする
            student[] students = new student[_list.size()];
            for(int i=0; i<_list.size(); i++)
                students[i] = (student) _list.get(i);
            Arrays.sort(students);
            _list = new ArrayList();
            for(int i=0; i<students.length; i++)
                _list.add(students[i]);
        }

        /** 画面と同じ試験区分の試験データの生徒を取得する */
        public student getStudentInTestdiv() {
            for (Iterator it=_list.iterator(); it.hasNext();) {
                student s = (student) it.next();
                if (_param._testDiv.equals(s._testdiv)) { 
                    return s;
                }
            }
            return null;
        }

        /** total4の最高点を返す */
        public Integer getBestTotal4() {
            if (_list==null) return null;
            Integer bestTotal4 = new Integer(0);
            for (Iterator it=_list.iterator(); it.hasNext();) {
                student s = (student) it.next();
                if (s.getTotal4().compareTo(bestTotal4) > 0)
                    bestTotal4 = s.getTotal4();
            }
            return bestTotal4;
        }

        /** judgeDeviationの最高点を返す */
        public Double getBestJudgeDeviation() {
            if (_list==null) return null;
            Double bestJudgeDeviation = new Double(0.0);
            for (Iterator it=_list.iterator(); it.hasNext();) {
                student s = (student) it.next();
                if (s.getJudgeDeviation().compareTo(bestJudgeDeviation) > 0)
                    bestJudgeDeviation = s.getJudgeDeviation();
            }
            return bestJudgeDeviation;
        }

        /** 志願した入試を全て欠席した場合trueを返す*/
        public boolean isAllAbsence() {
            boolean isAllAbsence = true;
            for(Iterator it=_list.iterator(); it.hasNext();) {
                student sudent = (student) it.next();
                isAllAbsence &= "2".equals(sudent._examinee_div);
            }
            return isAllAbsence;
        }
    }

    /** 得点クラス */
    private class scoreData {
        final String _reccount;
        final String _stdscore1;
        final String _stdscore2;
        final String _stdscore3;
        final String _stdscore4;
        final String _stdscore5;

        scoreData(
                final String reccount,
                final String stdscore1,
                final String stdscore2,
                final String stdscore3,
                final String stdscore4,
                final String stdscore5
        ) {
        	_reccount = reccount;
        	_stdscore1   = stdscore1;
        	_stdscore2   = stdscore2;
        	_stdscore3   = stdscore3;
        	_stdscore4   = stdscore4;
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
    	private final String _sort;
    	private final String _nendo;
    	private final String _date;
        private final String _testDiv;
        private final String _outData;
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
            _sort = nvlT(request.getParameter("SORT")); // 1:受験番号順, 2:塾名順, 3:偏差値/得点
            _nendo = KenjaProperties.gengou(Integer.parseInt(_year))+"年度";
            _date = KNJ_EditDate.h_format_JP(_loginDate);
            _testDiv = request.getParameter("TESTDIV");
            _outData = request.getParameter("OUT_DATA"); // 1:偏差値, 2:得点
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
