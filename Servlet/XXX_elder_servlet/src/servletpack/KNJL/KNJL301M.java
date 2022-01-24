package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
 * 志願者チェックリスト
 * 
 * @author nakasone
 *
 */
public class KNJL301M {
    private boolean _hasData;
    Param _param;

    private static final Log log = LogFactory.getLog(KNJL301M.class);
    
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
        
        svf.VrSetForm("KNJL301M.frm", 1);
        
        //総ページ数取得・設定
        String total_page = getTotalPage(db2, svf, _param._applicantDiv);
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
        String sapplicantdiv = "0";      //現在ページ数の判断用（入試区分）
        int reccnt = 0;             //合計レコード数
        int reccnt_man      = 0;    //男レコード数カウント用
        int reccnt_woman    = 0;    //女レコード数カウント用
        
        final String sql = getStudentSql(_param._applicantDiv);
        log.debug(" sql = " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                
                final String applicantdiv = nvlT(rs.getString("APPLICANTDIV"));
                if(reccnt == 0){
                    sapplicantdiv = applicantdiv;
                }
                
                gyo++;
                
                //２５行超えたとき、または、入試区分ブレイクの場合、ページ数カウント
                if ((gyo > 25) || (!sapplicantdiv.equals(applicantdiv) && !sapplicantdiv.equals("0"))) {
                    if ((!sapplicantdiv.equals(applicantdiv) && !sapplicantdiv.equals("0"))) {
                        svf.VrsOut("NOTE"   ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
                        reccnt_man = 0;
                        reccnt_woman = 0;
                        reccnt = 0;
                    }
                    svf.VrsOut("TOTAL_PAGE"     ,total_page);
                    svf.VrEndPage();
                    gyo = 1;
                    pagecnt++;
                }
                
                svf.VrsOut("NENDO"      , KenjaProperties.gengou(Integer.parseInt(_param._year))+"年度");                    // 対象年度
                svf.VrsOut("DATE"       , _param._dateStr);                     // 作成日
                svf.VrsOut("APPLICANTDIV"  ,nvlT(rs.getString("APPLICANTDIVNAME")));                    // 入試区分
                svf.VrsOut("PAGE"       ,String.valueOf(pagecnt));          // 現在ページ数
                // 受験番号
                svf.VrsOutn("EXAMNO"    , gyo, rs.getString("EXAMNO"));
                // 志願者(かな氏名)
                svf.VrsOutn(setformatArea("NAME_KANA", nvlT(rs.getString("NAME_KANA")), 10, "1", "2"), gyo, nvlT(rs.getString("NAME_KANA")));
                // 志願者(氏名)
                svf.VrsOutn(setformatArea("NAME", nvlT(rs.getString("NAME")), 10, "1", "2") , gyo, nvlT(rs.getString("NAME")));
                // 性別
                svf.VrsOutn("SEX"   , gyo, nvlT(rs.getString("SEXNAME")));
                // 誕生日
                String birthDay = h_format_dot(rs.getString("BIRTH_ERA_NAME"), rs.getString("BIRTH_Y"), rs.getString("BIRTH_M"), rs.getString("BIRTH_D"));
                svf.VrsOutn("BIRTHDAY" , gyo, birthDay);

                // 出身学校：出身学校名
                svf.VrsOutn(setformatArea("FINSCHOOL", rs.getString("FS_NAME"), 10, "1", "2") , gyo, rs.getString("FS_NAME"));
                // 保護者名
                svf.VrsOutn(setformatArea("GUARD_NAME", nvlT(rs.getString("GNAME")), 10, "1", "2"), gyo, nvlT(rs.getString("GNAME")));
                // 保護者かな
                svf.VrsOutn(setformatArea("GUARD_KANA", nvlT(rs.getString("GKANA")), 10, "1", "2"), gyo, nvlT(rs.getString("GKANA")));
                
                // 国公私立区分
                svf.VrsOutn("SCHOOL_GROUP"   , gyo, nvlT(rs.getString("NATPUBPRI_NAME")));
                // 所在地区分
                svf.VrsOutn("ADDRESS_GROUP"   , gyo, nvlT(rs.getString("AREA_DIV_NAME")));
                // 所在地
                svf.VrsOutn("ADDRESS"   , gyo, nvlT(rs.getString("AREA_NAME")));
                // 備考
                svf.VrsOutn(setformatArea("REMARK", rs.getString("REMARK"), 10, "1", "2") , gyo, rs.getString("REMARK"));

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
                //現在ページ数判断用
                sapplicantdiv = applicantdiv;
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
            reccnt_man = 0;
            reccnt_woman = 0;
            reccnt = 0;
            svf.VrEndPage();
        }
    }
    
