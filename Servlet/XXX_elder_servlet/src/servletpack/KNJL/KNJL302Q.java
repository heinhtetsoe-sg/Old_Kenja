/*
 * $Id: 1ed02a777cc1780de631070a93a8261bea373036 $
 *
 * 作成日: 2017/04/05
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL302Q {

    private static final Log log = LogFactory.getLog(KNJL302Q.class);

    private boolean _hasData;
    private final String SCHOOL_KIND_J = "J";

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
        final int maxLine = 34;
        final List pageList = getPageList(getList(db2, sql()), maxLine);
        final String form = "KNJL302Q.frm";
        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 ";

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);

            svf.VrSetForm(form, 4);
            svf.VrsOut("TITLE", nendo + "　" + _param._testdiv0Name1 + "　未記入ありの志願者");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
            svf.VrsOut("PAGE1", String.valueOf(pi + 1));
            svf.VrsOut("PAGE2", String.valueOf(pageList.size()));

            for (int i = 0; i < dataList.size(); i++) {
                final Map row = (Map) dataList.get(i);

                svf.VrsOut("EXAM_NO", getString(row, "EXAMNO"));
                svf.VrsOut("NAME", getString(row, "NAME"));
                svf.VrsOut("EXAM_DIV", getString(row, "TESTDIV0_ABBV1"));
                svf.VrsOut("COURSE_NAME", getString(row, "EXAMCOURSE_NAME"));
                svf.VrsOut("GUARDIAN", getString(row, "GNAME"));
                svf.VrsOut("DORMITORY", getString(row, "DORMITORY_FLG"));
                svf.VrsOut("HOPE", getString(row, "SH_SCHOOLCD1"));

                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
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

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String sql() {
        //TODO1　B1.TESTDIV0を参照しない理由は？何か不都合があるか？特にないと思うが・・・

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     B1.EXAMNO, ");
        stb.append("     B1.NAME, ");
        if (SCHOOL_KIND_J.equals(_param._schoolKind)) {
            stb.append("     B1.TESTDIV AS TESTDIV0, ");
        } else {
            stb.append("     B1.TESTDIV0, ");
        }
        stb.append("     N1.ABBV1 AS TESTDIV0_ABBV1, ");
        stb.append("     B1.DAI1_COURSECD || B1.DAI1_MAJORCD || B1.DAI1_COURSECODE AS DAI1_COURSE, ");
        stb.append("     C1.EXAMCOURSE_NAME, ");
        stb.append("     CASE WHEN A1.GNAME IS NULL THEN 'レ' END AS GNAME, ");
        stb.append("     CASE WHEN B1.DORMITORY_FLG IS NULL THEN 'レ' END AS DORMITORY_FLG, ");
        stb.append("     CASE WHEN B1.SH_SCHOOLCD1 IS NULL THEN 'レ' END AS SH_SCHOOLCD1 ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT A1 ON A1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND A1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND A1.EXAMNO = B1.EXAMNO ");
        if (SCHOOL_KIND_J.equals(_param._schoolKind)) {
            stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = '" + _param._nameMstTestDiv + "' AND N1.NAMECD2 = B1.TESTDIV ");
        } else {
            stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = '" + _param._nameMstTestDiv + "' AND N1.NAMECD2 = B1.TESTDIV0 ");
        }
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (SCHOOL_KIND_J.equals(_param._schoolKind)) {
            stb.append("     AND B1.TESTDIV = '" + _param._testDiv0 + "' ");
        } else {
            stb.append("     AND B1.TESTDIV0 = '" + _param._testDiv0 + "' ");
        }
        stb.append("     AND (A1.GNAME IS NULL OR B1.DORMITORY_FLG IS NULL OR B1.SH_SCHOOLCD1 IS NULL) ");
        stb.append(" ORDER BY ");
        stb.append("     B1.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56657 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv0;
        private final String _loginDate;
        final String _applicantdivName;
        final String _testdiv0Name1;
        final String _schoolKind;
        final String _nameMstTestDiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _schoolKind = request.getParameter("SCHOOLKIND");
            _nameMstTestDiv = SCHOOL_KIND_J.equals(_schoolKind) ? "L024" : "L045";
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv0 = request.getParameter("TESTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdiv0Name1 = StringUtils.defaultString(getNameMst(db2, "NAME1", _nameMstTestDiv, _testDiv0));
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
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

