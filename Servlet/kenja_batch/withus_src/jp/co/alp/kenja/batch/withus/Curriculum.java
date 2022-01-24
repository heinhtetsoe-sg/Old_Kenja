// kanji=漢字
/*
 * $Id: Curriculum.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/05/17 9:52:29 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

/**
 * 教育課程。
 * @author takaesu
 * @version $Id: Curriculum.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Curriculum {
    private static Map _map = new HashMap();

    private final String _namecd2;
    private final String _name3;
    private final String _abbv1;
    private final String _namespare1;
    private final String _namespare2;

    public Curriculum(
            final String namecd2,
            final String name3,
            final String abbv1,
            final String namespare1,
            final String namespare2
    ) {
        _namecd2    = namecd2;
        _name3      = name3;
        _abbv1      = abbv1;
        _namespare1 = namespare1;
        _namespare2 = namespare2;
    }

    public static void loadCurriculumMst(final Database db) throws SQLException {
        try {
            db.query("SELECT namecd2, name3, abbv1, namespare1, namespare2 FROM name_mst WHERE namecd1 = 'W002'");
            ResultSet rs = db.getResultSet();
            while (rs.next()) {
                final String namecd2    = rs.getString("namecd2");
                final String name3      = rs.getString("name3");
                final String abbv1      = rs.getString("abbv1");
                final String namespare1 = rs.getString("namespare1");
                final String namespare2 = rs.getString("namespare2");
                _map.put(namecd2, new Curriculum(namecd2, name3, abbv1, namespare1, namespare2));
            }
            db.commit();
            rs.close();
        } catch (final SQLException e) {
            throw e;
        }
    }

    /**
     * 教育課程コードより年度を得る。
     * @param code  教育課程コード
     * @return      教育課程年度
     */
    public static String getCurriculumYear(final String code) {
        final Curriculum data = (Curriculum) _map.get(code);
        if (null == data) {
            return "";
        }
        return data._name3;
    }

    /**
     * 今年度の教育課程年度を得る。
     * @return 教育課程年度
     */
    public static String getCurriculumFirstYear(final String year) {
        final Curriculum curriculum = getCurriculum(year);
        return curriculum._name3;
    }

    /**
     * 年度より教育課程コードを得る。
     * @param year  教育課程年度
     * @return      教育課程コード
     */
    public static String getCurriculumCd(final String year) {
        final Curriculum curriculum = getCurriculum(year);
        return (null == curriculum) ? "" : curriculum._namecd2;
    }

    private static Curriculum getCurriculum(final String year) {
        for (Iterator it = _map.values().iterator(); it.hasNext();) {
            final Curriculum data = (Curriculum) it.next();

            final int intYear = Integer.parseInt(year);
            final int fromYear = Integer.parseInt(data._namespare1);
            final int toYear = Integer.parseInt(data._namespare2);
            if (fromYear <= intYear && toYear >= intYear) {
                return data;
            }
        }
        return null;
    }
} // Curriculum

// eof
