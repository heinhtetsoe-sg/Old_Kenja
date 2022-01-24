/*
 * $Id: 9085bf681a4c0ff7892a08b825b3172f9d7700ba $
 *
 * 作成日: 2019/01/10
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０８Ｒ＞  入試Ⅰと入試Ⅱとの突合せリスト
 **/
public class KNJL308R {

    private static final Log log = LogFactory.getLog(KNJL308R.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final int maxLine = 50;

        final List list = BaseDat.load(db2, _param);
        final List pageList = getPageList(list, maxLine);
        String[] nendo = KNJ_EditDate.tate_format4(db2,_param._entexamyear + "-04-01");

        for (int page = 0; page < pageList.size(); page++) {
            final List baseDatList = (List) pageList.get(page);

            svf.VrSetForm("KNJL308R.frm", 1);

            svf.VrsOut("TITLE", nendo[0] + nendo[1] + "年度　特待生/適性検査型入試　両受験者名簿"); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._date)); //作成日
            svf.VrsOut("PAGE1", String.valueOf(page + 1)); // ページ
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // 総ページ数

            for (int j = 0; j < baseDatList.size(); j++) {
                final BaseDat base = (BaseDat) baseDatList.get(j);
                final int line = j + 1;
                svf.VrsOutn("NO", line, base._no); // 管理番号
                final String nameField = getMS932Bytecount(base._name) > 20 ? "2" : "1";
                svf.VrsOutn("NAME1_" + nameField, line, base._name); // 氏名
                svf.VrsOutn("SEX", line, base._sex); // 性別
                svf.VrsOutn("JH_NAME1", line, base._finschoolName); // 出身学校
                svf.VrsOutn("EXAM_NO1", line, base._receptno1); // 特待生入試 受験番号
                svf.VrsOutn("CONSENT1", line, base._consent1); // 特待生入試 内諾
                svf.VrsOutn("EXAM_NO2", line, base._receptno2); // 適性検査型入試 受験番号
                svf.VrsOutn("CONSENT2", line, base._consent2); // 適性検査型入試 内諾
                svf.VrsOutn("REMARK", line, base._remark); // 備考
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private static class BaseDat {
        final String _no;
        final String _name;
        final String _sex;
        final String _finschoolName;
        final String _receptno1;
        final String _consent1;
        final String _receptno2;
        final String _consent2;
        final String _remark;

        BaseDat(
                final String no,
                final String name,
                final String sex,
                final String finschoolName,
                final String receptno1,
                final String consent1,
                final String receptno2,
                final String consent2,
                final String remark
        ) {
            _no = no;
            _name = name;
            _sex = sex;
            _finschoolName = finschoolName;
            _receptno1 = receptno1;
            _consent1 = consent1;
            _receptno2 = receptno2;
            _consent2 = consent2;
            _remark = remark;

        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String no = rs.getString("EXAMNO");
                    final String name = StringUtils.defaultString(rs.getString("NAME"));
                    final String sex = rs.getString("SEX");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String receptno1 = rs.getString("RECEPTNO1");
                    final String consent1 = rs.getString("CONSENT1");
                    final String receptno2 = rs.getString("RECEPTNO2");
                    final String consent2 = rs.getString("CONSENT2");
                    final String remark = rs.getString("REMARK");

                    final BaseDat basedat = new BaseDat(no, name, sex, finschoolName, receptno1, consent1, receptno2, consent2, remark);
                    list.add(basedat);

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
            stb.append(" SELECT  ");
            stb.append("     B1.EXAMNO,  ");
            stb.append("     B1.NAME,  ");
            stb.append("     NMZ002.ABBV1 AS SEX, ");
            stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
            stb.append("     BD2_012.REMARK1 AS RECEPTNO1, ");
            stb.append("     NML006_1.NAME2 || NML064_1.NAME1 AS CONSENT1, ");
            stb.append("     BD2_012.REMARK2 AS RECEPTNO2, ");
            stb.append("     NML006_2.NAME2 || NML064_2.NAME1 AS CONSENT2, ");
            stb.append("     '' AS REMARK ");
            stb.append(" FROM  ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT B1 ");
            // 入試区分が2つある受験生が対象
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_010 ON BD2_010.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("         AND BD2_010.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND BD2_010.EXAMNO       = B1.EXAMNO ");
            stb.append("         AND BD2_010.SEQ          = '010' ");
            stb.append("         AND BD2_010.REMARK1 IS NOT NULL ");
            stb.append("         AND BD2_010.REMARK2 IS NOT NULL ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD2 = B1.SEX ");
            stb.append("         AND NMZ002.NAMECD1 = 'Z002' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_012 ON BD2_012.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("         AND BD2_012.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND BD2_012.EXAMNO       = B1.EXAMNO ");
            stb.append("         AND BD2_012.SEQ          = '012' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_013 ON BD2_013.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("         AND BD2_013.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND BD2_013.EXAMNO       = B1.EXAMNO ");
            stb.append("         AND BD2_013.SEQ          = '013' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_014 ON BD2_014.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("         AND BD2_014.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND BD2_014.EXAMNO       = B1.EXAMNO ");
            stb.append("         AND BD2_014.SEQ          = '014' ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON FINSCHOOL.FINSCHOOLCD = B1.FS_CD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD1 ON BD1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("         AND BD1.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND BD1.EXAMNO       = B1.EXAMNO ");
            stb.append("         AND BD1.SEQ          = '002' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD1_006 ON BD1_006.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("         AND BD1_006.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND BD1_006.EXAMNO       = B1.EXAMNO ");
            stb.append("         AND BD1_006.SEQ          = '006' ");
            stb.append("     LEFT JOIN NAME_MST NML006_1 ");
            stb.append("          ON NML006_1.NAMECD2 = BD2_013.REMARK1 ");
            stb.append("         AND NML006_1.NAMECD1 = 'L006' ");
            stb.append("     LEFT JOIN NAME_MST NML064_1 ");
            stb.append("          ON NML064_1.NAMECD2 = BD2_014.REMARK1 ");
            stb.append("         AND NML064_1.NAMECD1 = 'L064' ");
            stb.append("     LEFT JOIN NAME_MST NML006_2 ");
            stb.append("          ON NML006_2.NAMECD2 = BD2_013.REMARK2 ");
            stb.append("         AND NML006_2.NAMECD1 = 'L006' ");
            stb.append("     LEFT JOIN NAME_MST NML064_2 ");
            stb.append("          ON NML064_2.NAMECD2 = BD2_014.REMARK2 ");
            stb.append("         AND NML064_2.NAMECD1 = 'L064' ");
            stb.append(" WHERE  ");
            stb.append("     B1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND B1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append(" ORDER BY ");
            stb.append("     B1.EXAMNO ");
            return stb.toString();
        }
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65339 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _date;
        final String _prgid;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _date = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
        }
    }
}

// eof
