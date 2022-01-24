// kanji=����
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
 * �u���B
 * @author maesiro
 * @version $Id: Chair.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Chair {

    private static final Log log = LogFactory.getLog(Chair.class);

    /** �u���R�[�h */
    private final String _code;
    /** �ȖڃR�[�h */
    private final String _subclassCd;
    /** �u������ */
    private final String _name;
    /** �A���g�� */
    private final Integer _frameCount;
    /** ��u�N���X */
    private List _homeRooms = new ArrayList();
    /** ��u���k */
    private List _students = new ArrayList();
    /** �u���S���E��(�搶) */
    private List _staffs = new ArrayList();
    /** �g�p�{�� */
    private List _facilities = new ArrayList();


    /**
     * �R���X�g���N�^
     * @param code �u���R�[�h
     * @param subclassCd �ȖڃR�[�h
     * @param name �u����
     * @param frameCount �A�����Ɛ�
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
     * �u���𓾂�
     * @return �u��
     */
    public String getChairCd() { return _code; }
    /**
     * �Ȗڂ𓾂�
     * @return �Ȗ�
     */
    public String getSubclass() { return _subclassCd; }
    /**
     * �u�����̂𓾂�
     * @return �u������
     */
    public String getName() { return _name; }
    /**
     * ��u�N���X�𓾂�
     * @return ��u�N���X
     * @deprecated CHAIR_CLS_DAT�͎g�p���Ȃ�
     */
    public List getHomeRooms() { return _homeRooms; }
    /**
     * ��u���k�𓾂�
     * @return ��u���k
     */
    public List getStudents() { return _students; }
    /**
     * �X�^�b�t�̃R�[�h�𓾂�
     * @return �X�^�b�t�̃R�[�h
     */
    public List getStaffCodes() { return _staffs; }
    /**
     * �A�����Ƃ�����B
     * @return �A�����ƂȂ�<code>true</code>
     * @deprecated �A�����Ƃ̔���ɂ͎g�p���Ȃ����Ƃɂ���(ver1.4~)
     */
    public boolean isContinuance() {
        return null != _frameCount && 2 <= _frameCount.intValue();
    }
    /**
     * �{�݂�ǉ�����
     */
    private void addFacility(final Facility fac) {
        _facilities.add(fac);
    }
    /**
     * �g�p����{�݂𓾂�
     * @return �g�p����{��
     */
    public List getFacilities() {
        return _facilities;
    }

    /**
     * �u���f�[�^�����[�h����
     * @param db DB
     * @param year �N
     * @param semester �w��
     * @return �u���f�[�^�̃}�b�v
     * @throws SQLException SQL��O
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
     * �u���}�b�v�̂��ꂼ��̍u���̐搶�����[�h����
     * @param chairs �u���}�b�v
     * @param db DB
     * @param year �N
     * @param semester �w��
     * @throws SQLException SQL��O
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
                log.debug("�Y������u��������܂���B �u���R�[�h = [" + code + "]");
            }
        }
    }

    /**
    *
    * @param chairs �u��
    * @param db DB
    * @param year �N
    * @param semester �w��
    * @throws SQLException SQL��O
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
               // �S�~�f�[�^
               continue;
           }
           final String schregno = rs.getString("schregno");
           chair._students.add(schregno);
       }
   }

    /**
     *
     * @param chairs �u��
     * @param db DB
     * @param year �N
     * @param semester �w��
     * @param hrs HR�N���X�̃}�b�v
     * @throws SQLException SQL��O
     * @deprecated CHAIR_CLS_DAT�͎g�p���Ȃ�
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
                // �S�~�f�[�^
                continue;
            }
            chair._homeRooms.add(hr);
        }
    }

    /**
    *
    * @param chairs �u��
    * @param db DB
    * @param year �N
    * @param semester �w��
    * @param facs �g�p�{�݂̃}�b�v
    * @throws SQLException SQL��O
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
               // �S�~�f�[�^
               continue;
           }
           chair.addFacility(fac);
           log.debug("�u���{�݃f�[�^ �u��=[" + chair + "] , �{��=" + chair.getFacilities());
       }
   }

   /**
     * �搶��
     * @param staffno �ݐДԍ�
     * @return �搶�Ȃ�true�A�����łȂ����false��Ԃ��B
     */
    public boolean hasTeacher(final String staffno) {
        return _staffs.contains(staffno);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "�u���R�[�h=[" + getChairCd() +"]�A �u������=[" + getName() + "] ";
    }

} // Chair

// eof
