package servletpack.KNJL;

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
 *  学校教育システム 賢者 [入試処理] 鳥取・座席ラベル
 *
 */
public class KNJL300T {
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL300T.class);
    
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
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            
            svf.VrSetForm("KNJL300T.frm", 1);
            
            // 帳票出力のメソッド
            outPutPrint(db2, svf);
            
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
     * 帳票出力処理
     * @param svf       帳票オブジェクト
     * @param students   帳票出力対象クラスオブジェクト
     */
    private void outPutPrint(DB2UDB db2, final Vrw32alp svf) {
        
        _hasData = false;
        
        int gyo = Integer.parseInt(_param._poRow); //行番号
        int len = Integer.parseInt(_param._poCol); //列番号

        //SVF出力
        for( int ia=0 ; ia < _param._categoryName.length ; ia++ ){
            String classcd1 = _param._categoryName[ia];

            //連結されているパラメータの分割（試験会場コード + '-' + 開始受付番号（MIN）+ '-' + 終了受付番号（MAX））
            String hallCd = classcd1.substring(0, 4);
            String receptSt = classcd1.substring(5,10);                //開始受付番号（MIN）
            String receptEd = classcd1.substring(11);                 //終了受付番号（MAX）
        
            //指示画面で受付番号が入力されている場合、それをセット
            if (_param._noinfSt != null && !"".equals(_param._noinfSt)) receptSt = _param._noinfSt;     //開始受付番号（画面入力）
            if (_param._noinfEd != null && !"".equals(_param._noinfEd)) receptEd = _param._noinfEd;     //終了受付番号（画面入力）
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            final String sql = getExamnoSql(hallCd, receptSt, receptEd);
            try {
                log.debug("sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    
                    String examNo = rs.getString("EXAMNO");
                    
                    //最終列
                    if (len > 4) {
                        len = 1;
                        gyo++;
                        //最終行
                        if (gyo > 5) {
                            svf.VrEndPage();//ページを出力
                            gyo = 1;
                        }
                    }
                    //タックシールに印字
                    svf.VrsOutn("RECEPTNO"+String.valueOf(len), gyo, String.valueOf(examNo)); //受付番号
                    len++;
                    _hasData = true;
                }
            } catch (SQLException e) {
                log.error("Exception: sql = " + sql, e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        //最終ページを出力
        if (_hasData) svf.VrEndPage();
    }
    
    private String getExamnoSql(String hallCd, String receptSt, String receptEd) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_HALL_DAT T1, ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T2  ");
        stb.append(" WHERE ");
        stb.append("     T1.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("     AND T1.EXAMHALLCD = '" + hallCd +"' ");
        stb.append("     AND T1.TESTDIV = '1' ");
        stb.append("     AND T2.TESTDIV = '1' ");
        stb.append("     AND T1.EXAM_TYPE = '" + _param._examType +"' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T2.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND INT(T2.EXAMNO) BETWEEN " + receptSt + " AND " + receptEd + " ");
        stb.append("     AND T2.EXAMNO BETWEEN S_RECEPTNO AND E_RECEPTNO ");
        if (_param._testDiv2 != null) {
            stb.append("     AND T2.TESTDIV2 = '" + _param._testDiv2 + "' ");
        }
        return stb.toString();
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
        private final String _ctrlDate;
        private final String _applicantDiv;
        private final String[] _categoryName;
        private final String _noinfSt;
        private final String _noinfEd;
        private final String _poRow;
        private final String _poCol;
        private final String _examType;
        private final String _testDiv2;
        
        Param(final DB2UDB db2, HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            
            _categoryName = request.getParameterValues("category_name");     //試験会場コード + '-' + 開始受付番号（MIN）+ '-' + 終了受付番号（MAX）
            _noinfSt = request.getParameter("noinf_st");                    //開始受付番号（画面入力）
            _noinfEd = request.getParameter("noinf_ed");                    //終了受付番号（画面入力）
            
            _poRow = (_categoryName.length > 1) ? "1" : request.getParameter("POROW");                       //開始位置（行）
            _poCol = (_categoryName.length > 1) ? "1" : request.getParameter("POCOL");                       //開始位置（列）
            
            _examType = request.getParameter("EXAM_TYPE");
            
            _testDiv2 = request.getParameter("TESTDIV2");
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
     * @param area_name 帳票出力エリア
     * @param sval          値
     * @param area_len      制限文字数
     * @param hokan_Name1   制限文字以下の場合のエリア名
     * @param hokan_Name2   制限文字超の場合のエリア名
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
