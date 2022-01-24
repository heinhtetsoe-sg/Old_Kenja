// kanji=漢字
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
 * 講座。
 * @author maesiro
 * @version $Id: Chair.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Chair {

    private static final Log log = LogFactory.getLog(Chair.class);

    /** 講座コード */
    private final String _code;
    /** 科目コード */
    private final String _subclassCd;
    /** 講座名称 */
    private final String _name;
    /** 連続枠数 */
    private final Integer _frameCount;
    /** 受講クラス */
    private List _homeRooms = new ArrayList();
    /** 受講生徒 */
    private List _students = new ArrayList();
    /** 講座担当職員(先生) */
    private List _staffs = new ArrayList();
    /** 使用施設 */
    private List _facilities = new ArrayList();


    /**
     * コンストラクタ
     * @param code 講座コード
     * @param subclassCd 科目コード
     * @param name 講座名
     * @param frameCount 連続授業数
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
     * 講座を得る
     * @return 講座
     */
    public String getChairCd() { return _code; }
    /**
     * 科目を得る
     * @return 科目
     */
    public String getSubclass() { return _subclassCd; }
    /**
     * 講座名称を得る
     * @return 講座名称
     */
    public String getName() { return _name; }
    /**
     * 受講クラスを得る
     * @return 受講クラス
     * @deprecated CHAIR_CLS_DATは使用しない
     */
    public List getHomeRooms() { return _homeRooms; }
    /**
     * 受講生徒を得る
     * @return 受講生徒
     */
    public List getStudents() { return _students; }
    /**
     * スタッフのコードを得る
     * @return スタッフのコード
     */
    public List getStaffCodes() { return _staffs; }
    /**
     * 連続授業か判定。
     * @return 連続授業なら<code>true</code>
     * @deprecated 連続授業の判定には使用しないことにした(ver1.4~)
     */
    public boolean isContinuance() {
        return null != _frameCount && 2 <= _frameCount.intValue();
    }
    /**
     * 施設を追加する
     */
    private void addFacility(final Facility fac) {
        _facilities.add(fac);
    }
    /**
     * 使用する施設を得る
     * @return 使用する施設
     */
    public List getFacilities() {
        return _facilities;
    }

    /**
     * 講座データをロードする
     * @param db DB
     * @param year 年
     * @param semester 学期
     * @return 講座データのマップ
     * @throws SQLException SQL例外
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
     * 講座マップのそれぞれの講座の先生をロードする
     * @param chairs 講座マップ
     * @param db DB
     * @param year 年
     * @param semester 学期
     * @throws SQLException SQL例外
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
                log.debug("該当する講座がありません。 講座コード = [" + code + "]");
            }
        }
    }

    /**
    *
    * @param chairs 講座
    * @param db DB
    * @param year 年
    * @param semester 学期
    * @throws SQLException SQL例外
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
               // ゴミデータ
               continue;
           }
           final String schregno = rs.getString("schregno");
           chair._students.add(schregno);
       }
   }

    /**
     *
     * @param chairs 講座
     * @param db DB
     * @param year 年
     * @param semester 学期
     * @param hrs HRクラスのマップ
     * @throws SQLException SQL例外
     * @deprecated CHAIR_CLS_DATは使用しない
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
                // ゴミデータ
                continue;
            }
            chair._homeRooms.add(hr);
        }
    }

    /**
    *
    * @param chairs 講座
    * @param db DB
    * @param year 年
    * @param semester 学期
    * @param facs 使用施設のマップ
    * @throws SQLException SQL例外
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
               // ゴミデータ
               continue;
           }
           chair.addFacility(fac);
           log.debug("講座施設データ 講座=[" + chair + "] , 施設=" + chair.getFacilities());
       }
   }

   /**
     * 先生か
     * @param staffno 在籍番号
     * @return 先生ならtrue、そうでなければfalseを返す。
     */
    public boolean hasTeacher(final String staffno) {
        return _staffs.contains(staffno);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "講座コード=[" + getChairCd() +"]、 講座名称=[" + getName() + "] ";
    }

} // Chair

// eof
