// kanji=äøéö
/*
 * $Id: SubClass.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * çÏê¨ì˙: 2008/04/25 11:28:24 - JST
 * çÏê¨é“: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware.domain;

import java.util.HashMap;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * â»ñ⁄ÅB
 * @author takaesu
 * @version $Id: SubClass.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class SubClass {
    private final String _code;
    private final String _name;
    private final String _abbv;

    public SubClass(final String code, final String name, final String abbv) {
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

    public static Map load(final DB2UDB db, final String year, final String semester) throws SQLException {
        final Map rtn = new HashMap();

        ResultSet rs = null;
        db.query(getSql(year, semester));
        rs = db.getResultSet();
        while(rs.next()) {
            final String code = rs.getString("subclasscd");
            final String name = rs.getString("subclassname");
            final String abbv = rs.getString("subclassabbv");

            final SubClass subClass = new SubClass(code, name, abbv);
            rtn.put(code, subClass);
        }

        return rtn;
    }

    private static String getSql(final String year, final String semester) {
        final String sql;
        sql = "SELECT DISTINCT"
            + "  t2.subclasscd,"
            + "  t2.subclassname,"
            + "  t2.subclassabbv"
            + " FROM"
            + "  chair_dat t1 inner join v_subclass_mst t2 on t1.year=t2.year and t1.subclasscd=t2.subclasscd"
            + " WHERE"
            + "  t1.year='" + year + "' AND"
            + "  t1.semester='" + semester + "'"
            ;
        return sql;
    }
} // SubClass

// eof
