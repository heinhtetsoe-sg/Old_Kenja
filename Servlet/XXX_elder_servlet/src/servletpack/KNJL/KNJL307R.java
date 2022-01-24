/*
 * $Id: fd26efec6662956ec1b068f2f7170a9865b0a3d8 $
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
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０７Ｒ＞  入試区分別願書受付一覧
 **/
public class KNJL307R {

    private static final Log log = LogFactory.getLog(KNJL307R.class);

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

        final int maxLine = 25;

        final List list = BaseDat.load(db2, _param);
        final List pageList = getPageList(list, maxLine);
        String[] nendo = KNJ_EditDate.tate_format4(db2,_param._entexamyear + "-04-01");

        for (int page = 0; page < pageList.size(); page++) {
            final List baseDatList = (List) pageList.get(page);

            svf.VrSetForm("KNJL307R.frm", 1);

            svf.VrsOut("TITLE", nendo[0] + nendo[1] + "年度　" + _param._testdivName + "願書受付一覧"); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._date)); //作成日
            svf.VrsOut("PAGE1", String.valueOf(page + 1)); // ページ
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // 総ページ数

            for (int j = 0; j < baseDatList.size(); j++) {
                final BaseDat base = (BaseDat) baseDatList.get(j);
                final int line = j + 1;
                final String[] birthday = KNJ_EditDate.tate_format4(db2,base._birthday);
                svf.VrsOutn("NO", line, base._examno); // 管理番号
                svf.VrsOutn("EXAM_NO", line, base._receptno); // 受験番号
                svf.VrsOutn("HOPE", line, base._hope); // 専願併願
                final String name1Field = getMS932Bytecount(base._name) > 20 ? "2" : "1";
                svf.VrsOutn("NAME1_" + name1Field, line, base._name); // 名前
                final String kanaField = getMS932Bytecount(base._nameKana) > 30 ? "2" : "1";
                svf.VrsOutn("KANA" + kanaField, line, base._nameKana); // ふりがな
                svf.VrsOutn("BIRTHDAY", line, birthday[0] + birthday[1] + "." + birthday[2] + "." + birthday[3]); // 生年月日
                svf.VrsOutn("SEX", line, base._sex); // 性別
                svf.VrsOutn("ZIP_NO", line, base._zipcd); // 郵便番号
                final String addr1Field = getMS932Bytecount(base._address1) > 40 ? "2" : "1";
                svf.VrsOutn("ADDR1_" + addr1Field, line, base._address1); // 住所1
                final String addr2Field = getMS932Bytecount(base._address2) > 40 ? "2" : "1";
                svf.VrsOutn("ADDR2_" + addr2Field, line, base._address2); // 住所2
                svf.VrsOutn("TEL_NO1", line, base._telno); // 電話番号
                final String name2Field = getMS932Bytecount(base._gname) > 20 ? "2" : "1";
                svf.VrsOutn("NAME2_"  + name2Field, line, base._gname); // 保護者名
                svf.VrsOutn("TEL_NO2", line, base._gtelno); // 保護者電話番号
                svf.VrsOutn("JH_CODE", line, base._fsCd); // 出身校コード
                svf.VrsOutn("JH_NAME1", line, base._finschoolName); // 出身学校名
                svf.VrsOutn("EXAM_NO2", line, base._receptno2); // Ⅰ/Ⅱの受験番号
                svf.VrsOutn("ABSENT4", line, base._absent4); // 欠席日数
                svf.VrsOutn("CONSENT", line, base._consent); // 内諾
                final List remarkList = KNJ_EditKinsoku.getTokenList(base._remark, 40);
                for (int i = 0; i < remarkList.size(); i++) {
                	final String remark = (String) remarkList.get(i);
                	svf.VrsOutn("REMARK" + String.valueOf(i + 1), line, remark); // 備考
                }
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
        final String _receptno;
        final String _hope;
        final String _name;
        final String _nameKana;
        final String _birthday;
        final String _sex;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _telno;
        final String _gname;
        final String _gtelno;
        final String _fsCd;
        final String _finschoolName;
        final String _receptno2;
        final String _absent4;
        final String _consent;
        final String _remark;

        BaseDat(
                final String examno,
                final String receptno,
                final String hope,
                final String name,
                final String nameKana,
                final String birthday,
                final String sex,
                final String zipcd,
                final String address1,
                final String address2,
                final String telno,
                final String gname,
                final String gtelno,
                final String fsCd,
                final String finschoolName,
                final String receptno2,
                final String absent4,
                final String consent,
                final String remark
        ) {
            _examno = examno;
            _receptno = receptno;
            _hope = hope;
            _name = name;
            _nameKana = nameKana;
            _birthday = birthday;
            _sex = sex;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _telno = telno;
            _gname = gname;
            _gtelno = gtelno;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _receptno2 = receptno2;
            _absent4 = absent4;
            _consent = consent;
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
                    final String receptno = rs.getString("RECEPTNO");
                    final String hope = rs.getString("HOPE");
                    final String name = StringUtils.defaultString(rs.getString("NAME"));
                    final String nameKana = StringUtils.defaultString(rs.getString("NAME_KANA"));
                    final String birthday = StringUtils.defaultString(rs.getString("BIRTHDAY"));
                    final String sex = rs.getString("SEX");
                    final String zipcd = rs.getString("ZIPCD");
                    final String address1 = StringUtils.defaultString(rs.getString("ADDRESS1"));
                    final String address2 = StringUtils.defaultString(rs.getString("ADDRESS2"));
                    final String telno = rs.getString("TELNO");
                    final String gname = StringUtils.defaultString(rs.getString("GNAME"));
                    final String gtelno = rs.getString("GTELNO");
                    final String fsCd = rs.getString("FS_CD");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String receptno2 = rs.getString("RECEPTNO2");
                    final String absent4 = rs.getString("ABSENT4");
                    final String consent = rs.getString("CONSENT");
                    final String remark = rs.getString("REMARK");

                    final BaseDat basedat = new BaseDat(examno, receptno, hope, name, nameKana, birthday, sex, zipcd, address1, address2, telno, gname, gtelno, fsCd, finschoolName, receptno2, absent4, consent,remark);
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
            if("1".equals(param._testdiv)) {
                stb.append("     BD2_012.REMARK1 AS RECEPTNO, ");
                stb.append("     CASE WHEN BD2_013.REMARK1 = '1' THEN '○' ");
                stb.append("          ELSE '' ");
                stb.append("     END AS HOPE, ");
            }else {
                stb.append("     BD2_012.REMARK2 AS RECEPTNO, ");
                stb.append("     CASE WHEN BD2_013.REMARK2 = '1' THEN '○' ");
                stb.append("          ELSE '' ");
                stb.append("     END AS HOPE, ");
            }
            stb.append("     B1.NAME,  ");
            stb.append("     B1.NAME_KANA,  ");
            stb.append("     B1.BIRTHDAY, ");
            stb.append("     NMZ002.ABBV1 AS SEX, ");
            stb.append("     ADDR1.ZIPCD, ");
            stb.append("     ADDR1.ADDRESS1, ");
            stb.append("     ADDR1.ADDRESS2, ");
            stb.append("     ADDR1.TELNO, ");
            stb.append("     ADDR1.GNAME, ");
            stb.append("     ADDR1.GTELNO, ");
            stb.append("     B1.FS_CD,  ");
            stb.append("     FINSCHOOL.FINSCHOOL_NAME,  ");
            if("1".equals(param._testdiv)) {
                stb.append("     BD2_012.REMARK2 AS RECEPTNO2, ");
            }else{
                stb.append("     BD2_012.REMARK1 AS RECEPTNO2, ");
            }
            stb.append("     VALUE(INTEGER(BD1_006.REMARK5),0) + VALUE(INTEGER(BD1_006.REMARK6),0) AS ABSENT4, ");
            stb.append("     NML064.NAME1 AS CONSENT, ");
            stb.append("     B1.REMARK1 AS REMARK ");
            stb.append(" FROM  ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_010 ON BD2_010.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
            stb.append("         AND BD2_010.APPLICANTDIV = B1.APPLICANTDIV  ");
            stb.append("         AND BD2_010.EXAMNO       = B1.EXAMNO  ");
            stb.append("         AND BD2_010.SEQ          = '010'  ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_012 ON BD2_012.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
            stb.append("         AND BD2_012.APPLICANTDIV = B1.APPLICANTDIV  ");
            stb.append("         AND BD2_012.EXAMNO       = B1.EXAMNO  ");
            stb.append("         AND BD2_012.SEQ          = '012'  ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_013 ON BD2_013.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
            stb.append("         AND BD2_013.APPLICANTDIV = B1.APPLICANTDIV  ");
            stb.append("         AND BD2_013.EXAMNO       = B1.EXAMNO  ");
            stb.append("         AND BD2_013.SEQ          = '013'  ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_014 ON BD2_014.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
            stb.append("         AND BD2_014.APPLICANTDIV = B1.APPLICANTDIV  ");
            stb.append("         AND BD2_014.EXAMNO       = B1.EXAMNO  ");
            stb.append("         AND BD2_014.SEQ          = '014'  ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR1 ON ADDR1.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
            stb.append("         AND ADDR1.APPLICANTDIV = B1.APPLICANTDIV  ");
            stb.append("         AND ADDR1.EXAMNO       = B1.EXAMNO  ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD2 = B1.SEX ");
            stb.append("         AND NMZ002.NAMECD1 = 'Z002'  ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON FINSCHOOL.FINSCHOOLCD = B1.FS_CD  ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT BD1_006 ON BD1_006.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
            stb.append("         AND BD1_006.APPLICANTDIV = B1.APPLICANTDIV  ");
            stb.append("         AND BD1_006.EXAMNO       = B1.EXAMNO  ");
            stb.append("         AND BD1_006.SEQ          = '006' ");
            if("1".equals(param._testdiv)) {
                stb.append("     LEFT JOIN NAME_MST NML006  ");
                stb.append("          ON NML006.NAMECD2 = BD2_013.REMARK1 ");
                stb.append("         AND NML006.NAMECD1 = 'L006'  ");
                stb.append("     LEFT JOIN NAME_MST NML064  ");
                stb.append("          ON NML064.NAMECD2 = BD2_014.REMARK1 ");
                stb.append("         AND NML064.NAMECD1 = 'L064'  ");
            }else {
                stb.append("     LEFT JOIN NAME_MST NML006  ");
                stb.append("          ON NML006.NAMECD2 = BD2_013.REMARK2 ");
                stb.append("         AND NML006.NAMECD1 = 'L006'  ");
                stb.append("     LEFT JOIN NAME_MST NML064  ");
                stb.append("          ON NML064.NAMECD2 = BD2_014.REMARK2 ");
                stb.append("         AND NML064.NAMECD1 = 'L064'  ");
            }
            stb.append(" WHERE  ");
            stb.append("     B1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND B1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            if("1".equals(param._testdiv)) {
                stb.append("     AND BD2_010.REMARK1 = '" + param._testdiv + "' ");
            }else {
                stb.append("     AND BD2_010.REMARK2 = '" + param._testdiv + "' ");
            }
            stb.append(" ORDER BY ");
            if("1".equals(param._testdiv)) {
                stb.append("     BD2_012.REMARK1 ");
            }else {
                stb.append("     BD2_012.REMARK2 ");
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
        log.fatal("$Revision: 65338 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _prgid;
        final String _testdivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");

            if("2".equals(_applicantdiv)) {
                _testdivName = getNameMst(db2, "NAME1", "L024", _testdiv);
            }else {
            	_testdivName = "";
            }
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
