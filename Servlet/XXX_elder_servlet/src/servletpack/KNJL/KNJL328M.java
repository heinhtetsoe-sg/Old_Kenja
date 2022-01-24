/*
 * $Id: 023e25c0d8b73287ec0b7a10e0ff072e6abd17ab $
 *
 * 作成日: 2009/12/14
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試処理] 武蔵・合否別一覧表
 */
public class KNJL328M {

    private static final Log log = LogFactory.getLog(KNJL328M.class);

    private boolean _hasData;

    Param _param;

    int _page;

    /**
     * @param request リクエスト
     * @param response レスポンス
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

            _hasData = false;
            _page = 0;

            //総ページ数取得・設定
            String totalPage = getTotalPage(db2, svf);
            if (_param._isGoukaku) {
                printMain(db2, svf, totalPage, "1");
            }
            if (_param._isHoin) {
                printMain(db2, svf, totalPage, "2");
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String totalPage, final String div) {
        svf.VrSetForm("KNJL328M.frm", 4);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        int reccnt = 0;             //合計レコード数
        int reccnt_man      = 0;    //男レコード数カウント用
        int reccnt_woman    = 0;    //女レコード数カウント用
        
        try {
            final String sql = sql(div);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                
                if (0 < reccnt) {
                    svf.VrEndRecord();
                }
                
                if (0 == reccnt%50) {
                    _page += 1;
                }
                String title = "1".equals(rs.getString("NML013_NAMESPARE1")) ? "合格者一覧表(受験番号順)" : "補員者一覧表(補員順位順)"; 
                String resultName = "1".equals(rs.getString("NML013_NAMESPARE1")) ? "合否" : "補員順位"; 
                svf.VrsOut("RESULTNAME", resultName);
                svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "-04-01") + "度　" + title);
                svf.VrsOut("DATE", _param._dateTimeString);
                svf.VrsOut("PAGE", String.valueOf(_page));
                svf.VrsOut("TOTAL_PAGE"     ,totalPage);
                svf.VrsOut("EXAMNO", rs.getString("EXAMNO"));

//                // 志願者(かな氏名)
//                svf.VrsOut(setformatArea("KANA", rs.getString("NAME_KANA"), 10, "1", "2"), rs.getString("NAME_KANA"));
//                // 志願者(氏名)
//                svf.VrsOut(setformatArea("NAME", rs.getString("NAME"), 10, "1", "2"), rs.getString("NAME"));

                svf.VrsOut("SEX", rs.getString("SEXNAME"));
                svf.VrsOut("FINSCHOOL", rs.getString("FS_NAME"));
                svf.VrsOut("RESULT", ("2".equals(div)) ? rs.getString("SUB_ORDER") : rs.getString("NML013_NAME1"));
                
                ++reccnt;
                final String sex = rs.getString("SEX") == null ? "" : rs.getString("SEX");
                if(sex != null){
                    if(sex.equals("1")){
                        ++reccnt_man;
                    }
                    if(sex.equals("2")){
                        ++reccnt_woman;
                    }
                }

                _hasData = true;
            }
            
            if (0 < reccnt) {
                svf.VrsOut("NOTE"   ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
                svf.VrEndRecord();
            }
            
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
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
     * @param test_div      入試区分
     * @return              SQL文字列
     * @throws Exception
     */
    private String getTotalPageSql(){
        final StringBuffer rtn = new StringBuffer();
        rtn.append(" select");
        rtn.append("    SUM(T1.TEST_CNT) TOTAL_PAGE");
        rtn.append(" from");
        rtn.append("    (SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT ");
        rtn.append("     FROM   ENTEXAM_APPLICANTBASE_DAT T1 ");
        rtn.append("     LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'L013' ");
        rtn.append("        AND T2.NAMECD2 = T1.JUDGEMENT ");
        rtn.append("     WHERE");
        rtn.append("        ENTEXAMYEAR = '" + _param._year + "' ");                // 年度
        rtn.append("        AND APPLICANTDIV = '" + _param._applicantdiv + "' ");                // 年度
        if (_param._isGoukaku && _param._isHoin) {
            rtn.append(" AND (T2.NAMESPARE1 = '1' OR T1.JUDGEMENT = '2' ) ");
        } else if (_param._isGoukaku) {
            rtn.append(" AND T2.NAMESPARE1 = '1' ");
        } else if (_param._isHoin) {
            rtn.append(" AND T1.JUDGEMENT = '2' ");
        }
        rtn.append("  GROUP BY ");
        rtn.append("     APPLICANTDIV, T2.NAMESPARE1) T1 ");
        return rtn.toString();
    }

    private String sql(final String div) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     W1.EXAMNO, ");
        stb.append("     W1.APPLICANTDIV, ");
        stb.append("     W1.NAME, ");
        stb.append("     W1.NAME_KANA, ");
        stb.append("     W1.SEX, ");
        stb.append("     T4.ABBV1 AS SEXNAME, ");
        stb.append("     NML013.NAME1 AS NML013_NAME1, ");
        stb.append("     NML013.NAMESPARE1 AS NML013_NAMESPARE1, ");
        stb.append("     W1.SUB_ORDER, ");
        stb.append("     W1.FS_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT W1 ");
        stb.append("     LEFT JOIN ENTEXAM_DESIRE_DAT T2 ON ");
        stb.append("         T2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
        stb.append("         T2.APPLICANTDIV = W1.APPLICANTDIV AND ");
        stb.append("         T2.TESTDIV = W1.TESTDIV AND ");
        stb.append("         T2.EXAMNO = W1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST T4 ON ");
        stb.append("         T4.NAMECD1 = 'Z002' ");
        stb.append("         and T4.NAMECD2 = W1.SEX ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON ");
        stb.append("         NML013.NAMECD1 = 'L013' ");
        stb.append("         and NML013.NAMECD2 = W1.JUDGEMENT ");
        stb.append(" WHERE ");
        stb.append("     W1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND VALUE(T2.EXAMINEE_DIV, '1') <> '2' ");
        if (!"0".equals(_param._applicantdiv)) {
            stb.append("     AND W1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        }
        //1:合格一覧,2:補員一覧
        if ("1".equals(div)) {
            stb.append("     AND NML013.NAMESPARE1 = '1' ");
        }
        if ("2".equals(div)) {
            stb.append("     AND W1.JUDGEMENT = '2' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     NML013.NAMESPARE1, ");
        stb.append("     W1.APPLICANTDIV, ");
        if ("2".equals(div)) {
            stb.append("     smallint(value(W1.SUB_ORDER,'9999')), ");
        }
        stb.append("     W1.EXAMNO ");
        return stb.toString();
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _programid;
        private final String _year;
        private final String _applicantdiv;
        private final String _date;
        private final String _dateTimeString;
        private final boolean _isGoukaku;
        private final boolean _isHoin;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _date = request.getParameter("CTRL_DATE");
            _dateTimeString = getDateTime();
            _isGoukaku = "1".equals(request.getParameter("GOUKAKU"));
            _isHoin = "1".equals(request.getParameter("HOIN"));
        }

        private String getDateTime() {
            Calendar cal = Calendar.getInstance();
            DecimalFormat df = new DecimalFormat("00");
            final String hour = df.format(cal.get(Calendar.HOUR_OF_DAY));
            final String minute = df.format(cal.get(Calendar.MINUTE));
            
            Date date = Date.valueOf(_date);
            cal.setTime(date);
            String dayOfWeek = KNJ_EditDate.h_format_W(_date);
            
            return KNJ_EditDate.h_format_JP(_date) + "(" + dayOfWeek + ") " + hour + ":" + minute;
        }
    }
}

// eof

