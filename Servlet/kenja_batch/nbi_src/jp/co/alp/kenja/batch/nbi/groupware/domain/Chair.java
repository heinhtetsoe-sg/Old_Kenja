// kanji=����
/*
 * $Id: Chair.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/24 14:41:07 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * �u���B
 * @author takaesu
 * @version $Id: Chair.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Chair {
    /**
     * �u�Q�ł͂Ȃ��v��\������R�[�h�B
     */
    private static final String _NORMAL_CODE = "0000";
    private final String _chairCd;
    private final String _groupCd;
    private final SubClass _subclass;
    private final String _name;

    private List _homeRooms = new ArrayList();
    private List _staffs = new ArrayList();

    /** �Q�ɑ�����N�g */
    private static final MultiMap _gunClasses = new MultiHashMap();

    public Chair(
            final String chairCd,
            final String groupCd,
            final SubClass subclassCd,
            final String name
    ) {
        _chairCd = chairCd;
        _groupCd = groupCd;
        _subclass = subclassCd;
        _name = name;
    }

    public String getChairCd() { return _chairCd; }
    public String getGroupCd() { return _groupCd; }
    public SubClass getSubclass() { return _subclass; }
    public String getName() { return _name; }
    public List getHomeRooms() { return _homeRooms; }
    public List getStaffCodes() { return _staffs; }

    public static Map load(final DB2UDB db, final String year, final String semester, final Map classes) throws SQLException {
        final Map rtn = new HashMap();

        ResultSet rs = null;
        db.query(sql(year, semester));
        rs = db.getResultSet();
        while(rs.next()) {
            final String chairCd = rs.getString("chaircd");
            final String groupCd = rs.getString("groupcd");
            final String subclassCd = rs.getString("subclasscd");
            final String chairname = rs.getString("chairname");

            final SubClass subClass = (SubClass) classes.get(subclassCd);
            if (null == subClass) {
                continue;
            }

            final Chair chair = new Chair(chairCd, groupCd, subClass, chairname);
            rtn.put(chairCd, chair);
        }

        return rtn;
    }

    private static String sql(final String year, final String semester) {
        final String sql;
        sql = "SELECT"
            + "  chaircd,"
            + "  groupcd,"
            + "  subclasscd,"
            + "  chairname"
            + " FROM"
            + "  chair_dat"
            + " WHERE"
            + "  year='" + year + "' AND"
            + "  semester='" + semester + "'"
            ;
        return sql;
    }

    public static void loadStaffs(
            final Map chairs,
            final DB2UDB db,
            final String year,
            final String semester
    ) throws SQLException {
        final String sql;
        sql = "SELECT"
            + "  chaircd,"
            + "  staffcd"
            + " FROM"
            + "  chair_stf_dat"
            + " WHERE"
            + "  year='" + year + "' AND"
            + "  semester='" + semester + "'"
            ;

        ResultSet rs = null;
        db.query(sql);
        rs = db.getResultSet();
        while(rs.next()) {
            final String chairCd = rs.getString("chaircd");
            final String staffCd = rs.getString("staffcd");
            final Chair chair = (Chair) chairs.get(chairCd);
            if (null != chair) {
                chair._staffs.add(staffCd);
            } else {
                // �S�~�f�[�^
            }
        }
    }

    public static void loadClasses(
            final Map chairs,
            final DB2UDB db,
            final String year,
            final String semester,
            final Map homeRooms
    ) throws SQLException {
        final String sql;
        sql = "SELECT"
            + "  chaircd,"
            + "  groupcd,"
            + "  trgtgrade,"
            + "  trgtclass"
            + " FROM"
            + "  chair_cls_dat"
            + " WHERE"
            + "  year='" + year + "' AND"
            + "  semester='" + semester + "'"
            ;

        ResultSet rs = null;
        db.query(sql);
        rs = db.getResultSet();
        while(rs.next()) {
            final String chairCd = rs.getString("chaircd");
            final String groupCd = rs.getString("groupcd");
            final String grade = rs.getString("trgtgrade");
            final String hrClass = rs.getString("trgtclass");
            final HomeRoom hr = (HomeRoom) homeRooms.get(grade + hrClass);
            if (null == hr) {
                continue;
            }
            if (_NORMAL_CODE.equals(groupCd)) {
                final Chair chair = (Chair) chairs.get(chairCd);
                if (null == chair) {
                    // �S�~�f�[�^
                    continue;
                }
                if (!_NORMAL_CODE.equals(chair.getGroupCd())) {
                    // �s��!
                    continue;
                }
                chair._homeRooms.add(hr);
            } else {
                // �Q�ɂԂ牺����N�g
                _gunClasses.put(groupCd, hr);
            }
        }
    }

    /**
     * �Q�ɂԂ牺����N�g�� MultiMap �𓾂�B
     * @return �Q�ɂԂ牺����N�g�� MultiMap
     */
    public static MultiMap getGunClasses() { return _gunClasses; }

    /**
     * �Q�����ׂ�B
     * @return �Q�Ȃ�true
     */
    public boolean isGun() {
        return !_NORMAL_CODE.equals(_groupCd);
    }
} // Chair

// eof
