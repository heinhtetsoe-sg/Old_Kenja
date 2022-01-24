/*
 * $Id: 85fc4bd88f83c02c6667404e675181f8f4f009c1 $
 *
 * 作成日: 2010/11/19
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/*
 *  学校教育システム 賢者 [事務管理] 振込依頼書（武蔵）
 */
public class KNJG090 {
    
    private static final Log log = LogFactory.getLog(KNJG090.class);
    
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
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                svf.VrSetForm("KNJG090.frm", 1);
                
                final String gradeCd = zeroSuppress(rs.getString("GRADE_CD"));
                final String hrclassName1;
                if (null != rs.getString("HR_CLASS_NAME1")) {
                    hrclassName1 = rs.getString("HR_CLASS_NAME1") + "組";
                } else {
                    final String hrclass = rs.getString("HR_CLASS");
                    hrclassName1 = (StringUtils.isNumeric(hrclass) ? String.valueOf(Integer.parseInt(hrclass)) : hrclass) + "組";
                }
                final String attendno = zeroSuppress(rs.getString("ATTENDNO"));
                final String name = rs.getString("NAME");
                final String kana = rs.getString("NAME_KANA");
                
                final int lenNameShow = getMS932Count(name);
                final int lenKana = getMS932Count(kana);
                
                svf.VrsOut("GRADE1", gradeCd);
                svf.VrsOut("CLASS1", hrclassName1);
                svf.VrsOut("ATTENDNO1", attendno);
                svf.VrsOut("NAME1" + (lenNameShow < 20 ? "_1" : lenNameShow < 34 ? "_2" : "_3"), name);
                svf.VrsOut("NAME_KANA1"+ (lenKana < 30 ? "" : "_2"), kana);
                svf.VrsOut("MONEY1", _param._money);
                svf.VrsOut("MONEY2", _param._money);
                svf.VrEndPage();
                
                _hasData = true;
            }
            
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String zeroSuppress(final String str) throws SQLException {
        return StringUtils.isNumeric(str) ? String.valueOf(Integer.parseInt(str)) : null;
    }
    
    private int getMS932Count(String str) {
        int c = 0;
        try {
            c = str.getBytes("MS932").length;
        } catch (Exception e) {
        }
        return c;
    }
    
    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T4.GRADE_CD, ");
        stb.append("     T3.HR_CLASS_NAME1, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_SHOW, ");
        stb.append("     T2.NAME_KANA ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T3.GRADE = T1.GRADE ");
        stb.append("         AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR ");
        stb.append("         AND T4.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year  +"' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester  +"' ");
        stb.append("     AND T1.GRADE = '" + _param._grade  +"' ");
        if ("1".equals(_param._categoryIsClass)) {
            stb.append("     AND T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected)  +" ");
        } else {
            stb.append("     AND T1.HR_CLASS = '" + _param._hrClass  +"' ");
            stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected)  +" ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.HR_CLASS, T1.ATTENDNO ");
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
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String _hrClass;
        private final String _categoryIsClass;       // 選択区分
        private final String[] _categorySelected;
        private final String _money;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _money = request.getParameter("MONEY");
        }
    }
}

// eof

