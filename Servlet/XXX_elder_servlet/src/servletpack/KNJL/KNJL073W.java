/*
 *
 * 作成日: 2020/12/23
 * 作成者: matsushima
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL073W {

    private static final Log log = LogFactory.getLog(KNJL073W.class);

    private static final String OUTPUT_YOKO = "1";
    private static final String OUTPUT_TATE = "2";

    private static final String COURSE_NASHI = "0000";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        printForm(db2, svf); //合格者一覧
    }

    private static String getString(final Map map, final String field) {
        if (null == map || map.isEmpty()) {
            return null;
        }
        if (!map.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + map.keySet());
        }
        return (String) map.get(field);
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        log.info(" sql = " + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map map = new HashMap();
                for (int idx = 1; idx <= meta.getColumnCount(); idx++) {
                    map.put(meta.getColumnLabel(idx), StringUtils.defaultString(rs.getString(meta.getColumnLabel(idx))));
                }
                list.add(map);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    // 合格者一覧
    private void printForm(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final List list = getList(db2, getPassStudentInfo());
        if(list.size() == 0) {
            _hasData = false;
            return;
        }

        final int maxLine = (OUTPUT_YOKO.equals(_param._output)) ? 10 : 16;
        final int maxColumn = (OUTPUT_YOKO.equals(_param._output)) ? 8 : 5;
        final String form = (OUTPUT_YOKO.equals(_param._output)) ? "KNJL073W_1.frm" : "KNJL073W_2.frm";
        svf.VrSetForm(form, 1);

        String befValue = "";
        int line = 1;
        int column = 1;
        final List listCourseNashi = new ArrayList(); //コースなし合格者リスト
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row0 = (Map) it.next();

            //コースなし合格者は後で印字
            if (COURSE_NASHI.equals(getString(row0, "SUC_COURSECODE"))) {
                listCourseNashi.add(row0);
                continue;
            }

            //改頁判定
            if(!"".equals(befValue) && !befValue.equals(getString(row0, "SUC_COURSE"))) {
                svf.VrEndPage();
                line = 1;
                column = 1;
            }
            if(line > maxLine) {
                line = 1;
                column++;
            }
            if(column > maxColumn) {
                svf.VrEndPage();
                line = 1;
                column = 1;
            }

            //出力
            svf.VrsOut("TITLE1", _param._nendo + _param._schoolName + "入学者選抜　合格者発表");
            svf.VrsOut("TITLE2", _param._schoolName + "　" + getString(row0, "SUC_KATEI_NAME"));
            svf.VrsOut("MAJOR_NAME", getString(row0, "SUC_MAJOR_NAME") + "　" + getString(row0, "SUC_COURSE_NAME"));

            final String examno = getString(row0, "EXAMNO");
            svf.VrsOutn("EXAM_NO"+ column, line, Integer.valueOf(examno).toString()); //受検番号

            befValue = getString(row0, "SUC_COURSE");
            line++;
            _hasData = true;
        }

        //コースなし合格者印字
        for (final Iterator it = listCourseNashi.iterator(); it.hasNext();) {
            final Map row0 = (Map) it.next();

            //改頁判定
            if(!"".equals(befValue) && !befValue.equals(getString(row0, "SUC_COURSE"))) {
                svf.VrEndPage();
                line = 1;
                column = 1;
            }
            if(line > maxLine) {
                line = 1;
                column++;
            }
            if(column > maxColumn) {
                svf.VrEndPage();
                line = 1;
                column = 1;
            }

            //出力
            svf.VrsOut("TITLE1", _param._nendo + _param._schoolName + "入学者選抜　合格者発表");
            svf.VrsOut("TITLE2", _param._schoolName + "　" + getString(row0, "SUC_KATEI_NAME"));
            svf.VrsOut("MAJOR_NAME", getString(row0, "SUC_MAJOR_NAME"));

            final String examno = getString(row0, "EXAMNO");
            svf.VrsOutn("EXAM_NO"+ column, line, Integer.valueOf(examno).toString()); //受検番号

            befValue = getString(row0, "SUC_COURSE");
            line++;
            _hasData = true;
        }
        svf.VrEndPage();
    }

    // 4:合格通知書
    private String getPassStudentInfo() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(FINSCH.FINSCHOOL_NAME, '') AS FINSCHOOL_NAME, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.SUC_COURSECD, ");
        stb.append("     BASE.SUC_MAJORCD, ");
        stb.append("     BASE.SUC_COURSECODE, ");
        stb.append("     VALUE(COURSE.COURSENAME, '') AS SUC_KATEI_NAME, ");
        stb.append("     VALUE(MAJOR.MAJORNAME, '') AS SUC_MAJOR_NAME, ");
        stb.append("     VALUE(ENT_COURSE.EXAMCOURSE_NAME, '') AS SUC_COURSE_NAME, ");
        stb.append("     BASE.SUC_COURSECD || BASE.SUC_MAJORCD || BASE.SUC_COURSECODE AS SUC_COURSE ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCH ON FINSCH.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("     LEFT JOIN COURSE_MST COURSE ON COURSE.COURSECD = BASE.SUC_COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = BASE.SUC_COURSECD ");
        stb.append("                              AND MAJOR.MAJORCD  = BASE.SUC_MAJORCD  ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST ENT_COURSE ON ENT_COURSE.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                                            AND ENT_COURSE.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("                                            AND ENT_COURSE.TESTDIV      = BASE.TESTDIV ");
        stb.append("                                            AND ENT_COURSE.COURSECD     = BASE.SUC_COURSECD ");
        stb.append("                                            AND ENT_COURSE.MAJORCD      = BASE.SUC_MAJORCD ");
        stb.append("                                            AND ENT_COURSE.EXAMCOURSECD = BASE.SUC_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR  = '" + _param._entexamYear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND ((VALUE(BASE.JUDGEMENT, '') = '1') "); // 合格
        stb.append("          OR ");
        stb.append("          (BASE.TESTDIV = '4' AND VALUE(BASE.JUDGEMENT, '') = '3') "); // スポーツ特別枠選抜受験者で前期選抜合格
        stb.append("         ) ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.SUC_COURSECD, ");
        stb.append("     BASE.SUC_MAJORCD, ");
        stb.append("     BASE.SUC_COURSECODE, ");
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _output;
        final String _loginYear;
        final String _loginDate;
        final String _nendo;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOLKIND;
        final String _documentroot;
        final String _folder;

        final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _output = request.getParameter("OUTPUT");

            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginDate = request.getParameter("LOGIN_DATE").replace("-", "/");
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_entexamYear)) + "年度";
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD = request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");

            _documentroot = request.getParameter("DOCUMENTROOT");
            KNJ_Control imagepath_extension = new KNJ_Control();                //取得クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
            _folder = returnval.val4;                                           //丸データ格納フォルダ

            _schoolName = getSchoolName(db2);
        }

        private String getSchoolName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = getSchoolNameSql();
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("FINSCHOOL_NAME");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSchoolNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.FINSCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     V_SCHOOL_MST T1, ");
            stb.append("     FINSCHOOL_MST T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _loginYear + "' ");
            stb.append("     AND T1.KYOUIKU_IINKAI_SCHOOLCD = T2.FINSCHOOLCD ");
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("     AND T1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            return stb.toString();
        }

    }
}

// eof

