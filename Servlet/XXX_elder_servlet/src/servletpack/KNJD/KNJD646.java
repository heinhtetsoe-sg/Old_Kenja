package servletpack.KNJD;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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


/**
 * 成績一覧表
 * 
 * @author nakasone
 *
 */
public class KNJD646 {
	
    private static final String FORM_NAME = "KNJD646.frm";
    private boolean _hasData;
    Param _param;
    private static final Log log = LogFactory.getLog(KNJD646.class);
    /** 成績一覧表(最大行数) */
    private static final int maxgyo = 50;

    // 性別
    private static final String sex_type_Man  = "男";
    private static final String sex_type_Woman  = "女";
    
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

		for (int i=0 ; i<_param._categorySelected.length ; i++) {
	        	
            // 学年を取得
			String sGrade_code = _param._categorySelected[i].substring(0,2);
			
            // クラスを取得
			String sClass_code = _param._categorySelected[i].substring(2,5);
			
			// 各科目毎の得点データ 及び 学年順位の取得
			final List studentKamoku = createStudentKamoku(db2, sGrade_code, sClass_code);
			
			// 帳票出力のメソッド
	        outPutPrint(db2, svf, studentKamoku, sGrade_code, sClass_code, i);
        }
    }
    
    /**
     * 帳票出力処理
     * @param db2	ＤＢ接続オブジェクト
     * @param svf				帳票オブジェクト
     * @param studentCommon	帳票出力対象クラスオブジェクト
     * @param sGrade_code		学年コード
     * @param sClass_code		クラスコード
     * @param outcount			帳票出力行数
     * @param irow				クラス毎の帳票出力行数
     * @return		対象データ存在フラグ(有り：TRUE、無し：FALSE)
     */
    private void outPutPrint(
    	final DB2UDB db2,
    	final Vrw32alp svf, 
    	final List studentCommon, 
    	final String sGrade_code, 
    	final String sClass_code, 
    	int irow)
    	throws Exception {

        String schregno = null;

        _param.setCountinit();
        
        svf.VrSetForm(FORM_NAME, 1);

    	// 成績一覧表見出し印刷
        printSvfMeiboHead(db2, svf, sGrade_code, sClass_code);

        for (Iterator it = studentCommon.iterator(); it.hasNext();) {
            final studentKamoku sudentkamoku = (studentKamoku) it.next();
            
            // 学籍番号のブレイク
            if(schregno == null || !schregno.equals(sudentkamoku._schregno)){
            	
                if( maxgyo == _param.outcount ){
                    svf.VrEndPage();
                    _param.outcount = 0;
                	// 成績一覧表見出し印刷
                    printSvfMeiboHead(db2, svf, sGrade_code, sClass_code);
                }
                _param.outcount++;
                // 学籍番号毎の出力
                printSvfMeiboOut1(db2, svf, sudentkamoku._schregno, sGrade_code, sClass_code, _param.outcount);
                schregno = sudentkamoku._schregno;
            }
            // 各科目の得点 及び 学年順位の出力
            printSvfMeiboOut2(svf, sudentkamoku, _param.outcount);
        }

        // フッタ部を印刷
        if( schregno != null ){
            printTotalNum(svf);
            svf.VrEndPage();
            _param.outcount = 0;
            _hasData = true;
        }
    }
    
	/**
     *  成績一覧表 学籍番号毎のデータ出力
     * @param db2	ＤＢ接続オブジェクト
     * @param svf				帳票オブジェクト
     * @param sSchregno		学籍番号
     * @param sGrade_code		学年コード
     * @param sClass_code		クラスコード
     * @param irow				帳票出力行数
     */
    private void printSvfMeiboOut1(
    	final DB2UDB db2, 
    	Vrw32alp svf,
    	String sSchregno,
    	final String sGrade_code, 
    	final String sClass_code, 
    	int irow)
    	throws SQLException {
		
    	PreparedStatement ps = null;
		ResultSet rs = null;

    	try {
        	ps = db2.prepareStatement( sqlStudent(sSchregno, sGrade_code, sClass_code) );
			rs = ps.executeQuery();
            while (rs.next()) {
            	// 学年が6年且つ理系の場合、成績一覧表の右側欄の編集を行う
                if(nvlT(rs.getString("COURSE_DIV")).equals("2")){
                	// 合計
        			if(rs.getString("SCORE2") != null){
            			svf.VrsOutn("RI_TOTALPOINT",	irow , rs.getString("SCORE2"));
        			}
                	// 出席番号
        			int iAttendno = Integer.valueOf(rs.getString("ATTENDNO")).intValue();
        			svf.VrsOutn("RI_NUMBER",		irow , String.valueOf(iAttendno));
                	// 氏名
        			svf.VrsOutn(setformatArea("RI_NAME", rs.getString("NAME")) ,irow, rs.getString("NAME"));
                	// 性別
        			if(nvlT(rs.getString("SEX")).equals("1")){
            			svf.VrsOutn("RI_SEX",		irow , sex_type_Man);
        			}
        			if(nvlT(rs.getString("SEX")).equals("2")){
            			svf.VrsOutn("RI_SEX",		irow , sex_type_Woman);
        			}
        			// 統計：合計得点(理系)
        			if(rs.getString("SCORE2") != null){
                        _param.ri_total_point += Integer.valueOf(nvlT(rs.getString("SCORE2"))).intValue();
        			}
        			// 統計：人数
                    ++_param.ri_total_cnt;
                } else {
                // 学年が6年以外 又は 6年且つ文系の場合、成績一覧表の左側欄の編集を行う
//                if(nvlT(rs.getString("COURSE_DIV")).equals("0") || nvlT(rs.getString("COURSE_DIV")).equals("1")){
                    // 合計
                    if(rs.getString("SCORE2") != null){
                        svf.VrsOutn("BUN_TOTALPOINT",   irow , rs.getString("SCORE2"));
                    }
                    // 出席番号
                    int iAttendno = Integer.valueOf(rs.getString("ATTENDNO")).intValue();
                    svf.VrsOutn("BUN_NUMBER",       irow , String.valueOf(iAttendno));
                    // 氏名
                    svf.VrsOutn(setformatArea("BUN_NAME", rs.getString("NAME")) ,irow, rs.getString("NAME"));
                    // 性別
                    if(nvlT(rs.getString("SEX")).equals("1")){
                        svf.VrsOutn("BUN_SEX",      irow , sex_type_Man);
                    }
                    if(nvlT(rs.getString("SEX")).equals("2")){
                        svf.VrsOutn("BUN_SEX",      irow , sex_type_Woman);
                    }
                    // 統計：合計得点(文系)
                    if(rs.getString("SCORE2") != null){
                        _param.bun_total_point += Integer.valueOf(nvlT(rs.getString("SCORE2"))).intValue();
                    }
                    // 統計：人数
                    ++_param.bun_total_cnt;
                }
            }
        } finally {
            db2.commit();
    		DbUtils.closeQuietly(null, ps, rs);
        }

    }
    	

	/**
     *  成績一覧表 科目別の出力
     * @param svf	帳票オブジェクト
     * @param irow	帳票出力行数
     */
    private void printSvfMeiboOut2(Vrw32alp svf, studentKamoku sudentkamoku, int irow) {

    	// 学年順位
		svf.VrsOutn("GRADE_RANK",	irow , sudentkamoku._rank2);
		
		if(sudentkamoku._score != null) {
	    	// 得点(国語)
			if(sudentkamoku._namecd2.equals("01")){
				svf.VrsOutn("POINT1",	irow , sudentkamoku._score);
				_param.koku_total_point += Integer.valueOf(sudentkamoku._score).intValue();	// 統計：得点
				++_param.koku_total_cnt;	// 統計：人数
			}
	    	// 得点(社会)
			if(sudentkamoku._namecd2.equals("02")){
				svf.VrsOutn("POINT2",	irow , sudentkamoku._score);
				_param.sya_total_point += Integer.valueOf(sudentkamoku._score).intValue();	// 統計：得点
				++_param.sya_total_cnt;	// 統計：人数
			}
	    	// 得点(英語)
			if(sudentkamoku._namecd2.equals("03")){
				svf.VrsOutn("POINT3",	irow , sudentkamoku._score);
				_param.ei_total_point += Integer.valueOf(sudentkamoku._score).intValue();	// 統計：得点
				++_param.ei_total_cnt;	// 統計：人数
			}
	    	// 得点(数学)
			if(sudentkamoku._namecd2.equals("04")){
				svf.VrsOutn("POINT4",	irow , sudentkamoku._score);
				_param.su_total_point += Integer.valueOf(sudentkamoku._score).intValue();	// 統計：得点
				++_param.su_total_cnt;	// 統計：人数
			}
	    	// 得点(理科)
			if(sudentkamoku._namecd2.equals("05")){
				svf.VrsOutn("POINT5",	irow , sudentkamoku._score);
				_param.rika_total_point += Integer.valueOf(sudentkamoku._score).intValue();	// 統計：得点
				++_param.rika_total_cnt;	// 統計：人数
			}
		}
	}
    
    /**
     * 成績一覧表見出しの出力を行う
     * @param db2			ＤＢ接続オブジェクト
     * @param svf			帳票オブジェクト
     * @param sGrade_code	学年コード
     * @param sClass_code	クラスコード
     */
    private void printSvfMeiboHead(
       	final DB2UDB db2,
       	final Vrw32alp svf, 
       	final String sGrade_code, 
    	final String sClass_code)
    	throws Exception {
        
        // 日付を設定
        svf.VrsOut("DATE", fomatSakuseiDate(_param._loginDate));
        // 年度を設定
        svf.VrsOut("NENDO", _param._nendo);
        // 学年を設定
    	svf.VrsOut("GRADE", convZenkakuToHankaku(sGrade_code.substring(1,2)));
        // クラスを設定
        svf.VrsOut("HR_CLASS", convZenkakuToHankaku(sClass_code.substring(2,3)));

        // 担任名を取得
        final String sql = sqlStaffMst(sGrade_code, sClass_code);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
            	// 担任名を設定
            	svf.VrsOut("STAFF_NAME", rs.getString("STAFFNAME"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
    }
	/**
     * 統計データの出力
     * @param svf			帳票オブジェクト
     */
    private void printTotalNum(Vrw32alp svf)
	{
    	// 各合計得点欄
 		svf.VrsOutn("BUN_TOTALPOINT",	51, String.valueOf(_param.bun_total_point));	//文系(合計得点)
 		svf.VrsOutn("RI_TOTALPOINT",	51, String.valueOf(_param.ri_total_point));		//理系(合計得点)
 		svf.VrsOutn("POINT1",			51, String.valueOf(_param.koku_total_point));	//国語(合計得点)
 		svf.VrsOutn("POINT2",			51, String.valueOf(_param.sya_total_point));	//社会(合計得点)
 		svf.VrsOutn("POINT3",			51, String.valueOf(_param.ei_total_point));		//英語(合計得点)
 		svf.VrsOutn("POINT4",			51, String.valueOf(_param.su_total_point));		//数学(合計得点)
 		svf.VrsOutn("POINT5",			51, String.valueOf(_param.rika_total_point));	//理科(合計得点)
    	// 各合計人数欄
 		svf.VrsOutn("BUN_TOTALPOINT",	52, String.valueOf(_param.bun_total_cnt));		//文系(合計得点)
 		svf.VrsOutn("RI_TOTALPOINT",	52, String.valueOf(_param.ri_total_cnt));		//理系(合計得点)
 		svf.VrsOutn("POINT1",			52, String.valueOf(_param.koku_total_cnt));		//国語(合計得点)
 		svf.VrsOutn("POINT2",			52, String.valueOf(_param.sya_total_cnt));		//社会(合計得点)
 		svf.VrsOutn("POINT3",			52, String.valueOf(_param.ei_total_cnt));		//英語(合計得点)
 		svf.VrsOutn("POINT4",			52, String.valueOf(_param.su_total_cnt));		//数学(合計得点)
 		svf.VrsOutn("POINT5",			52, String.valueOf(_param.rika_total_cnt));		//理科(合計得点)
 		// 各平均点欄
        svf.VrsOutn("BUN_TOTALPOINT",   53,
       		String.valueOf((float)Math.round( (float)_param.bun_total_point / (float)_param.bun_total_cnt * 10) / 10));
        svf.VrsOutn("RI_TOTALPOINT",   53,
           		String.valueOf((float)Math.round( (float)_param.ri_total_point / (float)_param.ri_total_cnt * 10) / 10));
        svf.VrsOutn("POINT1",   53,
           		String.valueOf((float)Math.round( (float)_param.koku_total_point / (float)_param.koku_total_cnt * 10) / 10));
        svf.VrsOutn("POINT2",   53,
           		String.valueOf((float)Math.round( (float)_param.sya_total_point / (float)_param.sya_total_cnt * 10) / 10));
        svf.VrsOutn("POINT3",   53,
           		String.valueOf((float)Math.round( (float)_param.ei_total_point / (float)_param.ei_total_cnt * 10) / 10));
        svf.VrsOutn("POINT4",   53,
           		String.valueOf((float)Math.round( (float)_param.su_total_point / (float)_param.su_total_cnt * 10) / 10));
        svf.VrsOutn("POINT5",   53,
           		String.valueOf((float)Math.round( (float)_param.rika_total_point / (float)_param.rika_total_cnt * 10) / 10));
        
	}

    /**
     * 学籍番号毎のデータ取得
     * @param sSchregno	学籍番号
     * @param sGrade_code	学年コード
     * @param sClass_code	クラスコード
     * @return				SQL文字列
     * @throws Exception
     */
    private String sqlStudent(final String sSchregno, final String sGrade_code, final String sClass_code) {

    	final String rtn;
    	String sqlWhere_CourseDiv = "";
    	
    	if(sGrade_code.equals("06")){
    		// 学年が6年の場合、コース区分は'1'(文系)または'2'(理系)
    		sqlWhere_CourseDiv = " W2.COURSE_DIV IN('1','2') and ";
    	} else {
    		// 学年が6年以外の場合、コース区分は'0'(使用なし)
    		sqlWhere_CourseDiv = " W2.COURSE_DIV = '0' and ";
    	}

    	
    	rtn = " select"
            + "    W1.ATTENDNO,"
            + "    W2.COURSE_DIV,"
            + "    W2.SCORE2,"
            + "    W3.NAME,"
            + "    W3.SEX"
            + " from"
            + "    SCHREG_REGD_DAT W1"
            + "    LEFT JOIN RECORD_MOCK_RANK_DAT W2 ON"
            + "          W1.YEAR = W2.YEAR and"
            + "          W1.SCHREGNO = W2.SCHREGNO and"
            + "          W2.DATA_DIV = '0' and"
            +            sqlWhere_CourseDiv
            + "          W2.SUBCLASSCD = '333333' "
            + "    INNER JOIN SCHREG_BASE_MST W3 ON"
            + "          W1.SCHREGNO = W3.SCHREGNO"
            + " where"
            + "    W1.YEAR = '" + _param._year + "' and"
            + "    W1.SEMESTER = '" + _param._semester + "' and"
            + "    W1.GRADE = '" + sGrade_code + "' and"
            + "    W1.HR_CLASS = '" + sClass_code + "' and"
            + "    W1.SCHREGNO = '" + sSchregno + "'"
            ;
        return rtn;
    	
    }
    
    /**
     * 各科目毎の得点データ 及び 学年順位の取得
     * @param db2			ＤＢ接続オブジェクト
     * @param sGrade_code	学年コード
     * @param sClass_code	クラスコード
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudentKamoku(final DB2UDB db2, final String sGrade_code, final String sClass_code) throws Exception {
    	
        final List rtnList = new ArrayList();
        final String sql = sqlStudentKamoku(sGrade_code, sClass_code);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final studentKamoku studentKamoku = new studentKamoku(rs.getString("YEAR"),
                        rs.getString("SCHREGNO"),
                        rs.getString("SEMESTER"),
                        rs.getString("GRADE"),
                        rs.getString("HR_CLASS"),
                        rs.getString("RANK2"),
                        rs.getString("MOCK_SUBCLASS_CD"),
                        rs.getString("SCORE"),
                        rs.getString("NAMECD2")
                );
                rtnList.add(studentKamoku);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    
    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @param sClass_code  クラスコード
     * @return				SQL文字列
     * @throws Exception
     */
    private String sqlStudentKamoku(final String sGrade_code, final String sClass_code) throws Exception {

        final String rtn;
        rtn = " select"
            + "    W1.YEAR,"
            + "    W1.SCHREGNO,"
            + "    W1.SEMESTER,"
            + "    W1.GRADE,"
            + "    W1.HR_CLASS,"
            + "    W2.RANK2,"
            + "    W3.MOCK_SUBCLASS_CD,"
            + "    W3.SCORE,"
            + "    T1.NAMECD2"
            + " from"
            + "    SCHREG_REGD_DAT W1"
            + "    LEFT JOIN RECORD_MOCK_RANK_DAT W2 ON"
            + "          W1.YEAR = W2.YEAR and"
            + "          W1.SCHREGNO = W2.SCHREGNO and"
            + "          W2.DATA_DIV = '0' and"
            + "          W2.COURSE_DIV = '3' and"
            + "          W2.SUBCLASSCD = '333333'"
            + "    LEFT JOIN MOCK_DAT W3 ON"
            + "          W1.YEAR = W3.YEAR and"
            + "          W1.SCHREGNO = W3.SCHREGNO and"
            + "          SUBSTR(W3.MOCKCD,1,1) = '2'"
            + "    LEFT JOIN V_NAME_MST T1 ON"
            + "          W3.MOCK_SUBCLASS_CD = T1.NAME1 and"
            + "          T1.NAMECD1 = 'D007' and"
            + "          T1.YEAR = '" + _param._year + "'"
            + " where"
            + "    W1.YEAR = '" + _param._year + "' and"
            + "    W1.SEMESTER = '" + _param._semester + "' and"
            + "    W1.GRADE = '" + sGrade_code + "' and"
            + "    W1.HR_CLASS = '" + sClass_code + "'"
            + " ORDER BY W1.ATTENDNO, W2.RANK2, W2.SCHREGNO "
            ;
        return rtn;
    }
    
    /** 各科目データクラス */
    private class studentKamoku {
        final String _year;
        final String _schregno;
        final String _semester;
        final String _grade;
        final String _hr_class;
        final String _rank2;
        final String _mock_subclass_cd;
        final String _score;
        final String _namecd2;

        studentKamoku(
                final String year,
                final String schregno,
                final String semester,
                final String grade,
                final String hr_class,
                final String rank2,
                final String mock_subclass_cd,
                final String score,
                final String namecd2
        ) {
        	_year = year;
        	_schregno = schregno;
        	_semester = semester;
        	_grade = grade;
        	_hr_class = hr_class;
        	_rank2 = rank2;
        	_mock_subclass_cd = mock_subclass_cd;
        	_score = score;
        	_namecd2 = namecd2;
        }
    }
    
    /**
     * 担任名抽出ＳＱＬ生成処理
     * @param sGrade_code  学年コード
     * @param sClass_code  クラスコード
     * @return				SQL文字列
     * @throws Exception
     */
    private String sqlStaffMst(final String sGrade_code, final String sClass_code) throws Exception {

        final String rtn;
        rtn = " select"
            + "    T1.STAFFNAME"
            + " from"
            + "    SCHREG_REGD_HDAT W1"
            + "    LEFT JOIN STAFF_MST T1 ON"
            + "          W1.TR_CD1 = T1.STAFFCD "
            + " where"
            + "    W1.YEAR = '" + _param._year + "' and"
            + "    W1.SEMESTER = '" + _param._semester + "' and"
            + "    W1.GRADE = '" + sGrade_code + "' and"
            + "    W1.HR_CLASS = '" + sClass_code + "' "
            ;
        return rtn;
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
    	private final String _year;
    	private final String _semester;
    	private final String _programid;
    	private final String _loginDate;
    	private final String _grade;
    	private final String[] _categorySelected;
    	private final String _nendo;
    	
        private int outcount = 0;				// 帳票出力件数
    	private int bun_total_point = 0;		// 文系：合計得点
    	private int bun_total_cnt = 0;			// 文系：合計人数
    	private int bun_avg = 0;				// 文系：平均点
    	private int ri_total_point = 0;		// 理系：合計得点
    	private int ri_total_cnt = 0;			// 理系：合計人数
    	private int ri_avg = 0;				// 理系：平均点
    	private int koku_total_point = 0;		// 国語：合計得点
    	private int koku_total_cnt = 0;		// 国語：合計人数
    	private int koku_avg = 0;				// 国語：平均点
    	private int sya_total_point = 0;		// 社会：合計得点
    	private int sya_total_cnt = 0;			// 社会：合計人数
    	private int sya_avg = 0;				// 社会：平均点
    	private int ei_total_point = 0;		// 英語：合計得点
    	private int ei_total_cnt = 0;			// 英語：合計人数
    	private int ei_avg = 0;				// 英語：平均点
    	private int su_total_point = 0;		// 数学：合計得点
    	private int su_total_cnt = 0;			// 数学：合計人数
    	private int su_avg = 0;				// 数学：平均点
    	private int rika_total_point = 0;		// 理科：合計得点
    	private int rika_total_cnt = 0;		// 理科：合計人数
    	private int rika_avg = 0;				// 理科：平均点
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _programid = request.getParameter("PRGID");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
    		String sNENDO = convZenkakuToHankaku(_year);
            _nendo = sNENDO + "年度";
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
        }
        
        
        private void setCountinit() {
        	bun_total_point = 0;
        	bun_total_cnt = 0;
        	bun_avg = 0;
        	ri_total_point = 0;
        	ri_total_cnt = 0;
        	ri_avg = 0;
        	koku_total_point = 0;
        	koku_total_cnt = 0;
        	koku_avg = 0;
        	sya_total_point = 0;
        	sya_total_cnt = 0;
        	sya_avg = 0;
        	ei_total_point = 0;
        	ei_total_cnt = 0;
        	ei_avg = 0;
        	su_total_point = 0;
        	su_total_cnt = 0;
        	su_avg = 0;
        	rika_total_point = 0;
        	rika_total_cnt = 0;
        	rika_avg = 0;
        }

    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (!_hasData) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "note");
            svf.VrEndPage();
        }
        if (null != svf) {
            svf.VrQuit();
        }
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

    /**
     * 帳票に設定する文字数が10文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name	帳票出力エリア
     * @param sval			値
     * @return
     */
    private String setformatArea(String area_name, String sval) {

    	String retAreaName = "";
		// 値がnullの場合はnullが返される
    	if (sval == null) {
			return null;
		}
    	// 設定値が10文字超の場合、帳票設定エリアの変更を行う
    	if(10 >= sval.length()){
   			retAreaName = area_name + "1";
    	} else {
   			retAreaName = area_name + "2";
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
			return val.trim();
		}
	}
    
}
