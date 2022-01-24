/*
 * $Id: 84de87502bf0cbc8fd54977b64972f4c7ec70086 $
 *
 * 作成日: 2019/01/11
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
 *                  ＜ＫＮＪＬ３３２Ｒ＞  オリエンテーション名簿
 **/
public class KNJL332R {

    private static final Log log = LogFactory.getLog(KNJL332R.class);

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
        int renban = 1;
        int manCnt = 0;
        int womanCnt = 0;

        for (int page = 0; page < pageList.size(); page++) {
            final List baseDatList = (List) pageList.get(page);

            svf.VrSetForm("KNJL332R.frm", 1);

            svf.VrsOut("NENDO", nendo[0] + nendo[1] + "年度"); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._date)); //作成日
            svf.VrsOut("PAGE", String.valueOf(page + 1)); // ページ
            svf.VrsOut("TOTAL_PAGE", String.valueOf(pageList.size())); // 総ページ数

            for (int j = 0; j < baseDatList.size(); j++) {
                final BaseDat base = (BaseDat) baseDatList.get(j);
                final int line = j + 1;
                svf.VrsOutn("NO", line, String.valueOf(renban++)); // No.
                svf.VrsOutn("EXAMNO", line, base._examno); // 受験番号
                final String nameField = getMS932Bytecount(base._name) > 22 ? "2" : "";
                svf.VrsOutn("NAME" + nameField, line, base._name); // 生徒氏名
                final String kanaField = getMS932Bytecount(base._nameKana) > 24 ? "2" : "";
                svf.VrsOutn("KANA" + kanaField, line, base._nameKana); // カナ氏名
                svf.VrsOutn("SEX", line, base._sexName); // 性別
                svf.VrsOutn("BIRTHDAY", line, KNJ_EditDate.h_format_JP(db2,base._birthday)); // 生年月日
                svf.VrsOutn("FINSCHOOL", line, base._finschoolName); // 小学校名
                if("1".equals(base._sex)) {
                    manCnt++;
                }else {
                    womanCnt++;
                }

            }
            if( (page + 1) == pageList.size()) {
                svf.VrsOut("NOTE", "男" + manCnt + "名,女" + womanCnt + "名,合計" + (manCnt + womanCnt) + "名"); // 人数
            }

            _hasData = true;
            svf.VrEndPage();
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
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _sexName;
        final String _birthday;
        final String _finschoolName;

        BaseDat(
                final String examno,
                final String name,
                final String nameKana,
                final String sex,
                final String sexName,
                final String birthday,
                final String finschoolName
        ) {
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _sexName = sexName;
            _birthday = birthday;
            _finschoolName = finschoolName;
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
                    final String examno = rs.getString("EXAMNO");
                    final String name = StringUtils.defaultString(rs.getString("NAME"));
                    final String nameKana = StringUtils.defaultString(rs.getString("NAME_KANA"));
                    final String sex = StringUtils.defaultString(rs.getString("SEX"));
                    final String sexName = StringUtils.defaultString(rs.getString("SEX_NAME"));
                    final String birthday = StringUtils.defaultString(rs.getString("BIRTHDAY"));
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final BaseDat basedat = new BaseDat(examno, name, nameKana, sex, sexName, birthday, finschoolName);
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
            stb.append(" SELECT ");
            stb.append("     BD030.REMARK2 AS EXAMNO, ");
            stb.append("     B1.NAME, ");
            stb.append("     B1.NAME_KANA, ");
            stb.append("     B1.SEX, ");
            stb.append("     NMZ002.ABBV1 AS SEX_NAME, ");
            stb.append("     B1.BIRTHDAY, ");
            stb.append("     FINSCHOOL.FINSCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD2 = B1.SEX ");
            stb.append("         AND NMZ002.NAMECD1 = 'Z002' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR1 ON ADDR1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("         AND ADDR1.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND ADDR1.EXAMNO       = B1.EXAMNO ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON FINSCHOOL.FINSCHOOLCD = B1.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD2 = B1.JUDGEMENT ");
            stb.append("         AND NML013.NAMECD1 = 'L013' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD030 ON BD030.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("         AND BD030.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND BD030.EXAMNO       = B1.EXAMNO ");
            stb.append("         AND BD030.SEQ          = '030' ");
            stb.append(" WHERE ");
            stb.append("     B1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND B1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND NML013.NAMESPARE1 = '1' ");
            stb.append("     AND B1.PROCEDUREDIV   = '1' ");
            stb.append("     AND B1.ENTDIV         = '1' ");
            stb.append(" ORDER BY ");
            if("1".equals(param._output)) {
                stb.append("     B1.EXAMNO ");
            }else {
                stb.append("     B1.NAME_KANA ");
            }

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
        log.fatal("$Revision: 65478 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _output;
        final String _date;
        final String _prgid;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _output = request.getParameter("OUTPUT");
            _date = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
        }

    }

}

// eof
