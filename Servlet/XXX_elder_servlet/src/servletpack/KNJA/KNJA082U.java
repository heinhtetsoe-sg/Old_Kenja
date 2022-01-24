// kanji=漢字
/*
 * $Id: 6de719bd79fc2c2f29976d22461852a9fd89f434 $
 *
 * 作成日: 2017/08/14 10:24:21 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 6de719bd79fc2c2f29976d22461852a9fd89f434 $
 */
public class KNJA082U {

    private static final Log log = LogFactory.getLog("KNJA082U.class");

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
            log.fatal("$Revision: 56595 $");
            KNJServletUtils.debugParam(request, log);

            response.setContentType("application/pdf");
            
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            final Param param = new Param(db2, request);

            _param = param;

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
        String groupField = null;
        if ("1".equals(_param._sort)) {
            groupField = "NEW_GRADE";
        } else if ("2".equals(_param._sort)) {
            groupField = "OLD_GRADE";
        }

        final int maxLine = 50;
        final List pageList = getPageList(groupField, getList(db2, sql(_param)), maxLine);
        final String form = "KNJA082U.frm";

        for (int pi = 0; pi < pageList.size(); pi++) {

            svf.VrSetForm(form, 1);
            svf.VrsOut("TITLE", "旧学年→新学年(学年・クラス・出席番号)"); // タイトル

            final List dataList = (List) pageList.get(pi);
            for (int j = 0; j < dataList.size(); j++) {
                final Map row = (Map) dataList.get(j);

                final int line = j + 1;
                svf.VrsOutn("SCHREG_NO", line, getString(row, "SCHREGNO")); // 学籍番号
                svf.VrsOutn("NAME" + (getMS932ByteLength(getString(row, "NAME")) <= 20 ? "1" : getMS932ByteLength(getString(row, "NAME")) <= 30 ? "2" : "3"), line, getString(row, "NAME")); // 氏名
                svf.VrsOutn("KANA" + (getMS932ByteLength(getString(row, "NAME_KANA")) <= 20 ? "1" : getMS932ByteLength(getString(row, "NAME_KANA")) <= 30 ? "2" : "3"), line, getString(row, "NAME_KANA")); // 氏名かな
                svf.VrsOutn("OLD_GRADE", line, getString(row, "OLD_GRADE_NAME1")); // 旧学年
                svf.VrsOutn("OLD_HR", line, getString(row, "OLD_HR_CLASS_NAME1")); // 旧クラス
                svf.VrsOutn("OLD_NO", line, null == getString(row, "OLD_ATTENDNO") ? "" : String.valueOf(Integer.parseInt(getString(row, "OLD_ATTENDNO")))); // 旧出席番号
                svf.VrsOutn("NEW_GRADE", line, getString(row, "NEW_GRADE_NAME1")); // 新学年
                svf.VrsOutn("NEW_HR", line, getString(row, "NEW_HR_CLASS_NAME1")); // 新クラス
                svf.VrsOutn("NEW_NO", line, null == getString(row, "NEW_ATTENDNO") ? "" : String.valueOf(Integer.parseInt(getString(row, "NEW_ATTENDNO")))); // 新出席番号
            }
            svf.VrEndPage();
            _hasData = true;
        }
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

    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private static List getPageList(final String groupField, final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String oldGroupVal = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            final boolean isDiffGroup = null != groupField && (null == oldGroupVal && null != getString(row, groupField) || null != oldGroupVal && !oldGroupVal.equals(getString(row, groupField)));
            if (null == current || current.size() >= max || isDiffGroup) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(row);
            if (null != groupField) {
                oldGroupVal = getString(row, groupField);
            }
        }
        return rtn;
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

    private String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     OLD1.GRADE AS OLD_GRADE, ");
        stb.append("     OLD1.HR_CLASS AS OLD_HR_CLASS, ");
        stb.append("     OLD1.ATTENDNO AS OLD_ATTENDNO, ");
        stb.append("     OLD2.GRADE_NAME1 AS OLD_GRADE_NAME1, ");
        stb.append("     OLD3.HR_CLASS_NAME1 AS OLD_HR_CLASS_NAME1, ");
        stb.append("     NEW1.GRADE AS NEW_GRADE, ");
        stb.append("     NEW1.HR_CLASS AS NEW_HR_CLASS, ");
        stb.append("     NEW1.ATTENDNO AS NEW_ATTENDNO, ");
        stb.append("     NEW2.GRADE_NAME1 AS NEW_GRADE_NAME1, ");
        stb.append("     NEW3.HR_CLASS_NAME1 AS NEW_HR_CLASS_NAME1 ");
        stb.append(" FROM ");
        //NEW
        stb.append("     SCHREG_REGD_DAT NEW1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = NEW1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT NEW2 ON NEW2.YEAR = NEW1.YEAR ");
        stb.append("         AND NEW2.GRADE = NEW1.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT NEW3 ON NEW3.YEAR = NEW1.YEAR ");
        stb.append("         AND NEW3.SEMESTER = NEW1.SEMESTER ");
        stb.append("         AND NEW3.GRADE = NEW1.GRADE ");
        stb.append("         AND NEW3.HR_CLASS = NEW1.HR_CLASS ");
        //OLD
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             SCHREGNO, ");
        stb.append("             YEAR, ");
        stb.append("             MAX(SEMESTER) AS SEMESTER ");
        stb.append("         FROM ");
        stb.append("             SCHREG_REGD_DAT ");
        stb.append("         WHERE ");
        stb.append("             YEAR = '" + param._oldYear + "' ");
        stb.append("         GROUP BY ");
        stb.append("             SCHREGNO, ");
        stb.append("             YEAR ");
        stb.append("     ) OLD1_MAX ON OLD1_MAX.SCHREGNO = NEW1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT OLD1 ON OLD1.SCHREGNO = OLD1_MAX.SCHREGNO ");
        stb.append("         AND OLD1.YEAR = OLD1_MAX.YEAR ");
        stb.append("         AND OLD1.SEMESTER = OLD1_MAX.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT OLD2 ON OLD2.YEAR = OLD1.YEAR ");
        stb.append("         AND OLD2.GRADE = OLD1.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT OLD3 ON OLD3.YEAR = OLD1.YEAR ");
        stb.append("         AND OLD3.SEMESTER = OLD1.SEMESTER ");
        stb.append("         AND OLD3.GRADE = OLD1.GRADE ");
        stb.append("         AND OLD3.HR_CLASS = OLD1.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     NEW1.YEAR = '" + param._year + "' ");
        stb.append("     AND NEW1.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND NEW1.GRADE || NEW1.HR_CLASS IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
        stb.append(" ORDER BY ");
        if ("1".equals(param._sort)) {
            stb.append("     NEW1.GRADE, ");
            stb.append("     NEW1.HR_CLASS, ");
            stb.append("     NEW1.ATTENDNO ");
        } else if ("2".equals(param._sort)) {
            stb.append("     OLD1.GRADE, ");
            stb.append("     OLD1.HR_CLASS, ");
            stb.append("     OLD1.ATTENDNO ");
        }
        return stb.toString();
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _oldYear;
        final String _semester;
        final String _sort; // 1:新クラス 2:旧クラス
        final String[] _category_selected;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _oldYear = String.valueOf(Integer.parseInt(_year) - 1);
            _semester = request.getParameter("CTRL_SEMESTER");
            _sort = request.getParameter("SORT");
            _category_selected = request.getParameterValues("CATEGORY_SELECTED");
            for (int i = 0; i < _category_selected.length; i++) {
                _category_selected[i] = StringUtils.split(_category_selected[i], "-")[0];
            }
        }
    }
}

// eof
