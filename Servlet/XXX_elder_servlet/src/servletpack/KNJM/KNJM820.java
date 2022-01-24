/*
 * $Id: 57e668312dd04c0078949375483297033f10af32 $
 *
 * 作成日: 2012/11/28
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 除籍候補者一覧
 */
public class KNJM820 {

    private static final Log log = LogFactory.getLog(KNJM820.class);

    private boolean _hasData;

    Param _param;

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
        svf.VrSetForm("KNJM820.frm", 1);
        
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final Map m = new HashMap();
                m.put("SCHREGNO", rs.getString("SCHREGNO"));
                m.put("NAME", rs.getString("NAME"));
                m.put("SEX", rs.getString("SEX"));
                m.put("SEX_NAME", rs.getString("SEX_NAME"));
                studentList.add(m);
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        final int maxColumn = 3;
        final int maxLine = 50;
        final int maxPage = studentList.size() / (maxColumn * maxLine) + (studentList.size() % (maxColumn * maxLine) == 0 ? 0 : 1);
        int column = 1;
        int line = 0;
        int page = 1;
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度");
        svf.VrsOut("PAGE", String.valueOf(page));
        svf.VrsOut("TOTAL_PAGE", String.valueOf(maxPage));
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        final Map sexnameMap = new TreeMap();
        final Map sexCountMap = new HashMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            if (line >= maxLine) {
                if (column >= maxColumn) {
                    svf.VrEndPage();
                    page += 1;
                    column = 0;
                    svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度");
                    svf.VrsOut("PAGE", String.valueOf(page));
                    svf.VrsOut("TOTAL_PAGE", String.valueOf(maxPage));
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
                }
                column += 1;
                line = 0;
            }
            line += 1;
            svf.VrsOutn("NO" + column, line, (String) m.get("SCHREGNO"));
            svf.VrsOutn("NAME" + column, line, (String) m.get("NAME"));
            svf.VrsOutn("SEX" + column, line, (String) m.get("SEX_NAME"));
            if (null != m.get("SEX")) {
                sexnameMap.put(m.get("SEX"), m.get("SEX_NAME"));
                increment(sexCountMap, m.get("SEX"));
            }
            _hasData = true;
        }
        if (_hasData) {
            svf.VrsOut("NOTE", getNote(sexnameMap, sexCountMap));
            svf.VrEndPage();
        }
    }
    
    private void increment(final Map countMap, final Object key) {
        if (null == countMap.get(key)) {
            countMap.put(key, new Integer(0));
        }
        countMap.put(key, new Integer(((Integer) countMap.get(key)).intValue() + 1));
    }
    
    private String getNote(final Map nameMap, final Map countMap) {
        final StringBuffer stb = new StringBuffer();
        for (final Iterator it = nameMap.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final String name = (String) nameMap.get(key);
            stb.append(name).append(getCount(countMap, key)).append("名 ");
        }
        stb.append("計").append(getTotalCount(countMap)).append("名");
        return stb.toString();
    }
    
    private int getTotalCount(final Map countMap) {
        int total = 0;
        for (final Iterator it = countMap.keySet().iterator(); it.hasNext();) {
            final String key = (String) it.next();
            total += getCount(countMap, key);
        }
        return total;
    }
    
    private int getCount(final Map countMap, final Object key) {
        if (null == countMap.get(key)) {
            return 0;
        }
        return ((Integer) countMap.get(key)).intValue();
    }

    public String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SUBCLASS_STD_SELECT AS ( ");
        stb.append("     SELECT DISTINCT T1.YEAR, T1.SCHREGNO ");
        stb.append("     FROM SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         ( ");
        if (_param._setNextYear) {
            stb.append("          T1.YEAR = '" + _param._year +"' OR ");
        }
        stb.append("          T1.YEAR = '" + _param._ctrlYear +"' OR ");
        stb.append("          T1.YEAR = '" + _param._lastYear +"' ");
        stb.append("         ) ");
        stb.append("         AND T1.CLASSCD < '90' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T5.NAME, ");
        stb.append("     T5.SEX, ");
        stb.append("     T6.NAME2 AS SEX_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        if (_param._setNextYear) {
            stb.append(" LEFT JOIN SUBCLASS_STD_SELECT T2 ON T2.YEAR = '" + _param._year +"' AND T2.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append(" LEFT JOIN SUBCLASS_STD_SELECT T3 ON T3.YEAR = '" + _param._ctrlYear +"' AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SUBCLASS_STD_SELECT T4 ON T4.YEAR = '" + _param._lastYear +"' AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN NAME_MST T6 ON T6.NAMECD1 = 'Z002' ");
        stb.append("     AND T6.NAMECD2 = T5.SEX ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear  +"' ");
        stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester +"' ");
        stb.append("     AND FISCALYEAR(T5.ENT_DATE) < '" + _param._ctrlYear  +"' "); // ログイン年度入学生は対象外
        stb.append("     AND (T5.GRD_DIV IS NULL OR T5.GRD_DIV = '4') "); // 除籍の生徒は対象外
        if (_param._setNextYear) {
            stb.append("     AND T2.SCHREGNO IS NULL ");
        }
        stb.append("     AND T3.SCHREGNO IS NULL ");
        stb.append("     AND T4.SCHREGNO IS NULL ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year; // ログイン年度 もしくは ログイン年度 + 1
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _lastYear; // ログイン年度 - 1
        final boolean _setNextYear; // 次年度を指定した場合true
        final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _lastYear = String.valueOf(Integer.parseInt(_ctrlYear) - 1);
            _setNextYear = !_year.equals(_ctrlYear);
            _ctrlDate = request.getParameter("CTRL_DATE");
        }
    }
}

// eof

