// kanji=漢字
/*
 * $Id: HomeRoom.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/25 9:55:50 - JST
 * 作成者: takaesu
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
 * 年組。
 * @author takaesu
 * @version $Id: HomeRoom.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class HomeRoom {
    private final String _grade;
    private final String _hrClass;
    private final String _name;

    public HomeRoom(final String grade, final String hrClass, final String name) {
        _grade = grade;
        _hrClass = hrClass;
        _name = name;
    }

    public String getGrade() {
        return _grade;
    }

    public String getHrClass() {
        return _hrClass;
    }

    public String getName() {
        return _name;
    }

    /**
     * 学年コード+組コードを得る。
     * @return 学年コード+組コード
     */
    public String getCode() {
        return _grade + _hrClass;
    }

    public String toString() {
        return _grade + ", " + _hrClass + ", " + _name;
    }

    public static Map load(final DB2UDB db, final String year, final String semester) throws SQLException {
        final Map rtn = new HashMap();

        ResultSet rs = null;
        db.query(getRegdHdatSql(year, semester));
        rs = db.getResultSet();
        while(rs.next()) {
            final String grade = rs.getString("grade");
            final String hrClass = rs.getString("hr_class");
            final String name = rs.getString("hr_nameabbv");

            final HomeRoom hr = new HomeRoom(grade, hrClass, name);
            rtn.put(grade + hrClass, hr);
        }

        return rtn;
    }

    private static String getRegdHdatSql(final String year, final String semester) {
        final String sql;
        sql = "SELECT"
            + "  grade,"
            + "  hr_class,"
            + "  hr_nameabbv"
            + " FROM"
            + "  schreg_regd_hdat"
            + " WHERE"
            + "  year='" + year + "' AND"
            + "  semester='" + semester + "'"
            ;
        return sql;
    }
} // HomeRoom

// eof
