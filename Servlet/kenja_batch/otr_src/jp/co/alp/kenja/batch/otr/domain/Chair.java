// kanji=Ώ
/*
 * $Id: Chair.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * uΐB
 * @author maesiro
 * @version $Id: Chair.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Chair {

    private static final Log log = LogFactory.getLog(Chair.class);

    /** uΐR[h */
    private final String _code;
    /** ΘΪR[h */
    private final String _subclassCd;
    /** uΐΌΜ */
    private final String _name;
    /** A±g */
    private final Integer _frameCount;
    /** σuNX */
    private List _homeRooms = new ArrayList();
    /** σuΆk */
    private List _students = new ArrayList();
    /** uΐSEυ(ζΆ) */
    private List _staffs = new ArrayList();
    /** gp{έ */
    private List _facilities = new ArrayList();


    /**
     * RXgN^
     * @param code uΐR[h
     * @param subclassCd ΘΪR[h
     * @param name uΐΌ
     * @param frameCount A±φΖ
     */
    public Chair(
            final String code,
            final String subclassCd,
            final String name,
            final Integer frameCount
    ) {
        _code = code;
        _subclassCd = subclassCd;
        _name = name;
        _frameCount = frameCount;
    }

    /**
     * uΐπΎι
     * @return uΐ
     */
    public String getChairCd() { return _code; }
    /**
     * ΘΪπΎι
     * @return ΘΪ
     */
    public String getSubclass() { return _subclassCd; }
    /**
     * uΐΌΜπΎι
     * @return uΐΌΜ
     */
    public String getName() { return _name; }
    /**
     * σuNXπΎι
     * @return σuNX
     * @deprecated CHAIR_CLS_DATΝgp΅Θ’
     */
    public List getHomeRooms() { return _homeRooms; }
    /**
     * σuΆkπΎι
     * @return σuΆk
     */
    public List getStudents() { return _students; }
    /**
     * X^btΜR[hπΎι
     * @return X^btΜR[h
     */
    public List getStaffCodes() { return _staffs; }
    /**
     * A±φΖ©»θB
     * @return A±φΖΘη<code>true</code>
     * @deprecated A±φΖΜ»θΙΝgp΅Θ’±ΖΙ΅½(ver1.4~)
     */
    public boolean isContinuance() {
        return null != _frameCount && 2 <= _frameCount.intValue();
    }
    /**
     * {έπΗΑ·ι
     */
    private void addFacility(final Facility fac) {
        _facilities.add(fac);
    }
    /**
     * gp·ι{έπΎι
     * @return gp·ι{έ
     */
    public List getFacilities() {
        return _facilities;
    }

    /**
     * uΐf[^π[h·ι
     * @param db DB
     * @param year N
     * @param semester wϊ
     * @return uΐf[^Μ}bv
     * @throws SQLException SQLαO
     */
    public static Map load(final DB2UDB db, final String year, final String semester) throws SQLException {
        final Map rtn = new HashMap();

        ResultSet rs = null;
        db.query(sql(year, semester));
        rs = db.getResultSet();
        while (rs.next()) {
            final String chairCd = rs.getString("chaircd");
            final String subclassCd = rs.getString("subclasscd");
            final String chairname = rs.getString("chairname");
            final Integer frameCount = rs.getString("framecnt") == null ? new Integer(0) : Integer.valueOf(rs.getString("framecnt"));
            final Chair chair = new Chair(chairCd, subclassCd, chairname, frameCount);
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
            + "  chairname,"
            + "  framecnt"
            + " FROM"
            + "  chair_dat"
            + " WHERE"
            + "  year='" + year + "' AND"
            + "  semester='" + semester + "'"
            ;
        return sql;
    }

    /**
     * uΐ}bvΜ»κΌκΜuΐΜζΆπ[h·ι
     * @param chairs uΐ}bv
     * @param db DB
     * @param year N
     * @param semester wϊ
     * @throws SQLException SQLαO
     */
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
        while (rs.next()) {
            final String code = rs.getString("chaircd");
            final String staffCd = rs.getString("staffcd");
            final Chair chair = (Chair) chairs.get(code);
            if (null != chair) {
                chair._staffs.add(staffCd);
            } else {
                log.debug("Y·ιuΐͺ θάΉρB uΐR[h = [" + code + "]");
            }
        }
    }

    /**
    *
    * @param chairs uΐ
    * @param db DB
    * @param year N
    * @param semester wϊ
    * @throws SQLException SQLαO
    */
   public static void loadStudents(
           final Map chairs,
           final DB2UDB db,
           final String year,
           final String semester,
           final String executedate
   ) throws SQLException {
       final String sql;
       sql = "SELECT"
           + "  t1.chaircd,"
           + "  t1.schregno"
           + " FROM"
           + "  chair_std_dat t1"
           + "  INNER JOIN schreg_regd_dat t2 ON"
           + "   t2.schregno = t1.schregno AND"
           + "   t2.year = t1.year AND"
           + "   t2.semester = t1.semester"
           + " WHERE"
           + "  t1.year='" + year + "' AND"
           + "  t1.semester='" + semester + "' AND"
           + "  '" + executedate + "' BETWEEN appdate AND appenddate"
           ;

       ResultSet rs = null;
       db.query(sql);
       rs = db.getResultSet();
       while (rs.next()) {
           final String code = rs.getString("chaircd");
           final Chair chair = (Chair) chairs.get(code);
           if (null == chair) {
               // S~f[^
               continue;
           }
           final String schregno = rs.getString("schregno");
           chair._students.add(schregno);
       }
   }

    /**
     *
     * @param chairs uΐ
     * @param db DB
     * @param year N
     * @param semester wϊ
     * @param hrs HRNXΜ}bv
     * @throws SQLException SQLαO
     * @deprecated CHAIR_CLS_DATΝgp΅Θ’
     */
    public static void loadClasses(
            final Map chairs,
            final DB2UDB db,
            final String year,
            final String semester,
            final Map hrs
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
        while (rs.next()) {
            final String code = rs.getString("chaircd");
            final String grade = rs.getString("trgtgrade");
            final String hrClass = rs.getString("trgtclass");
            final HomeRoom hr = (HomeRoom) hrs.get(grade + hrClass);
            if (null == hr) {
                continue;
            }
            final Chair chair = (Chair) chairs.get(code);
            if (null == chair) {
                // S~f[^
                continue;
            }
            chair._homeRooms.add(hr);
        }
    }

    /**
    *
    * @param chairs uΐ
    * @param db DB
    * @param year N
    * @param semester wϊ
    * @param facs gp{έΜ}bv
    * @throws SQLException SQLαO
    */
   public static void loadFacilities(
           final Map chairs,
           final DB2UDB db,
           final String year,
           final String semester,
           final Map facs
   ) throws SQLException {
       final String sql;
       sql = "SELECT"
           + "  chaircd,"
           + "  faccd"
           + " FROM"
           + "  chair_fac_dat"
           + " WHERE"
           + "  year='" + year + "' AND"
           + "  semester='" + semester + "'"
           ;

       ResultSet rs = null;
       db.query(sql);
       rs = db.getResultSet();
       while (rs.next()) {
           final String code = rs.getString("chaircd");
           final String faccd = rs.getString("faccd");
           final Facility fac = (Facility) facs.get(faccd);
           if (null == fac) {
               continue;
           }
           final Chair chair = (Chair) chairs.get(code);
           if (null == chair) {
               // S~f[^
               continue;
           }
           chair.addFacility(fac);
           log.debug("uΐ{έf[^ uΐ=[" + chair + "] , {έ=" + chair.getFacilities());
       }
   }

   /**
     * ζΆ©
     * @param staffno έΠΤ
     * @return ζΆΘηtrueA»€ΕΘ―κΞfalseπΤ·B
     */
    public boolean hasTeacher(final String staffno) {
        return _staffs.contains(staffno);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "uΐR[h=[" + getChairCd() +"]A uΐΌΜ=[" + getName() + "] ";
    }

} // Chair

// eof
