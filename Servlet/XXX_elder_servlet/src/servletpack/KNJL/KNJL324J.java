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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;


/**
 * 合格者発表資料（掲示用）
 * 
 * @author nakasone
 *
 */
public class KNJL324J {
    private static final String FORM_NAME = "KNJL324J.frm";
    private static final int MAX_COL = 3;
    private static final int MAX_ROW = 5;
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL324J.class);
    
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

    	boolean retflg = false;	// 対象データ存在フラグ
        svf.VrSetForm(FORM_NAME, 1);

		// 合格者発表資料(掲示用)データ取得
		final List student = createStudents(db2);
		// 帳票出力のメソッド
		retflg = outPutPrint(svf, student);
		
		return retflg;
			
    }
    
    /**
     * 帳票出力処理
     * @param svf		帳票オブジェクト
     * @param student	帳票出力対象クラスオブジェクト
     */
    private boolean outPutPrint(final Vrw32alp svf, final List student) {

    	boolean retflg = false;	// 対象データ存在フラグ
		int gyo = 1;				//現在ページ数の判断用（行）
		int len = 1;				//現在ページ数の判断用（列）

        for (Iterator it = student.iterator(); it.hasNext();) {
            final student sudent = (student) it.next();

			//最終列
			if (len > MAX_COL) {
				len = 1;
				gyo++;
				//最終行
				if (gyo > MAX_ROW) {
					svf.VrEndPage();//ページを出力
					gyo = 1;
				}
			}
			// 1列目
			if(len == 1){
				// 受験番号
				svf.VrsOutn("EXAMNO1" 	, gyo, sudent._examno);
			}
			// 2列目
			if(len == 2){
				// 受験番号
				svf.VrsOutn("EXAMNO2" 	, gyo, sudent._examno);
			}
			// 3列目
			if(len == 3){
				// 受験番号
				svf.VrsOutn("EXAMNO3" 	, gyo, sudent._examno);
			}

			len++;
			retflg = true;
        }
        
		//最終レコードを出力
		if (retflg) {
            svf.VrEndPage();
		}
        
        
		return retflg;
    }
    
    /**
     * @param db2			ＤＢ接続オブジェクト
     * @return				帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudents(
    	final DB2UDB db2)
    	throws SQLException {
    	
        final List rtnList = new ArrayList();
        final String sql = getStudentSql();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final student student = new student(rs.getString("EXAMNO"));
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
        final String rtn;
        rtn = " select"
            + "    EXAMNO "
            + " from"
            + "    ENTEXAM_RECEPT_DAT "
            + " where"
            + "    ENTEXAMYEAR = '" + _param._year + "' "				// 年度
            + "    and TESTDIV = '" +_param._testdiv + "' "				// 入試区分
            + "    and JUDGEDIV = '1' "									// 合否区分(1:合格)
            + " order by"
            + "    EXAMNO"
            ;
        log.debug("帳票出力対象データ抽出 の SQL=" + rtn);
        return rtn;
    }

    /** 生徒クラス */
    private class student {
        final String _examno;

        student(
                final String examno
        ) {
        	_examno = examno;
        }
    }

    /** パラメータクラス */
    private class Param {
    	private final String _programid;
    	private final String _year;
    	private final String _semester;
    	private final String _loginDate;
    	private final String _testdiv;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testdiv = request.getParameter("TESTDIV");
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
