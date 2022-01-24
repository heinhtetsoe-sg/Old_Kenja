// kanji=漢字
/*
 * $Id: SubClass.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/05/22 14:57:13 - JST
 * 作成者: takaesu
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
 * 科目。
 * @author takaesu
 * @version $Id: SubClass.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class SubClass {
    private final String _clazzCd;
    private final String _curriculumCd;
    private final String _code;
    private final String _name;
    private final String _abbv;

    public SubClass(
            final String clazzCd,
            final String curriculumCd,
            final String code,
            final String name,
            final String abbv
    ) {
        _clazzCd = clazzCd;
        _curriculumCd = curriculumCd;
        _code = code;
        _name = name;
        _abbv = abbv;
    }

    /**
     * 教科コードを得る。
     * @return 教科コード
     */
    public String getClassCd() { return _clazzCd; }

    /**
     * 教育課程年度コードを得る。
     * @return 教育課程年度コード
     */
    public String getCurriculumCd() { return _curriculumCd; }

    /**
     * 科目コードを得る。
     * @return 科目コード
     */
    public String getCode() { return _code; }

    public String getName() { return _name; }

    public String getAbbv() { return _abbv; }

    public String toString() {
        return _clazzCd + _curriculumCd + _code + "/" + _abbv;
    }

    public static Map load(final Database db, final String year) throws SQLException {
        final Map rtn = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = "SELECT classcd, curriculum_cd, subclasscd, subclassname, subclassabbv FROM v_subclass_mst WHERE year='" + year + "'";
        ps = db.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String clazzCd = rs.getString("classcd");
            final String curriculumCd = rs.getString("curriculum_cd");
            final String code = rs.getString("subclasscd");
            final String name = rs.getString("subclassname");
            final String abbv = rs.getString("subclassabbv");

            final SubClass subClass = new SubClass(clazzCd, curriculumCd, code, name, abbv);
            rtn.put(clazzCd + curriculumCd + code, subClass);
        }

        return rtn;
    }
} // Clazz

// eof