    /**総ページ数を取得**/
    private String getTotalPage(
        final DB2UDB db2,
        final Vrw32alp svf,
        String applicantDiv) {
        
        String total_page = "";
        
        // 総ページ数を取得
        final String sql = getTotalPageSql(applicantDiv);
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
     * @param applicantdiv 入試制度
     * @return              SQL文字列
     * @throws Exception
     */
    private String getStudentSql(final String applicantdiv){
        StringBuffer stb = new StringBuffer();
        stb.append(" select");
        stb.append("    W1.EXAMNO,");
        stb.append("    W1.NAME,");
        stb.append("    W1.NAME_KANA,");
        stb.append("    W1.ERACD, ");
        stb.append("    T9.ABBV1 AS BIRTH_ERA_NAME, ");
        stb.append("    W1.BIRTH_Y, ");
        stb.append("    W1.BIRTH_M, ");
        stb.append("    W1.BIRTH_D, ");
        stb.append("    W1.SEX,");
        stb.append("    T4.NAME2 AS SEXNAME,");
        stb.append("    W1.REMARK1 AS REMARK,");
        stb.append("    W3.ZIPCD,");
        stb.append("    W3.GNAME,");
        stb.append("    W3.GKANA,");
        stb.append("    W1.FS_NAME,");
        stb.append("    W1.APPLICANTDIV, ");
        stb.append("    T3.NAME1 AS APPLICANTDIVNAME, ");
        stb.append("    W1.FS_ERACD, ");
        stb.append("    T5.NATPUBPRI_NAME, ");
        stb.append("    T6.AREA_DIV_NAME, ");
        stb.append("    T7.AREA_NAME ");
        stb.append(" from");
        stb.append("    ENTEXAM_APPLICANTBASE_DAT W1 ");
        stb.append("    LEFT JOIN ENTEXAM_APPLICANTADDR_DAT W3 ON");
        stb.append("        W1.ENTEXAMYEAR = W3.ENTEXAMYEAR and");              // 年度
        stb.append("        W1.EXAMNO = W3.EXAMNO ");                           // 受験番号
        stb.append("    LEFT JOIN NAME_MST T3 ON ");
        stb.append("         T3.NAMECD1 = 'L003' and");                         // 名称区分
        stb.append("         T3.NAMECD2 = W1.APPLICANTDIV");                         // 名称コード
        stb.append("    LEFT JOIN NAME_MST T4 ON ");
        stb.append("         T4.NAMECD1 = 'Z002' and");                         // 名称区分
        stb.append("         T4.NAMECD2 = W1.SEX");                             // 名称コード
        stb.append("    LEFT JOIN ENTEXAM_NATPUBPRI_MST T5 ON ");
        stb.append("         T5.NATPUBPRI_CD = W1.FS_NATPUBPRIDIV ");
        stb.append("    LEFT JOIN ENTEXAM_AREA_DIV_MST T6 ON ");
        stb.append("         T6.NATPUBPRI_CD = W1.FS_NATPUBPRIDIV ");
        stb.append("         and T6.AREA_DIV_CD = W1.FS_AREA_DIV ");
        stb.append("    LEFT JOIN ENTEXAM_AREA_MST T7 ON ");
        stb.append("         T7.NATPUBPRI_CD = W1.FS_NATPUBPRIDIV ");
        stb.append("         and T7.AREA_DIV_CD = W1.FS_AREA_DIV ");
        stb.append("         and T7.AREA_CD = W1.FS_AREA_CD ");
        stb.append("    LEFT JOIN NAME_MST T9 ON ");
        stb.append("         T9.NAMECD1 = 'L007' and");                         // 名称区分
        stb.append("         T9.NAMECD2 = W1.ERACD");                             // 名称コード
        stb.append(" where");
        stb.append("    W1.ENTEXAMYEAR = '" + _param._year + "' ");             // 年度
        stb.append("    and W1.APPLICANTDIV = '" + applicantdiv + "' ");                 // 入試制度
        //1:志願者全員,2:志願者指定
        if ("2".equals(_param._taishou)) {
            stb.append("    and W1.EXAMNO BETWEEN '" + _param._sExamno + "' AND '" + _param._eExamno + "' ");
        }
        stb.append(" order by");
        stb.append("    W1.APPLICANTDIV, ");
        if (!"1".equals(_param._sort)) {
            stb.append("    W1.FS_NAME, ");
        }
        stb.append("    W1.EXAMNO ");
        return stb.toString();
    }

    /**
     * 帳票出力対象データ抽出ＳＱＬ生成処理
     * @param test_div      入試区分
     * @return              SQL文字列
     * @throws Exception
     */
    private String getTotalPageSql(final String applicantDiv){
        final String rtn;
        rtn = " select"
            + "    SUM(T1.TEST_CNT) TOTAL_PAGE"
            + " from"
            + "    (SELECT CASE WHEN MOD(COUNT(*),25) > 0 THEN COUNT(*)/25 + 1 ELSE COUNT(*)/25 END TEST_CNT "
            + "     FROM   ENTEXAM_APPLICANTBASE_DAT "
            + "     WHERE"
            + "        ENTEXAMYEAR = '" + _param._year + "' "                // 年度
            + "        AND APPLICANTDIV = '" + _param._applicantDiv + "' "                // 年度
            + "     GROUP BY"
            + "        APPLICANTDIV ) T1 "
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

    private String h_format_dot(String h, String y, String m, String d) {
        if (y == null && m == null) {
            return "";
        }
        String header = (h == null) ? "" : h;
        String year = (y == null) ? "" : ((y.length() == 1 ?  "0" : "") + y);
        String month = (m == null) ? "" : ((m.length() == 1 ?  "0" : "") + m);
        return header + year + "." + month + (d == null ? "" : ("." + ((d.length() == 1 ?  "0" : "") + d)));
    }

    /** パラメータクラス */
    private class Param {
        private final String _programid;
        private final String _year;
        private final String _ctrlDate;
        private final String _applicantDiv;
        private final String _sort;
        private final String _taishou; //1:志願者全員,2:志願者指定
        private String _sExamno;
        private String _eExamno;
        
        private final String _dateStr;
        
        Param(final DB2UDB db2, HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _sort = request.getParameter("SYUTURYOKUJUN");
            _taishou = request.getParameter("TAISHOU");
            _sExamno = request.getParameter("S_EXAMNO");
            _eExamno = request.getParameter("E_EXAMNO");
            
            _dateStr = setDateStr();
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
