// kanji=äøéö
/*
 * $Id: Clazz.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * çÏê¨ì˙: 2008/05/22 14:57:13 - JST
 * çÏê¨é“: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus;

import java.util.HashMap;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

/**
 * ã≥â»ÅB
 * @author takaesu
 * @version $Id: Clazz.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Clazz {
    private final String _code;
    private final String _name;
    private final String _abbv;

    public Clazz(final String code, final String name, final String abbv) {
        _code = code;
        _name = name;
        _abbv = abbv;
    }

    public String getCode() { return _code; }

    public String getName() { return _name; }

    public String getAbbv() { return _abbv; }

    public String toString() {
        return _code + "/" + _abbv;
    }

    public static Map loadClassMst(final Database db, final String year) throws SQLException {
        final Map rtn = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = "SELECT classcd, classname, classabbv FROM v_class_mst WHERE year='" + year + "'";
        ps = db.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = rs.getString("classcd");
            final String name = rs.getString("classname");
            final String abbv = rs.getString("classabbv");

            final Clazz clazz = new Clazz(code, name, abbv);
            rtn.put(code, clazz);
        }

        return rtn;
    }
} // Clazz

// eof
