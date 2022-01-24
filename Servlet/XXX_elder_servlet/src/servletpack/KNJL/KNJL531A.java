/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: b638d6553f273f4eb85e2caf89c98dcaffecac0a $
 *
 * 作成日: 2019/29/30
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL531A {

    private static final Log log = LogFactory.getLog(KNJL531A.class);

    private boolean _hasData;
    private final String ALL = "all";

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
        svf.VrSetForm("KNJL531A.frm", 1);

        Map schregMap = new HashMap();
        final List printData = getPrintData(db2, schregMap);
        int idx = 1;
        int maxline = 50;

        for (final Iterator it = printData.iterator(); it.hasNext();) {
            final PrintData data = (PrintData) it.next();
            if (idx > maxline) {
                svf.VrEndPage();
                idx = 1;
            }
            if (idx == 1) {
                //ヘッダ
                setTitle(db2, svf);
            }

            svf.VrsOutn("EXAM_DIV", idx, data._testdiv_name); //入試区分
            svf.VrsOutn("HOPE_DIV", idx, data._desirediv_name); //志望区分
            svf.VrsOutn("EXAM_NO", idx, data._examno); //受験番号
            svf.VrsOutn("NAME", idx, data._name); //氏名
            svf.VrsOutn("FINSCHOOL_NAME", idx, data._finschool_name); //出身中学

            idx++;
           _hasData = true;
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        final String sortName = ("1".equals(_param._sort)) ? "　＜受験番号順＞" : "　＜名前順＞";
        svf.VrsOut("TITLE", _param.getYear(db2) + "　合格確約者一覧");
        svf.VrsOut("SUBTITLE", "受験区分：" + _param._testDivName + "　志望区分：" + _param._desireDivName + sortName);
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

    private List getPrintData(final DB2UDB db2, final Map schregMap) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sql();
        log.fatal(sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String examno = StringUtils.defaultString(rs.getString("EXAMNO"));
            	final String name = StringUtils.defaultString(rs.getString("NAME"));
            	final String fs_cd = StringUtils.defaultString(rs.getString("FS_CD"));
            	final String finschool_name = StringUtils.defaultString(rs.getString("FINSCHOOL_NAME"));
            	final String gokaku = StringUtils.defaultString(rs.getString("GOKAKU"));
            	final String testdiv = StringUtils.defaultString(rs.getString("TESTDIV"));
            	final String testdiv_name = StringUtils.defaultString(rs.getString("TESTDIV_NAME"));
            	final String desirediv = StringUtils.defaultString(rs.getString("DESIREDIV"));
            	final String desirediv_name = StringUtils.defaultString(rs.getString("DESIREDIV_NAME"));
                final PrintData printData = new PrintData(examno, name, fs_cd, finschool_name, gokaku, testdiv, testdiv_name, desirediv, desirediv_name);
                rtnList.add(printData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH HOPE_COURSE AS( ");
        stb.append(" SELECT DISTINCT");
        stb.append("   T1.HOPE_COURSECODE, ");
        stb.append("   T1.HOPE_NAME ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_HOPE_COURSE_MST T1 ");
        stb.append("   INNER JOIN ENTEXAM_HOPE_COURSE_YDAT T2 ");
        stb.append("           ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ");
        stb.append("          AND T2.ENTEXAMYEAR     = '"+ _param._entexamyear +"' ");
        stb.append("   INNER JOIN ( SELECT ");
        stb.append("                  HOPE_COURSECODE, ");
        stb.append("                  MIN(COURSECODE) AS COURSECODE ");
        stb.append("                FROM ENTEXAM_HOPE_COURSE_MST ");
        stb.append("                GROUP BY HOPE_COURSECODE ");
        stb.append("              ) T3 ON T3.HOPE_COURSECODE = T1.HOPE_COURSECODE ");
        stb.append("                  AND T3.COURSECODE      = T1.COURSECODE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     B1.EXAMNO, ");
        stb.append("     B1.NAME, ");
        stb.append("     B1.FS_CD, ");
        stb.append("     F1.FINSCHOOL_NAME AS FINSCHOOL_NAME, ");
        stb.append("     D1.REMARK8 AS GOKAKU, ");
        stb.append("     B1.TESTDIV, ");
        stb.append("     L004.NAME1 AS TESTDIV_NAME, ");
        stb.append("     B1.DESIREDIV, ");
        stb.append("     HOPE.HOPE_NAME AS DESIREDIV_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D1 ");
        stb.append("             ON D1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ");
        stb.append("            AND D1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("            AND D1.EXAMNO       = B1.EXAMNO ");
        stb.append("            AND D1.SEQ          = '004' ");
        stb.append("            AND D1.REMARK8      = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D2 ");
        stb.append("             ON D2.ENTEXAMYEAR  = B1.ENTEXAMYEAR ");
        stb.append("            AND D2.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("            AND D2.EXAMNO       = B1.EXAMNO ");
        stb.append("            AND D2.SEQ          = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_SCHOOL_MST S1 ");
        stb.append("             ON S1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ");
        stb.append("            AND S1.ENTEXAM_SCHOOLCD  = B1.FS_CD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ");
        stb.append("            ON F1.FINSCHOOLCD  = S1.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L004 ");
        stb.append("            ON L004.NAMECD2 = B1.TESTDIV ");
        stb.append("           AND L004.NAMECD1 = 'L004' ");
        stb.append("     LEFT JOIN HOPE_COURSE HOPE ");
        stb.append("            ON HOPE.HOPE_COURSECODE = D2.REMARK10 ");
        stb.append(" WHERE ");
        stb.append("         B1.ENTEXAMYEAR  = '"+ _param._entexamyear +"' ");
        stb.append("     AND B1.APPLICANTDIV = '"+ _param._applicantDiv +"' ");
        if(!"".equals(_param._testDiv)){
            stb.append("     AND B1.TESTDIV      = '"+ _param._testDiv +"' ");
        }
        if(!"".equals(_param._desireDiv)){
            stb.append("     AND D2.REMARK10     = '"+ _param._desireDiv +"' ");
        }
        stb.append(" ORDER BY ");
        if("2".equals(_param._sort)){
            stb.append("     B1.NAME, ");
        }
        stb.append("     B1.EXAMNO ");
        return stb.toString();
    }

    private class PrintData {
    	final String _examno;
    	final String _name;
    	final String _fs_cd;
    	final String _finschool_name;
    	final String _gokaku;
    	final String _testdiv;
    	final String _testdiv_name;
    	final String _desirediv;
    	final String _desirediv_name;
        PrintData(
        		final String examno,
        		final String name,
        		final String fs_cd,
        		final String finschool_name,
        		final String gokaku,
        		final String testdiv,
        		final String testdiv_name,
        		final String desirediv,
        		final String desirediv_name
        ) {
        	_examno = examno;
        	_name = name;
        	_fs_cd = fs_cd;
        	_finschool_name = finschool_name;
        	_gokaku = gokaku;
        	_testdiv = testdiv;
        	_testdiv_name = testdiv_name;
        	_desirediv = desirediv;
        	_desirediv_name = desirediv_name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70611 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _desireDiv;
        private final String _sort;
        private final String _loginDate;
        final String _applicantdivName;
        final String _testDivName;
        final String _desireDivName;
        private final boolean _seirekiFlg;
        final Map _testdivMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _desireDiv = request.getParameter("DESIREDIV");
            _sort = request.getParameter("SORT");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testDivName = getNameMst(db2, "NAME1", "L004", _testDiv);
            if("".equals(_desireDiv)) {
                _desireDivName = "全て";
            } else {
                _desireDivName = getHopeCourseName(db2, _entexamyear, _desireDiv);
            }
            _seirekiFlg = getSeirekiFlg(db2);
            _testdivMap = getNameMstMap(db2, "NAME1", "L004");
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

        private static String getHopeCourseName(final DB2UDB db2, final String entexamyear, final String desireDiv) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT");
            stb.append("   T1.HOPE_NAME ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_HOPE_COURSE_MST T1 ");
            stb.append("   INNER JOIN ENTEXAM_HOPE_COURSE_YDAT T2 ");
            stb.append("           ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ");
            stb.append("          AND T2.ENTEXAMYEAR     = '"+entexamyear+"' ");
            stb.append(" WHERE ");
            stb.append("     T1.HOPE_COURSECODE = '"+desireDiv+"' ");

            try {

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("HOPE_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private static Map getNameMstMap(final DB2UDB db2, final String field, final String namecd1) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NAMECD2, " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("NAMECD2"), rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retMap;
        }

        /**
         * 日付表示の和暦(年号)/西暦使用フラグ
         * @param db2
         * @return
         */
        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }

        /**
         * 年度の名称を得る
         * @return 年度の名称
         */
        public String getYear(final DB2UDB db2) {
            if (_seirekiFlg) {
                return _entexamyear + "年度";
            } else {
                final String[] gengou = KNJ_EditDate.tate_format4(db2, _loginDate);
                return gengou[0] + gengou[1] + "年度";
            }
        }

    }
}

// eof
