/*
 * $Id: a4770ea209b9ef5d63a37ba7f3a0252ac46f4bd0 $
 *
 * 作成日: 2015/10/13
 * 作成者: maesiro
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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL410Y {

    private static final Log log = LogFactory.getLog(KNJL410Y.class);

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

    private List getList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sexName = rs.getString("SEX_NAME");
                final String birthday = rs.getString("BIRTHDAY");
                final String fsName = rs.getString("FS_NAME");
                final String telno = rs.getString("TELNO");
                final String zipcd = rs.getString("ZIPCD");
                final String address = rs.getString("ADDRESS");
                final String gname = rs.getString("GNAME");
                final String gkana = rs.getString("GKANA");
                final String relationshipName = rs.getString("RELATIONSHIP_NAME");
                final String testdivName = rs.getString("TESTDIV_NAME");

                final PrintStdData stdData = new PrintStdData(examNo, name, nameKana, sexName, birthday, fsName, telno, zipcd, address, gname, gkana, relationshipName, testdivName);
                list.add(stdData);
            }
            
            for (final Iterator it = list.iterator(); it.hasNext();) {
            	final PrintStdData stdData = (PrintStdData) it.next();
                stdData.setBikou(db2);
            }

        } catch (SQLException ex) {
            log.fatal("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL410Y.frm", 4);
        final List list = getList(db2);
        for (Iterator itStd = list.iterator(); itStd.hasNext();) {
            PrintStdData stdData = (PrintStdData) itStd.next();
            svf.VrsOut("TITLE", _param._year + "年度　" + _param._applicantDivName + "入試志願者データチェックリスト");
            svf.VrsOut("SUBTITLE", "(" + _param._testDivName + ")");
            svf.VrsOut("DATE", _param._loginDate.replace('-', '/'));

            svf.VrsOut("EXAM_NO", stdData._examNo);
            int nameCnt = getMS932count(stdData._name);
            final String nameSoeji = 16 < nameCnt ? "2_1" : "1";
            svf.VrsOut("NAME" + nameSoeji, stdData._name);
            int kanaCnt = getMS932count(stdData._nameKana);
            final String kanaSoeji = 16 < kanaCnt ? "2_1" : "1";
            svf.VrsOut("KANA" + kanaSoeji, stdData._nameKana);
            svf.VrsOut("SEX", stdData._sexName);
            svf.VrsOut("BIRTHDAY", stdData._birthday.replace('-', '/'));
            int fsCnt = getMS932count(stdData._fsName);
            final String fsSoeji = 36 < fsCnt ? "4_1" : 28 < fsCnt ? "3_1" : 14 < fsCnt ? "2_1" : "1";
            svf.VrsOut("FINSCHOOL_NAME" + fsSoeji, stdData._fsName);
            svf.VrsOut("TELNO", stdData._telno);
            svf.VrsOut("ZIPNO", stdData._zipcd);
            int addrCnt = getMS932count(stdData._address);
            final String addrSoeji = 80 < addrCnt ? "4_1" : 60 < addrCnt ? "3_1" : 30 < addrCnt ? "2_1" : "1";
            svf.VrsOut("ADDR" + addrSoeji, stdData._address);
            int gNameCnt = getMS932count(stdData._gname);
            final String gNameSoeji = 16 < gNameCnt ? "2_1" : "1";
            svf.VrsOut("GRD_NAME" + gNameSoeji, stdData._gname);
            int gKanaCnt = getMS932count(stdData._gkana);
            final String gKanaSoeji = 16 < gKanaCnt ? "2_1" : "1";
            svf.VrsOut("GRD_KANA" + gKanaSoeji, stdData._gkana);
            svf.VrsOut("RELATION", stdData._relationshipName);
            for (Iterator itBikou = stdData._bikouList.iterator(); itBikou.hasNext();) {
                final String bikou = (String) itBikou.next();
                for (int i = 1; i < 13; i++) {
                    svf.VrsOut("GRP" + i, stdData._examNo);
                }
                svf.VrsOut("REMARK", bikou);
                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private int getMS932count(String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        stb.append("     T1.BIRTHDAY, ");
        stb.append("     T1.FS_NAME, ");
        stb.append("     T3.TELNO, ");
        stb.append("     T3.ZIPCD, ");
        stb.append("     VALUE(T3.ADDRESS1,'') || VALUE(T3.ADDRESS2,'') AS ADDRESS, ");
        stb.append("     T3.GNAME, ");
        stb.append("     T3.GKANA, ");
        stb.append("     N2.NAME1 AS RELATIONSHIP_NAME, ");
        stb.append("     N3.NAME1 AS TESTDIV_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T1.SEX ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'H201' AND N2.NAMECD2 = T3.RELATIONSHIP ");
        stb.append("     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L004' AND N3.NAMECD2 = T1.TESTDIV ");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR  = '" + _param._year + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if ("1".equals(_param._testDiv)) {
            stb.append("     AND T1.TESTDIV1      = '" + _param._testDiv + "' ");
        } else if ("2".equals(_param._testDiv)) {
            stb.append("     AND T1.TESTDIV2      = '" + _param._testDiv + "' ");
        } else if ("3".equals(_param._testDiv)) {
            stb.append("     AND T1.TESTDIV3      = '" + _param._testDiv + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.ENTEXAMYEAR, ");
        stb.append("     T1.APPLICANTDIV, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.EXAMNO ");

        return stb.toString();
    }

    private class PrintStdData {
        final String _examNo;
        final String _name;
        final String _nameKana;
        final String _sexName;
        final String _birthday;
        final String _fsName;
        final String _telno;
        final String _zipcd;
        final String _address;
        final String _gname;
        final String _gkana;
        final String _relationshipName;
        final String _testdivName;
        final List _bikouList;

        public PrintStdData(
                final String examNo,
                final String name,
                final String nameKana,
                final String sexName,
                final String birthday,
                final String fsName,
                final String telno,
                final String zipcd,
                final String address,
                final String gname,
                final String gkana,
                final String relationshipName,
                final String testdivName
        ) {
            _examNo             = examNo;
            _name               = name;
            _nameKana           = nameKana;
            _sexName            = sexName;
            _birthday           = birthday;
            _fsName             = fsName;
            _telno              = telno;
            _zipcd              = zipcd;
            _address            = address;
            _gname              = gname;
            _gkana              = gkana;
            _relationshipName   = relationshipName;
            _testdivName        = testdivName;
            _bikouList = new ArrayList();
        }

        public void setBikou(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = bikouSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String remark1 = StringUtils.defaultString(rs.getString("FAMILY_NAME"));
                    final String remark2 = StringUtils.defaultString(rs.getString("AGE"));
                    final String remark3 = StringUtils.defaultString(rs.getString("FAMILY_RELATIONSHIP_NAME"));
                    final String remark4 = StringUtils.defaultString(rs.getString("FAMILY_REMARK"));
                    String setBikou = "".equals(remark1) ? "" : remark1;
                    setBikou += "".equals(setBikou) ? remark2 : "/" + remark2;
                    setBikou += "".equals(setBikou) ? remark3 : "/" + remark3;
                    setBikou += "".equals(setBikou) ? remark4 : "/" + remark4;

                    _bikouList.add(setBikou);
                }
                if (_bikouList.size() == 0) {
                    _bikouList.add("");
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String bikouSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.NAME AS FAMILY_NAME, ");
            stb.append("     T1.AGE, ");
            stb.append("     N2.NAME1 AS FAMILY_RELATIONSHIP_NAME, ");
            stb.append("     T1.REMARK AS FAMILY_REMARK ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_FAMILY_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'H201' AND N2.NAMECD2 = T1.RELATIONSHIP ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append(" AND T1.EXAMNO = '" + _examNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.SEQ ");

            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76294 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _applicantDiv;
        private final String _applicantDivName;
        private final String _testDiv;
        private final String _testDivName;
        private final String _loginDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _applicantDivName = getNameMst(db2, "L003", _applicantDiv);
            _testDiv = request.getParameter("TESTDIV");
            _testDivName = getNameMst(db2, "L004", _testDiv);
            _loginDate = request.getParameter("LOGIN_DATE");
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) throws SQLException {
            String retStr = "";
            final String l004sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(l004sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("NAME1");
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retStr;
        }

    }
}

// eof

