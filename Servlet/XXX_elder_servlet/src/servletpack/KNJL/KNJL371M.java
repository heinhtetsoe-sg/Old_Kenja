package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
 *  学校教育システム 賢者 [入試処理]　武蔵・成績一覧 (総点順)
 * 
 *
 */
public class KNJL371M {
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL371M.class);
    
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
        
        svf.VrSetForm("KNJL371M.frm", 1);
        
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
        String examno = null;
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
                
                if (examno != null && examno.equals(rs.getString("EXAMNO"))) {
                    String testSubclassCd = rs.getString("TESTSUBCLASSCD");
                    if (testSubclassCd != null) {
                        svf.VrsOutn("SCORE" + testSubclassCd,  gyo, rs.getString("SCORE"));
                    }
                    continue;
                }
                
                gyo++;

                //５０行超えたとき、または、入試区分ブレイクの場合、ページ数カウント
                if (gyo > 50) {
                    gyo = 1;
                    svf.VrEndPage();
                    pagecnt++;
                }
                
                final String sort;
                if ("2".equals(_param._sort)) {
                    sort = "総点順";
                } else if ("3".equals(_param._sort)) {
                    sort = "Z1順";
                } else if ("4".equals(_param._sort)) {
                    sort = "Z2順";
                } else {
                    sort = "受験番号順";
                }
                svf.VrsOut("YEAR"      , KenjaProperties.gengou(Integer.parseInt(_param._year))+"年度　入試試験成績一覧（" + sort + "）"); // 対象年度
                svf.VrsOut("DATE"       , _param._dateStr);                     // 作成日
                svf.VrsOut("PAGE"       ,String.valueOf(pagecnt));          // 現在ページ数
                svf.VrsOut("TOTAL_PAGE"     ,total_page);
                // 順位
                svf.VrsOutn("RANK"    , gyo, rs.getString("TOTAL_RANK4"));
                // 受験番号
                svf.VrsOutn("EXAM_NO"    , gyo, rs.getString("EXAMNO"));
                // 総点
                svf.VrsOutn("TOTAL_SCORE"    , gyo, rs.getString("TOTAL4"));
                
                // 国公私立区分
                svf.VrsOutn("DIV"   , gyo, nvlT(rs.getString("NATPUBPRI_NAME")));
                // 所在地区分
                svf.VrsOutn("CITY"   , gyo, nvlT(rs.getString("AREA_DIV_NAME")));
                // 所在地
                svf.VrsOutn("SCHOOL_ADD"   , gyo, nvlT(rs.getString("AREA_NAME")));
                // 出身学校：出身学校名
                svf.VrsOutn("SCHOOL_NAME" , gyo, rs.getString("FS_NAME"));

                String testSubclassCd = rs.getString("TESTSUBCLASSCD");
                if (testSubclassCd != null) {
                    svf.VrsOutn("SCORE" + testSubclassCd, gyo, rs.getString("SCORE"));
                }
                // Z1順位
                svf.VrsOutn("Z1"   , gyo, nvlT(rs.getString("JUDGE_DEVIATION_RANK")));
                // Z2順位
                svf.VrsOutn("Z2"   , gyo, nvlT(rs.getString("LINK_JUDGE_DEVIATION_RANK")));

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
                examno = rs.getString("EXAMNO");
                //現在ページ数判断用
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
            svf.VrsOut("NOTE"   ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
            reccnt_man = 0;
            reccnt_woman = 0;
            reccnt = 0;
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
        log.debug(" totalpage sql = " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
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
     * @throws Exception
     */
    private String getStudentSql(){
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.TOTAL_RANK4, ");
        stb.append("     T1.TOTAL4, ");
        stb.append("     T3.SEX, ");
        stb.append("     T3.FS_NAME, ");
        stb.append("     T5.NATPUBPRI_NAME, ");
        stb.append("     T6.AREA_DIV_NAME, ");
        stb.append("     T7.AREA_NAME, ");
        stb.append("     T1.JUDGE_DEVIATION_RANK, ");
        stb.append("     T1.LINK_JUDGE_DEVIATION_RANK, ");
        stb.append("     T2.TESTSUBCLASSCD, T2.SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T2 ON ");
        stb.append("         T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T2.RECEPTNO = T1.RECEPTNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON ");
        stb.append("         T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_NATPUBPRI_MST T5 ON ");
        stb.append("         T5.NATPUBPRI_CD = T3.FS_NATPUBPRIDIV ");
        stb.append("     LEFT JOIN ENTEXAM_AREA_DIV_MST T6 ON ");
        stb.append("         T6.NATPUBPRI_CD = T3.FS_NATPUBPRIDIV ");
        stb.append("         AND T6.AREA_DIV_CD = T3.FS_AREA_DIV ");
        stb.append("     LEFT JOIN ENTEXAM_AREA_MST T7 ON ");
        stb.append("         T7.NATPUBPRI_CD = T3.FS_NATPUBPRIDIV ");
        stb.append("         AND T7.AREA_DIV_CD = T3.FS_AREA_DIV ");
        stb.append("         AND T7.AREA_CD = T3.FS_AREA_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year +"' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '1' ");
        // 1:受験番号順, 2:総点順, 3:Z1順, 4:Z2順
        stb.append(" ORDER BY ");
        if ("2".equals(_param._sort)) {
            stb.append("     VALUE(T1.TOTAL4, 0) DESC,  ");
        } else if ("3".equals(_param._sort)) {
            stb.append("     VALUE(T1.JUDGE_DEVIATION_RANK, 9999), ");
        } else if ("4".equals(_param._sort)) {
            stb.append("     VALUE(T1.LINK_JUDGE_DEVIATION_RANK, 9999), ");
        } else {
        }
        stb.append("     T1.EXAMNO, ");
        stb.append("     T2.TESTSUBCLASSCD ");
        return stb.toString();
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @param test_div      入試区分
     * @return              SQL文字列
     * @throws Exception
     */
    private String getTotalPageSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUM(PAGE) AS TOTAL_PAGE ");
        stb.append(" FROM (SELECT ");
        stb.append("       CASE WHEN MOD(COUNT(*), 50) > 0 THEN COUNT(*) / 50 + 1 ELSE COUNT(*) / 50 END AS PAGE ");
        stb.append("   FROM ENTEXAM_RECEPT_DAT T1 ");
        stb.append("       INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON ");
        stb.append("           T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("           AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("           AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("           AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("   WHERE ");
        stb.append("       T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("      AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("      AND T1.TESTDIV = '1' ) T1 ");
        return stb.toString();
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
        private final String _applicantDiv;
        private final String _sort;
        
        private final String _dateStr;
        private final List _subclassCds;
        
        Param(final DB2UDB db2, HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _sort = request.getParameter("ORDER_DIV");
            
            _dateStr = setDateStr();
            
            _subclassCds = getSubclassCds(db2);
        }
        
        private List getSubclassCds(DB2UDB db2) {
            List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT NAMECD2 AS TESTSUBCLASSCD FROM NAME_MST WHERE NAMECD1 = 'L009' ";
                
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.add(rs.getString("TESTSUBCLASSCD"));
                }
                
            } catch (SQLException e) {
                log.error("SQLException:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private int getSubclassIndex(String testsubclassCd) {
            for (int i = 0; i < _subclassCds.size(); i++) {
                if (testsubclassCd != null && testsubclassCd.equals(_subclassCds.get(i))) {
                    return i + 1;
                }
            }
            return -1;
        }
        
        private String setDateStr() {
            String date = KNJ_EditDate.h_format_JP(_ctrlDate);
            String youbi ="(" + KNJ_EditDate.h_format_W(_ctrlDate) +  ")";
            
            DecimalFormat df = new DecimalFormat("00");
            Calendar cal = Calendar.getInstance();
            int h = cal.get(Calendar.HOUR_OF_DAY);
            int m = cal.get(Calendar.MINUTE);
            String time = df.format(h) + ":" + df.format(m);
            
            return date + youbi + " " + time;
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
