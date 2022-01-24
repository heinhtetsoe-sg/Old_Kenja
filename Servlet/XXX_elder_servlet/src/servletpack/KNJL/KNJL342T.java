package servletpack.KNJL;

import java.io.IOException;
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
 *  学校教育システム 賢者 [入試処理] 鳥取・手続き済み名簿
 *
 */
public class KNJL342T {
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL342T.class);
    
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
        
        svf.VrSetForm("KNJL342T.frm", 1);
        
        //総ページ数取得・設定
        String total_page = getTotalPage(db2, svf);
        // 帳票出力のメソッド
        outPutPrint(db2, svf, total_page);
    }
    
    /**
     * 帳票出力処理
     * @param svf       帳票オブジェクト
     * @param students   帳票出力対象クラスオブジェクト
     */
    private void outPutPrint(DB2UDB db2, final Vrw32alp svf, final String total_page) {
        
        boolean dataflg = false;
        int gyo = 0;                //現在ページ数の判断用（行）
        int pagecnt = 1;            //現在ページ数
        int reccnt = 0;             //合計レコード数
        int reccnt_man      = 0;    //男レコード数カウント用
        int reccnt_woman    = 0;    //女レコード数カウント用
        
        final String sql = getStudentSql();
        log.debug(" sql = " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                
                gyo++;
                
                //５０行超えたとき、または、入試区分ブレイクの場合、ページ数カウント
                if (gyo > 50) {
                    svf.VrEndPage();
                    gyo = 1;
                    pagecnt++;
                }
                svf.VrsOut("TOTAL_PAGE"     ,total_page);
                
                svf.VrsOut("NENDO"      , KenjaProperties.gengou(Integer.parseInt(_param._year))+"年度");                    // 対象年度
                svf.VrsOut("DATE"       ,KNJ_EditDate.h_format_JP(_param._ctrlDate));                     // 作成日
                svf.VrsOut("PAGE"       ,String.valueOf(pagecnt));          // 現在ページ数
                // 受験番号
                svf.VrsOutn("EXAMNO"    , gyo, String.valueOf(rs.getInt("EXAMNO")));
                // 志願者(氏名)
                svf.VrsOutn(setformatArea("NAME", nvlT(rs.getString("NAME")), 11, "1", "2") , gyo, nvlT(rs.getString("NAME")));
                // 志願者(かな氏名)
                svf.VrsOutn(setformatArea("KANA", nvlT(rs.getString("NAME_KANA")), 12, "1", "2"), gyo, nvlT(rs.getString("NAME_KANA")));
                // 性別
                svf.VrsOutn("SEX"   , gyo, nvlT(rs.getString("SEXNAME")));
                // 出身学校：出身学校名
                svf.VrsOutn(setformatArea("FINSCHOOL", nvlT(rs.getString("FINSCHOOL_NAME")), 13, "1", "2") , gyo, nvlT(rs.getString("FINSCHOOL_NAME")));
                
                //レコード数カウント
                ++reccnt;
                final String sex = nvlT(rs.getString("SEX"));
                if(sex != null){
                    if(sex.equals("1")){
                        ++reccnt_man;
                    }
                    if(sex.equals("2")){
                        ++reccnt_woman;
                    }
                }
                dataflg = true;
                _hasData = true;   
            }
        } catch (SQLException e) {
            log.error("Exception: sql = " + sql, e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        //最終レコードを出力
        if (dataflg) {
            //最終ページに男女合計を出力
            svf.VrsOut("TOTAL_PAGE"     ,total_page);
            svf.VrsOut("NOTE"   ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
            svf.VrEndPage();
        }
    }
    
    /**総ページ数を取得**/
    private String getTotalPage(
        final DB2UDB db2,
        final Vrw32alp svf) {
        
        String total_page = "";
        
        // 総ページ数を取得
        final String sql = getTotalPageSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            log.debug(" getTotalPageSql =" + getTotalPageSql());
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                total_page = rs.getString("TOTAL_PAGE");
            }
        } catch (Exception e) {
            log.error("Exception: sql =" + sql, e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return total_page;

    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @return              SQL文字列
     */
    private String getStudentSql(){
        StringBuffer stb = new StringBuffer();
        stb.append(" select");
        stb.append("    W1.EXAMNO,");
        stb.append("    W1.NAME,");
        stb.append("    W1.NAME_KANA,");
        stb.append("    W1.ERACD, ");
        stb.append("    W1.SEX,");
        stb.append("    T4.NAME2 AS SEXNAME,");
        stb.append("    T1.FINSCHOOL_NAME,");
        stb.append("    W1.APPLICANTDIV, ");
        stb.append("    T3.NAME1 AS APPLICANTDIVNAME ");
        stb.append(" from");
        stb.append("    ENTEXAM_APPLICANTBASE_DAT W1 ");
        stb.append("    LEFT JOIN FINSCHOOL_MST T1 ON");
        stb.append("        W1.FS_CD = T1.FINSCHOOLCD ");
        stb.append("    INNER JOIN NAME_MST NML013 ON ");
        stb.append("         NML013.NAMECD1 = 'L013' ");
        stb.append("         and NML013.NAMECD2 = W1.JUDGEMENT ");
        stb.append("         and NML013.NAMESPARE1 = '1' ");
        stb.append("    LEFT JOIN NAME_MST T3 ON ");
        stb.append("         T3.NAMECD1 = 'L003' ");
        stb.append("         and T3.NAMECD2 = W1.APPLICANTDIV");
        stb.append("    LEFT JOIN NAME_MST T4 ON ");
        stb.append("         T4.NAMECD1 = 'Z002' ");
        stb.append("         and T4.NAMECD2 = W1.SEX");
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
    private String getTotalPageSql(){
        final String rtn;
        rtn = " select"
            + "    SUM(T1.TEST_CNT) TOTAL_PAGE"
            + " from"
            + "    (SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT "
            + "     FROM   ENTEXAM_APPLICANTBASE_DAT T1"
            + "     INNER JOIN NAME_MST T2 ON "
            + "         T2.NAMECD1 = 'L013' "
            + "         AND T2.NAMECD2 = T1.JUDGEMENT "
            + "         AND T2.NAMESPARE1 = '1' "
            + "     WHERE"
            + "        T1.ENTEXAMYEAR = '" + _param._year + "' "
            + "        AND T1.PROCEDUREDIV = '1') T1 "
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
