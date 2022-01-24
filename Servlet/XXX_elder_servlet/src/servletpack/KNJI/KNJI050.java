// kanji=漢字
/*
 * $Id: 47bb7fef9eb5497484404cbe879019f629210f7c $
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJA.KNJA130;
import servletpack.KNJA.KNJA130B;
import servletpack.KNJA.KNJA130C;

/*
 *  学校教育システム 賢者 [卒業生管理] 卒業生用生徒指導要録
 */

public class KNJI050 {

    private static final Log log = LogFactory.getLog(KNJA130C.class);

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        DB2UDB db2 = null;

        boolean isCallKNJA130 = false;
        boolean isCallKNJA130B = false;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
            
            final String z010Name1 = getZ010(db2, "NAME1");
//            final boolean _isHousei = "HOUSEI".equals(z010Name1);
            final boolean _isKumamoto = "kumamoto".equals(z010Name1);
//            final boolean _isYuushinkan = "Yuushinkan".equals(z010Name1);
            final boolean _isChiben = "CHIBEN".equals(z010Name1);
            final boolean _isKindaiHigh = "KINDAI".equals(z010Name1);
//            final boolean _isKindaiJunior = "KINJUNIOR".equals(z010Name1);
            final boolean _isHirokoku = "hirogaku".equals(z010Name1);
            final boolean _isTokyoto = "tokyoto".equals(z010Name1);
            final boolean _isChukyo = "chukyo".equals(z010Name1);
            final boolean _isTokiwa = "tokiwa".equals(z010Name1);
            isCallKNJA130 = _isChiben || _isKindaiHigh || _isHirokoku || _isTokyoto || _isChukyo || _isTokiwa;
            isCallKNJA130B = _isKumamoto;
            
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
        if (isCallKNJA130) {
            new KNJA130().svf_out(request, response);
        } else if (isCallKNJA130B) {
            new KNJA130B().svf_out(request, response);
        } else {
            new KNJA130C().svf_out(request, response);
        }
    }

    private String getZ010(final DB2UDB db2, final String field) {
        String rtn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement("SELECT " + field + " FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2 = '00'");
            rs = ps.executeQuery();
            if (rs.next()) {
                rtn = rs.getString(field);
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

}//クラスの括り
