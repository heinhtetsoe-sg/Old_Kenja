/*
 * $Id: a35df98340077fd22a4bd7bfa0e92dcbe1139208 $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 *                  ＜ＫＮＪＬ３３１Ｒ＞  入学者一覧
 **/
public class KNJL331R {

    private static final Log log = LogFactory.getLog(KNJL331R.class);

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
        String print = "1".equals(_param._printDiv) ? "合格者" :"入学者";
        int renban = 1;

        for (int page = 0; page < pageList.size(); page++) {
            final List baseDatList = (List) pageList.get(page);

            svf.VrSetForm("KNJL331R.frm", 1);

            svf.VrsOut("TITLE", _param._entexamyear + "年度　入学試験　" + print + "名簿"); // タイトル
            svf.VrsOut("SCHOOLNAME", (String)_param._certifSchoolMap.get("SCHOOL_NAME")); //学校名
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._date)); //作成日
            svf.VrsOut("PAGE", String.valueOf(page + 1)); // ページ
            svf.VrsOut("TOTAL_PAGE", String.valueOf(pageList.size())); // 総ページ数

            for (int j = 0; j < baseDatList.size(); j++) {
                final BaseDat base = (BaseDat) baseDatList.get(j);
                final int line = j + 1;
                svf.VrsOutn("NUMBER", line, String.valueOf(renban++)); // No.
                svf.VrsOutn("EXAMNO", line, base._examno); // 受験番号
                final String nameField = getMS932Bytecount(base._name) > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, line, base._name); // 生徒氏名
                final String kanaField = getMS932Bytecount(base._kana) > 20 ? "2" : "";
                svf.VrsOutn("KANA" + kanaField, line, base._kana); // カナ氏名
                svf.VrsOutn("SEX", line, base._sex); // 性別
                final String schoolField = getMS932Bytecount(base._finschoolName) > 26 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL" + schoolField, line, base._finschoolName); // 出身校
                final String gnameField = getMS932Bytecount(base._gname) > 20 ? "2" : "1";
                svf.VrsOutn("GUARD_NAME"  + gnameField, line, base._gname); // 保護者名
                svf.VrsOutn("ZIPCODE", line, base._zipcd); // 郵便番号
                final String addrField = getMS932Bytecount(base._address) > 50 ? "2" : "1";
                svf.VrsOutn("ADDRESS" + addrField, line, base._address); // 住所1
                svf.VrsOutn("TELNO", line, base._telno); // 電話番号
                svf.VrsOutn("REMARK", line, base._remark); // 備考
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
        final String _kana;
        final String _sex;
        final String _zipcd;
        final String _address;
        final String _telno;
        final String _gname;
        final String _finschoolName;

        final String _remark;

        BaseDat(
                final String examno,
                final String name,
                final String kana,
                final String sex,
                final String zipcd,
                final String address,
                final String telno,
                final String gname,
                final String finschoolName,
                final String remark
        ) {
            _examno = examno;
            _name = name;
            _kana = kana;
            _sex = sex;
            _zipcd = zipcd;
            _address = address;
            _telno = telno;
            _gname = gname;
            _finschoolName = finschoolName;
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
                	final String examno = rs.getString("EXAMNO");
                    final String name = StringUtils.defaultString(rs.getString("NAME"));
                    final String kana = StringUtils.defaultString(rs.getString("NAME_KANA"));
                    final String sex = rs.getString("SEX");
                    final String zipcd = rs.getString("ZIPCD");
                    final String address = StringUtils.defaultString(rs.getString("ADDRESS"));
                    final String telno = rs.getString("TELNO");
                    final String gname = StringUtils.defaultString(rs.getString("GNAME"));
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String remark = rs.getString("REMARK");
                    final BaseDat basedat = new BaseDat(examno, name, kana, sex, zipcd, address, telno, gname, finschoolName, remark);
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
            stb.append("     NMZ002.ABBV1 AS SEX, ");
            stb.append("     ADDR1.ZIPCD, ");
            stb.append("     VALUE(ADDR1.ADDRESS1,'') || VALUE(ADDR1.ADDRESS2,'') AS ADDRESS, ");
            stb.append("     ADDR1.TELNO, ");
            stb.append("     ADDR1.GNAME, ");
            stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
            stb.append("     '' AS REMARK ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR1 ON ADDR1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("         AND ADDR1.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("         AND ADDR1.EXAMNO       = B1.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD2 = B1.SEX ");
            stb.append("         AND NMZ002.NAMECD1 = 'Z002' ");
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
            stb.append("     AND NML013.NAMESPARE1   = '1' ");
            if("2".equals(param._printDiv)) {
                stb.append("     AND B1.PROCEDUREDIV = '1' ");
                stb.append("     AND B1.ENTDIV       = '1' ");
            }
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
        log.fatal("$Revision: 65477 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _printDiv;
        final String _date;
        final String _prgid;
        final Map _certifSchoolMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _printDiv = request.getParameter("PRINT_DIV");
            _date = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _certifSchoolMap = getCertifScholl(db2);
        }

        private Map getCertifScholl(final DB2UDB db2) {
            final Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '105' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put("CORP_NAME", rs.getString("REMARK6"));
                    rtnMap.put("SCHOOL_NAME", rs.getString("SCHOOL_NAME"));
                    rtnMap.put("JOB_NAME", rs.getString("JOB_NAME"));
                    rtnMap.put("PRINCIPAL_NAME", rs.getString("PRINCIPAL_NAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnMap;
        }
    }

}

// eof
