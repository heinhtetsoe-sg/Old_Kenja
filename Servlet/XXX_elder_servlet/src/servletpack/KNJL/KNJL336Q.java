/*
 * $Id: 2564d2e40776235d91512c8cff3c2cb7872ef74c $
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

public class KNJL336Q {

    private static final Log log = LogFactory.getLog(KNJL336Q.class);

    private boolean _hasData;

    private Param _param;

    private final String SIBOUKOU = "2";
    private final String SCHOOL_KIND_P = "P";
    private final String SCHOOL_KIND_J = "J";

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
        final int maxLine = 39;
        final List pageList = getPageList(getList(db2, sql()), maxLine);
        final String form = SIBOUKOU.equals(_param._taisyou) ? "KNJL336Q_2.frm" : "KNJL336Q_1.frm";
        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度 ";
        final String title = SIBOUKOU.equals(_param._taisyou) ? "欠席者志望校一覧表" : "欠席者一覧表";

        int applyCnt = 0;
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);

            applyCnt += dataList.size();
        }

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);

            svf.VrSetForm(form, 4);
            svf.VrsOut("TITLE", nendo + "　" + _param._testdiv0Name1 + "　" + title);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
            svf.VrsOut("PAGE1", String.valueOf(pi + 1));
            svf.VrsOut("PAGE2", String.valueOf(pageList.size()));
            svf.VrsOut("APPLY", String.valueOf(applyCnt));

            if (SCHOOL_KIND_P.equals(_param._schoolKind)) {
                svf.VrsOut("FINSCHOOL_HEADER", "出身園");
            } else {
                svf.VrsOut("FINSCHOOL_HEADER", "出身校");
            }
            for (int i = 0; i < dataList.size(); i++) {
                final Map row = (Map) dataList.get(i);

                svf.VrsOut("EXAM_NO", getString(row, "EXAMNO"));
                svf.VrsOut("NAME", getString(row, "NAME"));
                if (!SIBOUKOU.equals(_param._taisyou)) {
                    svf.VrsOut("DIV", getString(row, "TESTDIV_ABBV1"));
                }
                svf.VrsOut("COURSE_NAME", getString(row, "EXAMCOURSE_NAME"));
                svf.VrsOut("FINSCHOOL_NAME", getString(row, "FINSCHOOL_NAME"));
                if (!SIBOUKOU.equals(_param._taisyou)) {
                    svf.VrsOut("PLACE_NAME", getString(row, "EXAMHALL_NAME"));
                }
                if (SIBOUKOU.equals(_param._taisyou)) {
                    svf.VrsOut("HOPE1", getString(row, "SH_SCHOOL_NAME1"));
                    svf.VrsOut("HOPE2", getString(row, "SH_SCHOOL_NAME2"));
                    svf.VrsOut("HOPE3", getString(row, "SH_SCHOOL_NAME3"));
                    svf.VrsOut("HOPE4", getString(row, "SH_SCHOOL_NAME4"));
                }

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
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     B1.EXAMNO, ");
        stb.append("     B1.NAME, ");
        stb.append("     B1.TESTDIV0, ");
        stb.append("     N1.ABBV1 AS TESTDIV_ABBV1, ");
        stb.append("     B1.DAI1_COURSECD || B1.DAI1_MAJORCD || B1.DAI1_COURSECODE AS DAI1_COURSE, ");
        stb.append("     C1.EXAMCOURSE_NAME, ");
        stb.append("     B1.FS_CD, ");
        stb.append("     F1.FINSCHOOL_NAME, ");
        stb.append("     B1.SIKEN_KAIJOU_CD1, ");
        stb.append("     H1.EXAMHALL_NAME, ");
        stb.append("     B1.SH_SCHOOLCD1, ");
        stb.append("     B1.SH_SCHOOLCD2, ");
        stb.append("     B1.SH_SCHOOLCD3, ");
        stb.append("     B1.SH_SCHOOLCD4, ");
        stb.append("     SH1.FINSCHOOL_NAME AS SH_SCHOOL_NAME1, ");
        stb.append("     SH2.FINSCHOOL_NAME AS SH_SCHOOL_NAME2, ");
        stb.append("     SH3.FINSCHOOL_NAME AS SH_SCHOOL_NAME3, ");
        stb.append("     SH4.FINSCHOOL_NAME AS SH_SCHOOL_NAME4 ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        if (!(SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind))) {
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT REC ON REC.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
            stb.append("             AND REC.APPLICANTDIV = B1.APPLICANTDIV ");
            stb.append("             AND REC.EXAM_TYPE = '1' ");
            stb.append("             AND REC.EXAMNO = B1.EXAMNO ");
        }
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = '" + _param._nameMstTestDiv + "' ");
        if (SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind)) {
            stb.append("         AND N1.NAMECD2 = B1.TESTDIV ");
        } else {
            stb.append("         AND N1.NAMECD2 = REC.TESTDIV ");
        }
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_HALL_YDAT H1 ON H1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND H1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND H1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND H1.EXAMHALLCD = B1.SIKEN_KAIJOU_CD1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH1 ON SH1.FINSCHOOLCD = B1.SH_SCHOOLCD1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH2 ON SH2.FINSCHOOLCD = B1.SH_SCHOOLCD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH3 ON SH3.FINSCHOOLCD = B1.SH_SCHOOLCD3 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH4 ON SH4.FINSCHOOLCD = B1.SH_SCHOOLCD4 ");
        stb.append(" WHERE ");
        stb.append("     B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind)) {
            stb.append("     AND B1.TESTDIV  = '" + _param._testDiv + "' ");
            stb.append("     AND B1.JUDGEMENT = '4' ");//4:欠席
        } else {
            stb.append("     AND REC.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND REC.JUDGEDIV = '4' ");//4:欠席
        }
        stb.append(" ORDER BY ");
        stb.append("     B1.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65625 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _taisyou;
        private final String _loginDate;
        final String _schoolKind;
        final String _nameMstTestDiv;

        final String _applicantdivName;
        final String _testdiv0Name1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");//NAME_MST L004
            _taisyou = request.getParameter("TAISYOU");//1:欠席者一覧表、2:欠席者志望校一覧表
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _nameMstTestDiv = SCHOOL_KIND_P.equals(_schoolKind) ? "LP24" : SCHOOL_KIND_J.equals(_schoolKind) ? "L024" : "L004";

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdiv0Name1 = StringUtils.defaultString(getNameMst(db2, "NAME1", _nameMstTestDiv, _testDiv));
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

