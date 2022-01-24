package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

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
 *  学校教育システム 賢者 [入試処理] 武蔵・入学手続者一覧
 *
 */
public class KNJL342M {
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL342M.class);
    
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
     * @param db2   ＤＢ接続オブジェクト
     * @param svf   帳票オブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        
        svf.VrSetForm("KNJL342M.frm", 3);
        
        //手続き者数取得・設定
        setProceduresCount(db2, svf);
        //手続き者総数取得・設定
        setTotalProceduresCount(db2, svf);
        // 帳票出力のメソッド
        outPutPrint(db2, svf);
    }
    
    /**
     * 帳票出力処理
     * @param svf       帳票オブジェクト
     * @param students   帳票出力対象クラスオブジェクト
     */
    private void outPutPrint(DB2UDB db2, final Vrw32alp svf) {
        
        boolean dataflg = false;
        int gyo = 0;                //現在ページ数の判断用（行）
        
        final String sql = getStudentSql();
        log.debug(" sql = " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                gyo++;
                svf.VrsOut("NENDO"      , KenjaProperties.gengou(Integer.parseInt(_param._year))+"年度　入学手続者一覧");                    // 対象年度
                svf.VrsOut("DATE"       ,KNJ_EditDate.h_format_JP(_param._ctrlDate));                     // 作成日
                // 番号
                svf.VrsOut("NO", String.valueOf(gyo));
                // 受験番号
                svf.VrsOut("EXAMNO", rs.getString("EXAMNO"));
                // 志願者(氏名)
                svf.VrsOut(setformatArea("NAME", nvlT(rs.getString("NAME")), 11, "1", "2"), nvlT(rs.getString("NAME")));
                svf.VrEndRecord();
                
                dataflg = true;
                _hasData = true;   
            }
        } catch (SQLException e) {
            log.error("Exception:", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        //最終レコードを出力
        if (dataflg) {
            svf.VrEndPage();
        }
    }
    
    //手続き者数取得・設定
    private void setProceduresCount(
        final DB2UDB db2,
        final Vrw32alp svf) {
        
        // 総ページ数を取得
        final String sql = getProceduresCountSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        int dayCount = 0;
        try {
            log.debug(" getProceduresCountSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                dayCount += 1;
                final String sentence;
                if (rs.getString("PROCEDUREDATE") == null) {
                    sentence = "";
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(java.sql.Date.valueOf(rs.getString("PROCEDUREDATE")));
                    final int month = cal.get(Calendar.MONTH) + 1;
                    final int day = cal.get(Calendar.DAY_OF_MONTH);
                    sentence = month + "/" + day + "(" + KNJ_EditDate.h_format_W(rs.getString("PROCEDUREDATE")) + ")";
                }
                final String countStr = rs.getString("COUNT") + "名";
                log.debug(" sentence  = " + sentence + countStr);
                
                svf.VrsOut("PROCEDURE" + dayCount, sentence + countStr);
                if (dayCount >= 2) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Exception: sql =" + sql, e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    
    //手続き者総数取得・設定
    private void setTotalProceduresCount(
        final DB2UDB db2,
        final Vrw32alp svf) {
        
        // 総ページ数を取得
        final String sql = getTotalProceduresCountSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.debug(" getTotalProceduresCountSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String totalProcedureCount = rs.getString("TOTAL_PROCEDURE_COUNT");
                String totalPassCount = rs.getString("TOTAL_PASS_COUNT");
                svf.VrsOut("PROCEDURE3", "計" + totalProcedureCount + "名／" + totalPassCount + "名中");
            }
        } catch (Exception e) {
            log.error("Exception: sql =" + sql, e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @return              SQL文字列
     */
    private String getStudentSql(){
        StringBuffer stb = new StringBuffer();
        stb.append(" select");
        stb.append("    W1.EXAMNO,");
        stb.append("    W1.NAME");
        stb.append(" from");
        stb.append("    ENTEXAM_APPLICANTBASE_DAT W1 ");
        stb.append("    INNER JOIN NAME_MST NML013 ON ");
        stb.append("         NML013.NAMECD1 = 'L013' ");
        stb.append("         and NML013.NAMECD2 = W1.JUDGEMENT ");
        stb.append("         and NML013.NAMESPARE1 = '1' ");
        stb.append(" where");
        stb.append("    W1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("    and W1.PROCEDUREDIV = '1' ");
        stb.append(" order by");
        stb.append("    W1.EXAMNO ");
        return stb.toString();
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @return              SQL文字列
     */
    private String getProceduresCountSql(){
        final String rtn;
        rtn = " select"
            + "    T1.PROCEDUREDATE, COUNT(*) AS COUNT "
            + " from"
            + "    (SELECT T1.PROCEDUREDATE, T1.EXAMNO, T1.NAME "
            + "     FROM   ENTEXAM_APPLICANTBASE_DAT T1"
            + "     INNER JOIN NAME_MST T2 ON "
            + "         T2.NAMECD1 = 'L013' "
            + "         AND T2.NAMECD2 = T1.JUDGEMENT "
            + "         AND T2.NAMESPARE1 = '1' "
            + "     WHERE"
            + "        T1.ENTEXAMYEAR = '" + _param._year + "' "
            + "        AND T1.PROCEDUREDIV = '1') T1 "
            + "  group by T1.PROCEDUREDATE "
            + "  order by T1.PROCEDUREDATE "
            ;
        return rtn;
    }
    
    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @return              SQL文字列
     */
    private String getTotalProceduresCountSql(){
        final String rtn;
        rtn = " select"
            + "    SUM(CASE WHEN T1.PROCEDUREDIV = '1' THEN 1 ELSE 0 END) AS TOTAL_PROCEDURE_COUNT, COUNT(*) AS TOTAL_PASS_COUNT "
            + " from"
            + "     ENTEXAM_APPLICANTBASE_DAT T1"
            + "     INNER JOIN NAME_MST T2 ON "
            + "         T2.NAMECD1 = 'L013' "
            + "         AND T2.NAMECD2 = T1.JUDGEMENT "
            + "         AND T2.NAMESPARE1 = '1' "
            ;
        return rtn;
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
        private final String _ctrlDate;
        
        Param(final DB2UDB db2, HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
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
