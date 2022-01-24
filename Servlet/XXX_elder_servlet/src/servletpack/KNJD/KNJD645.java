package servletpack.KNJD;

import java.io.IOException;
import java.util.ArrayList;
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
 * 推薦名簿
 * 
 * @author nakasone
 *
 */
public class KNJD645 {
    private static final String FORM_NAME = "KNJD645.frm";
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJD645.class);
    
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
        
		// 学校コード取得
        String sourse_div = getSourse_div(db2);
        
		for (int i=0 ; i<_param._categorySelected.length ; i++) {
	        	
            // 学部コード取得
			String sbu_code = _param._categorySelected[i].substring(0,2); 
            // 学科コード取得
			String ska_code = _param._categorySelected[i].substring(2,3);
			
			// 推薦名簿データ取得
			final List student = createStudents(db2, sbu_code, ska_code, sourse_div);
			
			// 帳票出力のメソッド
	        outPutPrint(svf, student, i);
        }
    }
    
    /**
     * 帳票出力処理
     * @param svf		帳票オブジェクト
     * @param student	帳票出力対象クラスオブジェクト
     * @param irow		帳票出力行数
     * @return		対象データ存在フラグ(有り：TRUE、無し：FALSE)
     */
    private void outPutPrint(final Vrw32alp svf, final List student, int irow) {
        int line = 1;

        svf.VrSetForm(FORM_NAME, 1);

        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();

            if(line == 1){
		        // ヘッダ・フッタ部出力
				setHead(svf, sudent);
	        }
	        // 名前
			svf.VrsOutn(setformatArea("NAME1", sudent._name, 10) ,line, sudent._name);
			// 総合判定順位
			svf.VrsOutn("RANK",line ,sudent._rank1);
			// 総合判定成績
			svf.VrsOutn("RESULT",line ,sudent._score1);
			// 備考
			svf.VrsOutn("REMARK",line ,sudent._score3);

			++line;
			if (line > 20) {
                svf.VrEndPage();
                line = 1;
            }
        }

        if (line > 1) {
            svf.VrEndPage();
            _hasData = true;            
        }
    }
    
    /**
     * ヘッダ・フッタ部の出力を行う
     * @param svf		帳票オブジェクト
     * @param sudent	帳票出力対象クラスオブジェクト
     */
    private void setHead(final Vrw32alp svf, final student sudent) {
        svf.VrsOut("NENDO", _param._nendo);
        svf.VrsOut("BUKA_NAME1", "推薦学部" + " " + sudent._buname + " "+ sudent._kaneme);
		svf.VrsOut(setformatArea("BUKA_NAME2", "推薦学部" + " " + sudent._buname + " "+ sudent._kaneme, 15) , "推薦学部" + " " + sudent._buname + " "+ sudent._kaneme);
    }

    /**
     * @param db2			ＤＢ接続オブジェクト
     * @param sbu_code		学部コード
     * @param ska_code		学科コード
     * @param sourse_div	学校コード
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudents(
    	final DB2UDB db2,
    	final String sbu_code,
    	final String ska_code,
    	final String sourse_div)
    	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getStudentSql(sbu_code, ska_code, sourse_div);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final student student = new student(rs.getString("NAME"),
                        rs.getString("RANK1"),
                        rs.getString("SCORE1"),
                        rs.getString("SCORE3"),
                        rs.getString("BU_NAME"),
                        rs.getString("KA_NAME")
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
     * @param sbu_code		学部コード
     * @param ska_code		学科コード
     * @param sourse_div	学校コード
     * @return				SQL文字列
     * @throws Exception
     */
    private String getStudentSql(
        final String sbu_code,
        final String ska_code,
        final String sourse_div)
    	{
        final String rtn;
        rtn = " select"
            + "    W2.BU_NAME,"
            + "    W2.KA_NAME,"
            + "    W3.RANK1,"
            + "    W3.SCORE1,"
            + "    W3.SCORE3,"
            + "    T1.NAME"
            + " from"
            // 生徒推薦希望データ
            + "    SCHREG_RECOMMENDATION_WISH_DAT W1"
            // 大学推薦設定データ
            + "    LEFT OUTER JOIN COLLEGE_RECOMMENDATION_DAT W2 ON"
            + "        W1.YEAR = W2.YEAR AND"						// 年度
            + "        W1.SCHOOL_CD = W2.SCHOOL_CD AND"				// 学校コード
            + "        W1.BU_CD = W2.BU_CD AND"						// 学部コード
            + "        W1.KA_CD = W2.KA_CD"							// 学科コード
            // 成績A値順位データ
            + "    LEFT OUTER JOIN RECORD_MOCK_RANK_DAT W3 ON"
            + "        W1.YEAR = W3.YEAR AND"						// 年度
            + "        W1.SCHREGNO = W3.SCHREGNO AND"				// 学籍番号
            + "        W2.DIV = W3.COURSE_DIV AND "					// コース区分
            + "        W3.DATA_DIV = '1' AND "						// データ区分
            + "        W3.SUBCLASSCD = '333333' "					// 科目コード
            // 学籍基礎マスタ
            + "    LEFT OUTER JOIN SCHREG_BASE_MST T1 ON"
            + "        W1.SCHREGNO = T1.SCHREGNO"					// 学籍番号
            + " where"
            + "    W1.YEAR = '" + _param._year + "' "				// 年度
            + "    and W1.SCHOOL_CD = '" + sourse_div + "' "		// 学校コード
            + "    and W1.BU_CD = '" + sbu_code + "' "				// 学部コード
            + "    and W1.KA_CD = '" + ska_code + "' "				// 学科コード
            + "    and W1.RECOMMENDATION_FLG = '1' "				// 推薦フラグ
            + " order by"
            + "    W3.RANK1, W1.SCHREGNO"
            ;
        log.debug("帳票出力対象データ抽出 の SQL=" + rtn);
        return rtn;
    }
    
    /** 生徒クラス */
    private class student {
        final String _name;
        final String _rank1;
        final String _score1;
        final String _score3;
        final String _buname;
        final String _kaneme;

        student(
                final String name,
                final String rank1,
                final String score1,
                final String score3,
                final String buname,
                final String kaneme
        ) {
        	_name = name;
        	_rank1 = rank1;
        	_score1 = score1;
        	_score3 = score3;
        	_buname = buname;
        	_kaneme = kaneme;
        }
    }

    /** 名称マスタ抽出処理 */
    private String getSourse_div(final DB2UDB db2) throws SQLException {
        String retAbbv3 = "";

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
			String sql ="";
			sql = "SELECT ABBV3 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' ";
			db2.query(sql);
			rs = db2.getResultSet();
			while( rs.next() ){
				retAbbv3 = rs.getString("ABBV3");
			}
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retAbbv3;
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
    	private final String[] _categorySelected;
    	private final String _nendo;
    	int yokuNen = 0;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _programid = request.getParameter("PRGID");
            _loginDate = request.getParameter("LOGIN_DATE");
            yokuNen = Integer.parseInt(_year);
    		String sNENDO = convZenkakuToHankaku(String.valueOf(yokuNen+1));
            _nendo = sNENDO + "年度";
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
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
     * 帳票に設定する文字数が制限文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name	帳票出力エリア
     * @param sval			値
     * @param maxVal		最大値
     * @return
     */
    private String setformatArea(String area_name, String sval, int maxVal) {

    	String retAreaName = "";
		// 値がnullの場合はnullが返される
    	if (sval == null) {
			return null;
		}
    	// 設定値が10文字超の場合、帳票設定エリアの変更を行う
    	if(maxVal >= sval.length()){
   			retAreaName = area_name + "_1";
    	} else {
   			retAreaName = area_name + "_2";
    	}
        return retAreaName;
    }
}
