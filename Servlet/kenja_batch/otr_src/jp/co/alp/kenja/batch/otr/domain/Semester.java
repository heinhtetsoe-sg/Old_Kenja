// kanji=����
/*
 * $Id: Semester.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2004/06/04 21:03:10 - JST
 * �쐬��: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �w���B
 * @version $Id: Semester.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class Semester {
    /** log */
    private static final Log log = LogFactory.getLog(Semester.class);
    private static final Class MYCLASS = Semester.class;

    private final String _semes;
    private final String _year;
    private final String _name;
    private final KenjaDateImpl _sDate;
    private final KenjaDateImpl _eDate;

    /*
     * �R���X�g���N�^�B
     */
    private Semester(
            final String year,
            final String semes,
            final String name,
            final KenjaDateImpl sDate,
            final KenjaDateImpl eDate
    ) {
        _year = year;
        _semes = semes;
        _name = name;
        _sDate = sDate;
        _eDate = eDate;
    }

    /**
     * �N�𓾂�B
     * @return �N
     */
    public String getYear() {
        return _year;
    }

    /**
     * �w���𓾂�B
     * @return �w��
     */
    public String getSemesterString() {
        return _semes;
    }

    /**
     * �w�����𓾂�B
     * @return �w����
     */
    public String getName() {
        return _name;
    }

    /**
     * �w���J�n���t�𓾂�B
     * @return �w���J�n���t
     */
    public KenjaDateImpl getSDate() {
        return _sDate;
    }

    /**
     * �w���I�����t�𓾂�B
     * @return �w���I�����t
     */
    public KenjaDateImpl getEDate() {
        return _eDate;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _semes + ":" + _name + "[" + _sDate + "," + _eDate + "]";
    }

    /**
     * �w��
     * @param year �N
     * @param semes �w���R�[�h
     * @param name �w����
     * @param sDate �w���J�n���t
     * @param eDate �w���I�����t
     * @return �w��
     */
    public static Semester create(
            final String year,
            final String semes,
            final String name,
            final KenjaDateImpl sDate,
            final KenjaDateImpl eDate
    ) {
        if (null == year)  { throw new IllegalArgumentException("�������s��(year)"); }
        if (null == semes) { throw new IllegalArgumentException("�������s��(code)"); }
        if (null == name)  { throw new IllegalArgumentException("�������s��(name)"); }
        if (null == sDate) { throw new IllegalArgumentException("�������s��(sDate)"); }
        if (null == eDate) { throw new IllegalArgumentException("�������s��(eDate)"); }

        return new Semester(year, semes, name, sDate, eDate);
    }

    /**
     * �w�肳�ꂽdate�̊w����Ԃ�
     * @param db DB
     * @param date �w�肳�ꂽ�N����
     * @return date�̊w��
     * @throws SQLException SQL��O
     */
    public static Semester load(final DB2UDB db, final String date) throws SQLException {
        Semester semester = null;
        boolean found = false;
        db.query("SELECT year, semester, semestername, sdate, edate FROM semester_mst WHERE '" + date + "' BETWEEN sdate AND edate AND semester <> '9' ");
        final ResultSet rs = db.getResultSet();
        if (rs.next()) {
            found = true;
            final String year = rs.getString("year");
            final String semes = rs.getString("semester");
            final String name = rs.getString("semestername");
            final Date sqlSdate = Date.valueOf(rs.getString("sdate"));
            final Date sqlEdate = Date.valueOf(rs.getString("edate"));
            final KenjaDateImpl sdate = KenjaDateImpl.getInstance(sqlSdate);
            final KenjaDateImpl edate = KenjaDateImpl.getInstance(sqlEdate);

            semester = new Semester(year, semes, name, sdate, edate);
        }
        if (!found) {
            throw new SQLException("�w���f�[�^���Z�b�g����Ă��܂��� : " + date);
        }
        
        log.info("�Ώ۔N�x=" + semester.getYear() + ", �w��=" + semester.getSemesterString() + ", �J�n��=" + semester.getSDate() + ", �I����=" + semester.getEDate());
        db.commit();
        rs.close();
        return semester;
    }

} // Semester

// eof
