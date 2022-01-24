// kanji=����
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/04
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �p�����[�^�B
 * @author takaesu
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {
    /*pkg*/static final Log log = LogFactory.getLog(Param.class);

    /** CSV�t�@�C���̕����R�[�h */
    public static final String encode = "MS932";

    /** �w�Z�R�[�h�B���Z */
    public static final String SCHOOL_CODE_HIGH = "1";

    private DB2UDB _db;

    private final String _dbUrl;
    /** ��ƂȂ�� */
    private final String _date;
    /** ��ƂȂ��(Calendar�^) */
    private final Calendar _calDate;
    private final boolean _isOutputMode;

    /** �t�H���_ */
    private final String _folder;

    /** �X�V�� */
    private final String _update;

    /** �N�x�B(�w���}�X�^) */
    protected String _year;
    /** �w���B(�w���}�X�^) */
    protected String _semester;
    /** �w���̊J�n�� */
    protected String _sdate;
    /** �w���̏I���� */
    protected String _edate;

    /** �w�Z���ʃR�[�h */
    private final String _schoolDiv;

    /** SimpleDateFormat */
    public static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    /** �f�o�b�O�p�̏o���͂��f�[�^CSV�t�@�C����. */
    String _debugFileName;

    /**
     * �R���X�g���N�^�B
     * @param args
     */
    public Param(final String[] args) throws ParseException {
        if (args.length < 5) {
            System.err.println("Usage: java Main <//localhost:50000/dbname> <yyyy-mm-dd> <-out|-in> <folder> <SchoolDiv> [groupYYYYMMDD.csv]");
            throw new IllegalArgumentException("�����̐����Ⴄ");
        }

        _dbUrl = args[0];
        _date = args[1];
        final String option = args[2].toLowerCase();
        _folder = args[3];
        _schoolDiv = args[4];
        if (6 <= args.length) {
            _debugFileName = args[5];
            log.debug("�f�o�b�O�p�̏o���͂��f�[�^CSV�t�@�C����=" + _debugFileName);
        }

        if (!"-out".equals(option) && !"-in".equals(option)) {
            throw new IllegalArgumentException("��3������ -in or -out");
        }
        _isOutputMode = "-out".equals(option) ? true : false;
        final File file = new File(_folder);
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("'" + _folder + "'�̓f�B���N�g���ł͂Ȃ�");
        }

        _update = SDF.format(new Date());
        log.info("�X�V��=" + _update + ", �w�Z�R�[�h=" + _schoolDiv);

        _calDate = Calendar.getInstance();
        try {
            final Date date = SDF.parse(_date);
            _calDate.setTime(date);
            
        } catch (final ParseException e) {
            log.fatal("���t���������͂ł��Ȃ�: " + _date);
            throw e;
        }
        log.debug("��ƂȂ��(Calendar�^)=" + _calDate.getTime());

        log.fatal("CSV�t�@�C���̕����R�[�h=" + encode);
    }

    public String getDbUrl() {
        return _dbUrl;
    }

    public String getDate() {
        return _date;
    }

    public Calendar getCalDate() {
        return _calDate;
    }

    public String getFolder() {
        return _folder;
    }

    public boolean isOutputMode() {
        return _isOutputMode;
    }

    public void load(final DB2UDB db) throws SQLException {
        loadSemesterMst(db);
    }

    private void loadSemesterMst(final DB2UDB db) throws SQLException {
        try {
            db.query("SELECT year, semester, sdate, edate FROM semester_mst WHERE '" + _date + "' BETWEEN sdate AND edate");
            ResultSet rs = db.getResultSet();
            if (rs.next()) {
                _year = rs.getString("year");
                _semester = rs.getString("semester");
                _sdate = rs.getString("sdate");
                _edate = rs.getString("edate");
            }
            db.commit();
            rs.close();
        } catch (final SQLException e) {
            log.fatal(_date + "����N�x�A�w���������Ȃ�! semester_mst����擾�o���Ȃ�");
            throw e;
        }
        try {
            Integer.parseInt(_year);
        } catch (final NumberFormatException e) {
            log.fatal(_date + "���狁�߂��N�x���������Ȃ�(semester_mst����擾�o���Ȃ�)��" + _year);
            throw e;
        }
        log.info("�Ώ۔N�x=" + _year + ", �w��=" + _semester + ", �J�n��=" + _sdate + ", �I����=" + _edate);
    }
    
    /**
     * �N�x�𓾂�B
     * (�w���}�X�^)
     * @return �N�x
     */
    public String getYear() {
        return _year;
    }

    /**
     * �w���𓾂�B
     * (�w���}�X�^)
     * @return �w��
     */
    public String getSemester() {
        return _semester;
    }

    /**
     * �X�V���𓾂�B
     * @return �X�V��
     */
    public String getUpdate() {
        return _update;
    }

    /**
     * �t���p�X�̃t�@�C�����𓾂�B
     * @param file �t�@�C����
     * @return �t���p�X�ȃt�@�C����
     */
    public String getFullPath(final String file) {
        return getFolder() + "/" + file;  // TAKAESU: �p�����[�^�͋�؂蕶�����t���Ă���ꍇ����Band ��؂蕶���� '/' �� '\' �ȂǁB
    }

    /**
     * �w�Z���ʃR�[�h�𓾂�B
     * @return �w�Z���ʃR�[�h
     */
    public String getSchoolDiv() {
        return _schoolDiv;
    }

    /**
     * �w�Z�R�[�h�𓾂�B
     * @return �w�Z�R�[�h
     */
    public String getSchoolCode() {
        return SCHOOL_CODE_HIGH;
    }
} // Param

// eof
