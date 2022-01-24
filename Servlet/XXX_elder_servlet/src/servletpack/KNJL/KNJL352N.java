/*
 * $Id: 5dae0a14db2e6659bbdc669b82d230ff257677ad $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

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
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３５０Ｎ＞  入学試験受験者入試台帳
 **/
public class KNJL352N {

    private static final Log log = LogFactory.getLog(KNJL352N.class);

    private boolean _hasData;

    private Param _param;

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
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJL352N.frm";
        final int maxLine = 40;

        final List allApplicantList = Applicant.getApplicantList(db2, _param);

        final List pageList = getPageList(allApplicantList, maxLine);

        final String nendoOld2 = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._oldYear2));
        final String nendoOld1 = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._oldYear1));
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear));

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);

            svf.VrSetForm(form, 1);

			svf.VrsOut("TITLE", nendo + "年度　高等学校入試　生徒募集部資料"); // タイトル
            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ分子
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ分母
            svf.VrsOut("DATE", _param._dateStr); // 印刷日時

			svf.VrsOut("YEAR1_1", nendoOld2 + "年度"); // 年度
			svf.VrsOut("YEAR1_2", nendoOld1 + "年度"); // 年度
            svf.VrsOut("YEAR1_3", nendo + "年度"); // 年度
            svf.VrsOut("YEAR2_1", nendoOld2 + "年度"); // 年度
            svf.VrsOut("YEAR2_2", nendoOld1 + "年度"); // 年度
            svf.VrsOut("YEAR2_3", nendo + "年度"); // 年度

            for (int j = 0; j < applicantList.size(); j++) {
                final Applicant appl = (Applicant) applicantList.get(j);
                final int line = j + 1;
                svf.VrsOutn("APPLICANT_NUM1_1", line, appl._applicant_num1_1); // 推薦
                svf.VrsOutn("APPLICANT_NUM1_2", line, appl._applicant_num1_2); // 専願
                svf.VrsOutn("APPLICANT_NUM1_3", line, appl._applicant_num1_3); // 一般
                svf.VrsOutn("APPLICANT_NUM1_4", line, appl._applicant_num1_4); // 受験者
                svf.VrsOutn("APPLICANT_NUM2_1", line, appl._applicant_num2_1); // 推薦
                svf.VrsOutn("APPLICANT_NUM2_2", line, appl._applicant_num2_2); // 専願
                svf.VrsOutn("APPLICANT_NUM2_3", line, appl._applicant_num2_3); // 一般受験者
                svf.VrsOutn("APPLICANT_NUM2_4", line, appl._applicant_num2_4); // 受験者
                svf.VrsOutn("APPLICANT_NUM3_1", line, appl._applicant_num3_1); // 推薦
                svf.VrsOutn("APPLICANT_NUM3_2", line, appl._applicant_num3_2); // 専願
                svf.VrsOutn("APPLICANT_NUM3_3", line, appl._applicant_num3_3); // 一般
                svf.VrsOutn("APPLICANT_NUM3_4", line, appl._applicant_num3_4); // 受験者
                svf.VrsOutn("APPLICANT_NUM5_1", line, appl._applicant_num5_1); // 推薦(3ヵ年)
                svf.VrsOutn("APPLICANT_NUM5_2", line, appl._applicant_num5_2); // 専願(3ヵ年)
                svf.VrsOutn("APPLICANT_NUM5_3", line, appl._applicant_num5_3); // 一般(3ヵ年)
                svf.VrsOutn("APPLICANT_NUM5_4", line, appl._applicant_num5_4); // 受験者(3ヵ年)
                svf.VrsOutn("FINSCHOOL_NAME", line, appl._finschoolNameAbbv); // 出身学校略称
                svf.VrsOutn("ENT_NUM_TOTAL", line, appl._ent_num_total); // 入学者(3ヵ年)
                svf.VrsOutn("ENT_NUM1", line, appl._ent_num1); // 入学者
                svf.VrsOutn("ENT_NUM2", line, appl._ent_num2); // 入学者
                svf.VrsOutn("ENT_NUM3", line, appl._ent_num3); // 入学者
                svf.VrsOutn("ENT_NUM5_1", line, appl._ent_num5_1); // 入学者(推薦)
                svf.VrsOutn("ENT_NUM5_2", line, appl._ent_num5_2); // 入学者(専願)
                svf.VrsOutn("ENT_NUM5_3", line, appl._ent_num5_3); // 入学者(一般)
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Applicant o = (Applicant) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static class Applicant {
        final String _fs_cd;
        final String _applicant_num1_1;
        final String _applicant_num1_2;
        final String _applicant_num1_3;
        final String _applicant_num1_4;
        final String _applicant_num2_1;
        final String _applicant_num2_2;
        final String _applicant_num2_3;
        final String _applicant_num2_4;
        final String _applicant_num3_1;
        final String _applicant_num3_2;
        final String _applicant_num3_3;
        final String _applicant_num3_4;
        final String _applicant_num5_1;
        final String _applicant_num5_2;
        final String _applicant_num5_3;
        final String _applicant_num5_4;
        final String _finschoolNameAbbv;
        final String _ent_num_total;
        final String _ent_num1;
        final String _ent_num2;
        final String _ent_num3;
        final String _ent_num5_1;
        final String _ent_num5_2;
        final String _ent_num5_3;

        Applicant(
            final String fs_cd,
            final String applicant_num1_1,
            final String applicant_num1_2,
            final String applicant_num1_3,
            final String applicant_num1_4,
            final String applicant_num2_1,
            final String applicant_num2_2,
            final String applicant_num2_3,
            final String applicant_num2_4,
            final String applicant_num3_1,
            final String applicant_num3_2,
            final String applicant_num3_3,
            final String applicant_num3_4,
            final String applicant_num5_1,
            final String applicant_num5_2,
            final String applicant_num5_3,
            final String applicant_num5_4,
            final String finschoolNameAbbv,
            final String ent_num_total,
            final String ent_num1,
            final String ent_num2,
            final String ent_num3,
            final String ent_num5_1,
            final String ent_num5_2,
            final String ent_num5_3
        ) {
            _fs_cd = fs_cd;
            _applicant_num1_1 = applicant_num1_1;
            _applicant_num1_2 = applicant_num1_2;
            _applicant_num1_3 = applicant_num1_3;
            _applicant_num1_4 = applicant_num1_4;
            _applicant_num2_1 = applicant_num2_1;
            _applicant_num2_2 = applicant_num2_2;
            _applicant_num2_3 = applicant_num2_3;
            _applicant_num2_4 = applicant_num2_4;
            _applicant_num3_1 = applicant_num3_1;
            _applicant_num3_2 = applicant_num3_2;
            _applicant_num3_3 = applicant_num3_3;
            _applicant_num3_4 = applicant_num3_4;
            _applicant_num5_1 = applicant_num5_1;
            _applicant_num5_2 = applicant_num5_2;
            _applicant_num5_3 = applicant_num5_3;
            _applicant_num5_4 = applicant_num5_4;
            _finschoolNameAbbv = finschoolNameAbbv;
            _ent_num_total = ent_num_total;
            _ent_num1 = ent_num1;
            _ent_num2 = ent_num2;
            _ent_num3 = ent_num3;
            _ent_num5_1 = ent_num5_1;
            _ent_num5_2 = ent_num5_2;
            _ent_num5_3 = ent_num5_3;
        }

        public static List getApplicantList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String fs_cd = rs.getString("FS_CD");
                    final String applicant_num1_1 = rs.getString("APPLICANT_NUM1_1");
                    final String applicant_num1_2 = rs.getString("APPLICANT_NUM1_2");
                    final String applicant_num1_3 = rs.getString("APPLICANT_NUM1_3");
                    final String applicant_num1_4 = rs.getString("APPLICANT_NUM1_4");
                    final String applicant_num2_1 = rs.getString("APPLICANT_NUM2_1");
                    final String applicant_num2_2 = rs.getString("APPLICANT_NUM2_2");
                    final String applicant_num2_3 = rs.getString("APPLICANT_NUM2_3");
                    final String applicant_num2_4 = rs.getString("APPLICANT_NUM2_4");
                    final String applicant_num3_1 = rs.getString("APPLICANT_NUM3_1");
                    final String applicant_num3_2 = rs.getString("APPLICANT_NUM3_2");
                    final String applicant_num3_3 = rs.getString("APPLICANT_NUM3_3");
                    final String applicant_num3_4 = rs.getString("APPLICANT_NUM3_4");
                    final String applicant_num5_1 = rs.getString("APPLICANT_NUM5_1");
                    final String applicant_num5_2 = rs.getString("APPLICANT_NUM5_2");
                    final String applicant_num5_3 = rs.getString("APPLICANT_NUM5_3");
                    final String applicant_num5_4 = rs.getString("APPLICANT_NUM5_4");
                    final String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                    final String ent_num_total = rs.getString("ENT_NUM_TOTAL");
                    final String ent_num1 = rs.getString("ENT_NUM1");
                    final String ent_num2 = rs.getString("ENT_NUM2");
                    final String ent_num3 = rs.getString("ENT_NUM3");
                    final String ent_num5_1 = rs.getString("ENT_NUM5_1");
                    final String ent_num5_2 = rs.getString("ENT_NUM5_2");
                    final String ent_num5_3 = rs.getString("ENT_NUM5_3");

                    final Applicant applicant = new Applicant(fs_cd, applicant_num1_1, applicant_num1_2, applicant_num1_3, applicant_num1_4, applicant_num2_1, applicant_num2_2, applicant_num2_3, applicant_num2_4, applicant_num3_1, applicant_num3_2, applicant_num3_3, applicant_num3_4, applicant_num5_1, applicant_num5_2, applicant_num5_3, applicant_num5_4, finschoolNameAbbv, ent_num_total, ent_num1, ent_num2, ent_num3, ent_num5_1, ent_num5_2, ent_num5_3);
                    list.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_BASE AS ( ");
            stb.append("     SELECT ");
            stb.append("         BASE.FS_CD, ");
            stb.append("         FIN.FINSCHOOL_NAME_ABBV, ");
            stb.append("         BASE.ENTEXAMYEAR, ");
            stb.append("         BASE.SHDIV, ");
            stb.append("         BASE.ENTDIV ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("         INNER JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     WHERE ");
            stb.append("         BASE.ENTEXAMYEAR BETWEEN '" + param._oldYear2 + "' AND '" + param._entexamyear + "' ");
            stb.append("         AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("         AND BASE.JUDGEMENT NOT IN ('4','5') ");
            stb.append(" ) ");

            stb.append(" SELECT  ");
            stb.append("     T1.FS_CD, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._oldYear2 + "'    AND T1.SHDIV  = '1' THEN 1 ELSE 0 END) AS APPLICANT_NUM1_1, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._oldYear2 + "'    AND T1.SHDIV  = '2' THEN 1 ELSE 0 END) AS APPLICANT_NUM1_2, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._oldYear2 + "'    AND T1.SHDIV  = '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM1_3, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._oldYear2 + "'    AND T1.SHDIV <= '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM1_4, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._oldYear1 + "'    AND T1.SHDIV  = '1' THEN 1 ELSE 0 END) AS APPLICANT_NUM2_1, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._oldYear1 + "'    AND T1.SHDIV  = '2' THEN 1 ELSE 0 END) AS APPLICANT_NUM2_2, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._oldYear1 + "'    AND T1.SHDIV  = '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM2_3, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._oldYear1 + "'    AND T1.SHDIV <= '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM2_4, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._entexamyear + "' AND T1.SHDIV  = '1' THEN 1 ELSE 0 END) AS APPLICANT_NUM3_1, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._entexamyear + "' AND T1.SHDIV  = '2' THEN 1 ELSE 0 END) AS APPLICANT_NUM3_2, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._entexamyear + "' AND T1.SHDIV  = '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM3_3, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._entexamyear + "' AND T1.SHDIV <= '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM3_4, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR BETWEEN '" + param._oldYear2 + "' AND '" + param._entexamyear + "' AND T1.SHDIV  = '1' THEN 1 ELSE 0 END) AS APPLICANT_NUM5_1, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR BETWEEN '" + param._oldYear2 + "' AND '" + param._entexamyear + "' AND T1.SHDIV  = '2' THEN 1 ELSE 0 END) AS APPLICANT_NUM5_2, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR BETWEEN '" + param._oldYear2 + "' AND '" + param._entexamyear + "' AND T1.SHDIV  = '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM5_3, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR BETWEEN '" + param._oldYear2 + "' AND '" + param._entexamyear + "' AND T1.SHDIV <= '3' THEN 1 ELSE 0 END) AS APPLICANT_NUM5_4, ");
            stb.append("     T1.FINSCHOOL_NAME_ABBV, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR BETWEEN '" + param._oldYear2 + "' AND '" + param._entexamyear + "' AND T1.SHDIV <= '3' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM_TOTAL, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._oldYear2 + "'    AND T1.SHDIV <= '3' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM1, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._oldYear1 + "'    AND T1.SHDIV <= '3' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM2, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._entexamyear + "' AND T1.SHDIV <= '3' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM3, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._entexamyear + "' AND T1.SHDIV  = '1' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM5_1, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._entexamyear + "' AND T1.SHDIV  = '2' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM5_2, ");
            stb.append("     SUM(CASE WHEN T1.ENTEXAMYEAR = '" + param._entexamyear + "' AND T1.SHDIV  = '3' AND T1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_NUM5_3 ");
            stb.append(" FROM T_BASE T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.FS_CD, ");
            stb.append("     T1.FINSCHOOL_NAME_ABBV ");
            stb.append(" ORDER BY ");
            stb.append("     T1.FS_CD ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 64679 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _date;

        final String _oldYear2;
        final String _oldYear1;
        final String _applicantdivAbbv1;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _oldYear2 = String.valueOf(Integer.parseInt(_entexamyear)-2);
            _oldYear1 = String.valueOf(Integer.parseInt(_entexamyear)-1);
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(db2, _date);
            _applicantdivAbbv1 = getNameMst(db2, "ABBV1", "L003", _applicantdiv);
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.h_format_JP(db2, date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

