// kanji=����
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/03/22 10:21:37 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.Curriculum;

/**
 * �p�����[�^�B
 * @author takaesu
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {
    /*pkg*/static final Log log = LogFactory.getLog(Param.class);

    /** CSV�t�@�C���̕����R�[�h */
    public static final String encode = "MS932";

    private DB2UDB _db;

    private final String _dbUrl;
    private final String _date;
    private final boolean _isOutputMode;
    private final String _file;
    private final String _schoolDiv;
    private final String _update;

    /** �N�x�B(�w���}�X�^) */
    protected String _year;
    /** �w���B(�w���}�X�^) */
    protected String _semester;

    /**
     * �R���X�g���N�^�B
     * @param args
     */
    public Param(final String[] args) {
        if (5 != args.length) {
            System.err.println("Usage: java Main <//localhost:50000/witestdb> <yyyy-mm-dd> <-out|-in> <outputFolder|inputFile> <GakkoKubun>");
            throw new IllegalArgumentException("�����̐����Ⴄ");
        }

        _dbUrl = args[0];
        _date = args[1];
        final String option = args[2].toLowerCase();
        _file = args[3];

        if (!"-out".equals(option) && !"-in".equals(option)) {
            throw new IllegalArgumentException("��3������ -in or -out");
        }
        _isOutputMode = "-out".equals(option) ? true : false;
        if (_isOutputMode) {
            final File file = new File(_file);
            if (!file.isDirectory()) {
                throw new IllegalArgumentException("'" + _file + "'�̓f�B���N�g���ł͂Ȃ�");
            }
        }

        _schoolDiv = args[4];

        _update = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        log.debug("�X�V��=" + _update);
    }

    public String getDbUrl() {
        return _dbUrl;
    }

    public String getDate() {
        return _date;
    }

    public String getFile() {
        return _file;
    }

    public boolean isOutputMode() {
        return _isOutputMode;
    }

    public void load(final DB2UDB db) throws SQLException {
        loadSemesterMst(db);
        Curriculum.loadCurriculumMst(db);
    }

    private void loadSemesterMst(final DB2UDB db) throws SQLException {
        try {
            db.query("SELECT year, semester FROM semester_mst WHERE '" + _date + "' BETWEEN sdate AND edate");
            ResultSet rs = db.getResultSet();
            if (rs.next()) {
                _year = rs.getString("year");
                _semester = rs.getString("semester");
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
     * �w�Z�敪�𓾂�B
     * @return �w�Z�敪
     */
    public String getSchoolDiv() {
        return _schoolDiv;
    }

    /**
     * �t���p�X�̃t�@�C�����𓾂�B
     * @param file �t�@�C����
     * @return �t���p�X�ȃt�@�C����
     */
    public String getFullPath(final String file) {
        return getFile() + "/" + file;  // TAKAESU: �p�����[�^�͋�؂蕶�����t���Ă���ꍇ����Band ��؂蕶���� '/' �� '\' �ȂǁB
    }
} // Param

// eof
