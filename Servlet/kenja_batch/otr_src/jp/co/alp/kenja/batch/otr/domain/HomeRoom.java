// kanji=����
/*
 * $Id: HomeRoom.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/25 9:55:50 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nao_package.db.DB2UDB;

/**
 * �N�g�B
 * @version $Id: HomeRoom.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class HomeRoom implements Comparable {
    /** �w�N */
    private final String _grade;
    /** HR�N���X */
    private final String _hrClass;
    /** HR���� */
    private final String _name;
    /** �N���X�̊w�Дԍ����X�g */
    private final List _studentList;

    /**
     * �R���X�g���N�^
     * @param grade �w�N
     * @param hrClass HR�N���X
     * @param name ����
     */
    public HomeRoom(final String grade, final String hrClass, final String name) {
        _grade = grade;
        _hrClass = hrClass;
        _name = name;
        _studentList = new ArrayList();
    }

    /**
     * �w�N�𓾂�
     * @return �w�N
     */
    public String getGrade() {
        return _grade;
    }

    /**
     * HR�N���X�𓾂�
     * @return HR�N���X
     */
    public String getHrClass() {
        return _hrClass;
    }

    /**
     * HR���̂𓾂�
     * @return HR����
     */
    public String getName() {
        return _name;
    }

    /**
     * �w�N�R�[�h+�g�R�[�h�𓾂�B
     * @return �w�N�R�[�h+�g�R�[�h
     */
    public String getCode() {
        return _grade + _hrClass;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _grade + ", " + _hrClass + ", " + _name;
    }

    /**
     * ���̔N�g�Ɋw����ǉ�����
     * @param schregno �w���̊w�Дԍ�
     */
    public void addStudent(final String schregno) {
        _studentList.add(schregno);
    }

    /**
     * ���̔N�g�Ɏw�肳�ꂽ�w�������邩
     * @param schregno �w���̊w�Дԍ�
     * @return ���̔N�g�Ɏw�肳�ꂽ�w��������Ȃ�true�A�����łȂ����false
     */
    public boolean hasStudent(final String schregno) {
        return _studentList.contains(schregno);
    }

    /**
     * ���̔N�g�ɏ�������w���̐���Ԃ�
     * @return ���̔N�g�ɏ�������w���̐�
     */
    public int getStudentCount() {
        return _studentList.size();
    }


    /**
     * ���̔N�g�̍ݐДԍ��̃��X�g��Ԃ�
     * @return �ݐДԍ��̃��X�g
     */
    public List getStudentList() {
        return _studentList;
    }

    /**
     * HR�N���X�f�[�^�����[�h����
     * @param db DB
     * @param year �N
     * @param semester �w��
     * @return HR�N���X�f�[�^
     * @throws SQLException SQL��O
     */
    public static Map load(final DB2UDB db, final String year, final String semester) throws SQLException {
        final Map rtn = new TreeMap();

        ResultSet rs = null;
        db.query(getRegdHdatSql(year, semester));
        rs = db.getResultSet();
        while (rs.next()) {
            final String grade = rs.getString("grade");
            final String hrClass = rs.getString("hr_class");
            final String name = rs.getString("hr_nameabbv");

            final HomeRoom hr = new HomeRoom(grade, hrClass, name);
            rtn.put(grade + hrClass, hr);
        }

        db.query(getRegdDatSql(year, semester));
        rs = db.getResultSet();
        while (rs.next()) {
            final String grade = rs.getString("grade");
            final String hrClass = rs.getString("hr_class");
            final String schregno = rs.getString("schregno");

            final HomeRoom hr = (HomeRoom) rtn.get(grade + hrClass);
            if (hr != null) {
                hr.addStudent(schregno);
            }
        }

        return rtn;
    }

    /**
     * �N�g�N���X���擾����SQL�𓾂�
     * �w����1�l�ȏ㏊�����Ă���N�g��ΏۂƂ���
     * @param year �w�N
     * @param semester �w��
     * @return �N�g���擾����SQL
     */
    private static String getRegdHdatSql(final String year, final String semester) {
        final String sql;
        sql = "SELECT"
            + "  t1.grade,"
            + "  t1.hr_class,"
            + "  t1.hr_nameabbv,"
            + "  count(*)"
            + " FROM"
            + "  schreg_regd_hdat t1"
            + "  INNER JOIN schreg_regd_dat t2 on"
            + "    t2.grade = t1.grade AND"
            + "    t2.hr_class = t1.hr_class AND"
            + "    t2.year = t1.year AND"
            + "    t2.semester = t1.semester"
            + " WHERE"
            + "  t1.year='" + year + "' AND"
            + "  t1.semester='" + semester + "' "
            + " GROUP BY"
            + "  t1.grade, t1.hr_class, t1.hr_nameabbv"
            + " HAVING"
            + "  count(*) <> 0"
            ;
        return sql;
    }

    /**
     * �N�g�ɏ�������w�����擾����SQL�𓾂�
     * @param year �N�x 
     * @param semester �w��
     * @return �N�g�ɏ�������w�����擾����SQL
     */
    private static String getRegdDatSql(final String year, final String semester) {
        final String sql;
        sql = "SELECT"
            + "  grade,"
            + "  hr_class,"
            + "  schregno"
            + " FROM"
            + "  schreg_regd_dat"
            + " WHERE"
            + "  year='" + year + "' AND"
            + "  semester='" + semester + "'"
            + " ORDER BY"
            + "  grade, hr_class, attendno"
            ;
        return sql;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Object o) {
        if (!(o instanceof HomeRoom)) {
            return -1;
        }
        final HomeRoom that = (HomeRoom) o;
        int cmp = _grade.compareTo(that._grade);
        if (cmp == 0) {
            cmp = _hrClass.compareTo(that._hrClass);
        }
        return cmp;
    }
} // HomeRoom

// eof
