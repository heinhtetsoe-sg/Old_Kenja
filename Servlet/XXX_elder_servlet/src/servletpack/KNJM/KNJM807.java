/*
 * $Id: 8581f7e6dac101a29d450579a84ad8c9faebe500 $
 *
 * 作成日: 2012/10/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

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
 * 教科書購入票
 */
public class KNJM807 {

    private static final Log log = LogFactory.getLog(KNJM807.class);

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


        for (int i = 0; i < _param._schregnos.length; i++) {
        	final String frmName1 = _param._isMieken ? "KNJM807_1MIE.frm" : _param._isSagaken ? "KNJM807_1_2.frm" : "KNJM807_1.frm";
            svf.VrSetForm(frmName1, 1);
            printHeader(db2, svf, _param._schregnos[i]);
            printChairText(db2, svf, _param._schregnos[i]);
            svf.VrEndPage();

            svf.VrSetForm(_param._isMieken ? "KNJM807_2MIE.frm" : "KNJM807_2.frm", 1);
            printHeader(db2, svf, _param._schregnos[i]);
            printChairText(db2, svf, _param._schregnos[i]);
            svf.VrEndPage();

            _hasData = true;
        }
    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final String schregno) {

    	if(_param._isMieken && _param._isNameSpare3) svf.VrsOut("CURRICURAM_NAME", "通信制");

        svf.VrsOut("CHECK_NO", schregno.length() >= 2 ? schregno.substring(2) : "");
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("NO", schregno);
        svf.VrsOut("SCHOOLNAME", _param._schoolname);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql(schregno);
            log.debug("address sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                svf.VrsOut("ZIP_NO", rs.getString("ZIPCD"));
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(addr1) > 50 || getMS932ByteLength(addr2) > 50)) {
                    svf.VrsOut("ADDRESS1_3", addr1);
                    svf.VrsOut("ADDRESS2_3", addr2);
                } else if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(addr1) > 44 || getMS932ByteLength(addr2) > 44)) {
                    svf.VrsOut("ADDRESS1_2", addr1);
                    svf.VrsOut("ADDRESS2_2", addr2);
                } else {
                    svf.VrsOut("ADDRESS1", addr1);
                    svf.VrsOut("ADDRESS2", addr2);
                }
                svf.VrsOut("NAME", StringUtils.defaultString(rs.getString("NAME2")) + "　様");
            }
        } catch (final Exception ex) {
            log.error("set_detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        if ("1".equals(_param._output)) {
            stb.append("SELECT ");
            stb.append("  t0.SCHREGNO, ");
            stb.append("  t0.ZIPCD,");
            stb.append("  t0.ADDR1,");
            stb.append("  t0.ADDR2,");
            stb.append("  t0.NAME, ");
            stb.append("  t0.NAME AS NAME2 ");
            stb.append(" FROM ");
            stb.append("   FRESHMAN_DAT t0 ");
            stb.append(" WHERE ");
            stb.append("  t0.ENTERYEAR = '" + (Integer.parseInt(_param._year) + 1) + "' ");
            stb.append("  AND t0.SCHREGNO = '" + schregno + "' ");
        } else {
            stb.append(" WITH SCHREG_ADDRESS AS ( ");
            stb.append("   SELECT  ");
            stb.append("      T3.NAME AS SCHREG_NAME, ");
            stb.append("      T1.*  ");
            stb.append("   FROM  ");
            stb.append("      SCHREG_ADDRESS_DAT T1  ");
            stb.append("      INNER JOIN ( ");
            stb.append("        SElECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO ");
            stb.append("      ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
            stb.append("      INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ) ");

            stb.append("SELECT ");
            stb.append("  t0.SCHREGNO, ");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ZIPCD ELSE t2.ZIPCD END AS ZIPCD,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ADDR1 ELSE t2.ADDR1 END AS ADDR1,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_ADDR2 ELSE t2.ADDR2 END AS ADDR2,");
            stb.append("  CASE WHEN SEND_ADDR1 IS NOT NULL THEN SEND_NAME  ELSE t2.SCHREG_NAME END AS NAME,");
            stb.append("  t0.NAME AS NAME2 ");
            stb.append(" FROM ");
            stb.append("   SCHREG_BASE_MST t0 ");
            stb.append(" LEFT JOIN SCHREG_SEND_ADDRESS_DAT t1 ON t1.SCHREGNO = t0.SCHREGNO ");
            stb.append("   AND t1.DIV = '1' ");
            stb.append(" LEFT JOIN SCHREG_ADDRESS t2 ON t2.SCHREGNO = t0.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("  t0.SCHREGNO = '" + schregno + "' ");
        }
        return stb.toString();
    }

    private void printChairText(final DB2UDB db2, final Vrw32alp svf, final String schregno) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getChairCountSql(schregno);
            log.debug(" chair sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("SUBJECT", rs.getString("COUNT"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final List subclassList = new ArrayList();
        try {
            final String sql = sql(schregno);

            log.debug(" textbook sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                TextbookSubclass textBookSubclass = getTextBookSubclass(subclassList, rs.getString("CLASSCD"), rs.getString("SCHOOL_KIND"), rs.getString("CURRICULUM_CD"), rs.getString("SUBCLASSCD"));
                if (null == textBookSubclass) {
                    textBookSubclass = new TextbookSubclass(rs.getString("CLASSCD"), rs.getString("SCHOOL_KIND"), rs.getString("CURRICULUM_CD"), rs.getString("SUBCLASSCD"));
                    subclassList.add(textBookSubclass);
                }
                final Textbook textbook = new Textbook(rs.getString("TEXTBOOKCD"), rs.getString("TEXTBOOKDIV"), rs.getString("NAMESPARE1"), rs.getString("TEXTBOOKMS"), rs.getString("TEXTBOOKNAME"), rs.getString("TEXTBOOKUNITPRICE"));
                textBookSubclass._textBookList.add(textbook);
            }

        } catch (final SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        final String[] textbookdiv = new String[] {"1", "2", "3"};
        final long[] pricedivtotal = new long[3];
        int line = 1;

        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final TextbookSubclass textBookSubclass = (TextbookSubclass) it.next();
            int subline = 0;
            for (int i = 0; i < textbookdiv.length; i++) {
                final List textList = textBookSubclass.getTextList(textbookdiv[i]);
                subline = Math.max(subline, textList.size());
                for (int j = 0; j < Math.min(16 - line - j, textList.size()); j++) {
                    final Textbook textbook = (Textbook) textList.get(j);
                    if ("1".equals(textbookdiv[i])) {
                        svf.VrsOutn("TEXTNO", line + j, textbook._textbookms);
                    }
                    svf.VrsOutn("TEXT" + textbookdiv[i] + "_" + (getMS932ByteLength(textbook._textbookname) > 24 ? "2" : "1"), line + j, textbook._textbookname);
                    svf.VrsOutn("TEXT_PRICE" + ("1".equals(textbookdiv[i]) ? "" : textbookdiv[i]), line + j, textbook._textbookunitprice);
                    if (null != textbook._textbookunitprice) {
                        pricedivtotal[i] += Long.parseLong(textbook._textbookunitprice);
                    }
                }
            }
            line += subline;
        }

        long pricetotal = 0;
        long totalPrice1 = 0; //教科書・学習書合計
        long totalPrice2 = 0; //補助教材合計
        for (int i = 0; i < textbookdiv.length; i++) {
            svf.VrsOutn("TEXT_PRICE" + ("1".equals(textbookdiv[i]) ? "" : textbookdiv[i]), 16, String.valueOf(pricedivtotal[i]));
            pricetotal += pricedivtotal[i];
            if("1".equals(textbookdiv[i]) || "2".equals(textbookdiv[i])) {
                totalPrice1 += pricedivtotal[i];
            }else {
                totalPrice2 += pricedivtotal[i];
            }
        }
        if(_param._isSagaken) {
            svf.VrsOut("TOTAL_PRICE1", String.valueOf(totalPrice1)); //教科書・学習書合計
            svf.VrsOut("TOTAL_PRICE2", String.valueOf(totalPrice2)); //補助教材合計
        }
        svf.VrsOut("TOTAL_PRICE3", String.valueOf(pricetotal));
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    private TextbookSubclass getTextBookSubclass(final List list, String classcd, String schoolkind, String curriculumcd, String subclasscd) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final TextbookSubclass tbs = (TextbookSubclass) it.next();
            if (tbs._classcd.equals(classcd) && tbs._schoolkind.equals(schoolkind) && tbs._curriculumcd.equals(curriculumcd) && tbs._subclasscd.equals(subclasscd)) {
                return tbs;
            }
        }
        return null;
    }

    private static class TextbookSubclass {
        final String _classcd;
        final String _schoolkind;
        final String _curriculumcd;
        final String _subclasscd;
        final List _textBookList = new ArrayList();
        public TextbookSubclass(String classcd, String schoolkind, String curriculumcd, String subclasscd) {
            _classcd = classcd;
            _schoolkind = schoolkind;
            _curriculumcd = curriculumcd;
            _subclasscd = subclasscd;
        }
        public List getTextList(String textbookdiv) {
            final List list = new ArrayList();
            for (final Iterator it = _textBookList.iterator(); it.hasNext();) {
                final Textbook tb = (Textbook) it.next();
                if (textbookdiv.equals(tb._namespare1)) {
                    list.add(tb);
                }
            }
            return list;
        }
    }

    private static class Textbook {
        final String _textbookcd;
        final String _textbookdiv;
        final String _namespare1;
        final String _textbookms;
        final String _textbookname;
        final String _textbookunitprice;
        public Textbook(String textbookcd, String textbookdiv, String namespare1, String textbookms, String textbookname, String textbookunitprice) {
            _textbookcd = textbookcd;
            _textbookdiv = textbookdiv;
            _namespare1 = namespare1;
            _textbookms = textbookms;
            _textbookname = textbookname;
            _textbookunitprice = textbookunitprice;
        }
    }

    public String sql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.TEXTBOOKCD, ");
        stb.append("     T3.TEXTBOOKMS, ");
        stb.append("     T3.TEXTBOOKDIV, ");
        stb.append("     NMM004.NAMESPARE1, ");
        stb.append("     T3.TEXTBOOKNAME, ");
        stb.append("     T3.TEXTBOOKUNITPRICE ");
        stb.append(" FROM SCHREG_TEXTBOOK_SUBCLASS_DAT T1 ");
        stb.append(" INNER JOIN TEXTBOOK_MST T3 ON T3.TEXTBOOKCD = T1.TEXTBOOKCD ");
        stb.append(" INNER JOIN NAME_MST NMM004 ON NMM004.NAMECD1 = 'M004' ");
        stb.append("     AND NMM004.NAMECD2 = T3.TEXTBOOKDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.TEXTBOOKCD ");
        return stb.toString();
    }

    public String getChairCountSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COUNT(DISTINCT T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD) AS COUNT ");
        stb.append(" FROM SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append(" INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
        stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71857 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _semester;
        private final String _output;
        private final String[] _schregnos;
        private final String _useAddrField2;
        final String _schoolname;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final boolean _isMieken;
        final boolean _isSagaken;
        final boolean _isNameSpare3;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _output = request.getParameter("OUTPUT");
            if (null != request.getParameter("SCHREGNO")) {
                _schregnos = new String[] {request.getParameter("SCHREGNO")};
            } else {
                final String[] src = request.getParameterValues("category_name");
                final String[] dst = new String[src.length];
                for (int i = 0; i < src.length; i++) {
                    dst[i] = StringUtils.split(src[i], "-")[0];
                }
                _schregnos = dst;
            }
            _useAddrField2 = request.getParameter("useAddrField2");
            _schoolname = getSchoolname(db2);
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            final String z010name1 = setZ010Name1(db2);
            _isMieken = "mieken".equals(z010name1);
            _isSagaken = "sagaken".equals(z010name1);
            _isNameSpare3 = "1".equals(getNameMst(db2,"Z010","00","NAMESPARE3"));
        }


        // 卒業認定単位数の取得
        private String getSchoolname(
                final DB2UDB db2
        ) {
            String schoolname1 = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "'";
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                    sql += "   AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolname1 = rs.getString("SCHOOLNAME1");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolname1;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        /**
         * 名称マスタ
         */
        private String getNameMst(DB2UDB db2, final String namecd1, final String namecd2, final String field) {
            String rtnStr = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM NAME_MST WHERE NAMECD1 = '"+namecd1+"' AND NAMECD2 = '"+namecd2+"' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtnStr = rs.getString(field);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnStr;
        }

    }
}

// eof

