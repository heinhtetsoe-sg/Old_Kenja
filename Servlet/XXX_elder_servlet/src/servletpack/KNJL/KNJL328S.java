/*
 * $Id: ad59d5fb4bc8447ea4f0550467f2c9050812a564 $
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
 *  学校教育システム 賢者 [入試処理] 鳥取・合否別一覧表
 */
public class KNJL328S {

    private static final Log log = LogFactory.getLog(KNJL328S.class);

    private boolean _hasData;

    Param _param;

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

            printMain(db2, svf);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL328S.frm", 4);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        String oldNmL013Namespare1 = "";
        String oldApplicantDiv = "";
        String oldWishMajorCd = "";
        
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                
                if (!"".equals(oldNmL013Namespare1) && oldNmL013Namespare1 != null && !oldNmL013Namespare1.equals(rs.getString("NML013_NAMESPARE1"))) {
                    svf.VrEndRecord();
                    
                    svf.VrsOut("TOTAL_NAME", "(管理計人数)");
                    svf.VrsOut("TOTAL_NUMBER", String.valueOf(count));
                    svf.VrEndRecord();

                    count = 0;
                    svf.VrSetForm("KNJL328S.frm", 4);
                } else if (
                        (!"".equals(oldApplicantDiv) && oldApplicantDiv != null && !oldApplicantDiv.equals(rs.getString("APPLICANTDIV"))) || 
                        (!"".equals(oldWishMajorCd) && oldWishMajorCd != null && !oldWishMajorCd.equals(rs.getString("WISH_MAJORCD")))) {
                    svf.VrEndRecord();
                    
                    svf.VrsOut("TOTAL_NAME", "(管理計人数)");
                    svf.VrsOut("TOTAL_NUMBER", String.valueOf(count));
                    svf.VrEndRecord();
                    
                    count = 0;
                }
                String title = "1".equals(rs.getString("NML013_NAMESPARE1")) ? "合格者一覧表(受検番号順)" : "不合格者一覧表(受検番号順)"; 
                svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._year + "-04-01") + "度　" + title);
                svf.VrsOut("DATE1", _param._dateTimeString);
                
                svf.VrsOut("EXAM_NO", String.valueOf(rs.getInt("EXAMNO")));
                svf.VrsOut("EXAM_DIV", ("0".equals(_param._applicantdiv) ? "全て" : rs.getString("APPLICANTDIVNAME")));
                svf.VrsOut("DIV", rs.getString("APPLICANTDIVNAME"));

                // 志願者(かな氏名)
                svf.VrsOut(setformatArea("NAME_KANA", rs.getString("NAME_KANA"), 8, "1", "2"), rs.getString("NAME_KANA"));
                // 志願者(氏名)
                svf.VrsOut(setformatArea("NAME", rs.getString("NAME"), 8, "1", "2"), rs.getString("NAME"));

                svf.VrsOut("SEX", rs.getString("SEXNAME"));
                svf.VrsOut("HOPE_SUBJECT", rs.getString("WISH_MAJOR"));
                svf.VrsOut("PASS_SUBJECT", rs.getString("SUC_MAJOR"));
                svf.VrsOut("JHSCHOOL_NAME", rs.getString("FINSCHOOL_NAME"));
                
                count += 1;
                oldNmL013Namespare1 = rs.getString("NML013_NAMESPARE1");
                oldApplicantDiv = rs.getString("APPLICANTDIV");
                oldWishMajorCd = rs.getString("WISH_MAJORCD");
                
                svf.VrEndRecord();
                _hasData = true;
            }
            
            if (_hasData) {
                svf.VrEndRecord();
                
                svf.VrsOut("TOTAL_NAME", "(管理計人数)");
                svf.VrsOut("TOTAL_NUMBER", String.valueOf(count));
                svf.VrEndRecord();
            }
            
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     NML013.NAMESPARE1 AS NML013_NAMESPARE1, ");
        stb.append("     W1.EXAMNO, ");
        stb.append("     W1.APPLICANTDIV, ");
        stb.append("     T3.NAME1 AS APPLICANTDIVNAME, ");
        stb.append("     W1.NAME, ");
        stb.append("     W1.NAME_KANA, ");
        stb.append("     W1.SEX, ");
        stb.append("     T4.NAME2 AS SEXNAME, ");
        stb.append("     VALUE(T1.FINSCHOOL_NAME_ABBV, T1.FINSCHOOL_NAME) AS FINSCHOOL_NAME, ");
        stb.append("     T6.MAJORCD AS WISH_MAJORCD, ");
        stb.append("     (CASE WHEN T5.MAJORSCD = '0' THEN T6.MAJORLABBV ELSE T6.MAJORSABBV END) AS WISH_MAJOR, ");
        stb.append("     W1.SUC_MAJORCD, ");
        stb.append("     (CASE WHEN W1.SUC_MAJORCD LIKE '%0' THEN T7.MAJORLABBV ELSE T7.MAJORSABBV END) AS SUC_MAJOR ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT W1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT W3 ON ");
        stb.append("         W1.ENTEXAMYEAR = W3.ENTEXAMYEAR ");
        stb.append("         and W1.EXAMNO = W3.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T1 ON ");
        stb.append("         W1.FS_CD = T1.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST T3 ON ");
        stb.append("         T3.NAMECD1 = 'L003' ");
        stb.append("         and T3.NAMECD2 = W1.APPLICANTDIV ");
        stb.append("     LEFT JOIN NAME_MST T4 ON ");
        stb.append("         T4.NAMECD1 = 'Z002' ");
        stb.append("         and T4.NAMECD2 = W1.SEX ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTWISH_DAT T5 ON ");
        stb.append("         W1.ENTEXAMYEAR = T5.ENTEXAMYEAR ");
        stb.append("         and W1.EXAMNO = T5.EXAMNO ");
        stb.append("         and T5.WISHNO = '1' ");
        stb.append("     LEFT JOIN V_ENTEXAM_MAJOR_MST T6 ON ");
        stb.append("         T6.MAJORCD = T5.MAJORLCD || MAJORSCD ");
        stb.append("     LEFT JOIN V_ENTEXAM_MAJOR_MST T7 ON ");
        stb.append("         T7.MAJORCD = W1.SUC_MAJORCD ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON ");
        stb.append("         NML013.NAMECD1 = 'L013' ");
        stb.append("         and NML013.NAMECD2 = W1.JUDGEMENT ");
        stb.append(" WHERE ");
        stb.append("     W1.ENTEXAMYEAR = '" + _param._year + "' ");
        if (!"0".equals(_param._applicantdiv)) {
            stb.append("     AND W1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     NML013.NAMESPARE1, ");
        stb.append("     W1.APPLICANTDIV, ");
        stb.append("     T6.MAJORCD, ");
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
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _date = request.getParameter("CTRL_DATE");
            
            _dateTimeString = getDateTime();
        }

        private String getDateTime() {
            Calendar cal = Calendar.getInstance();
            DecimalFormat df = new DecimalFormat("00");
            final String hour = df.format(cal.get(Calendar.HOUR_OF_DAY));
            final String minute = df.format(cal.get(Calendar.MINUTE));
            
            Date date = Date.valueOf(_date);
            cal.setTime(date);
            String dayOfWeek = new String[]{"", "日", "月", "火", "水", "木", "金", "土"}[cal.get(Calendar.DAY_OF_WEEK)];
            
            return KNJ_EditDate.h_format_JP(_date) + "(" + dayOfWeek + ") " + hour + ":" + minute;
        }
    }
}

// eof

