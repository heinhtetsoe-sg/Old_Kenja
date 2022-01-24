// kanji=����
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/30 14:23:11 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �p�����[�^�B
 * @author takaesu
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {
    /*pkg*/static final Log log = LogFactory.getLog(Param.class);

    final String _knjUrl;
    final String _vqsUrl;
    final String _vqsUser;
    final String _vqsPass;
    /** ���. */
    final String _date;
    /** �X�V����. */
    private Date _now = new Date();

    /** �N�x�B(�w���}�X�^) */
    protected String _year;
    /** �w���B(�w���}�X�^) */
    protected String _semester;

    public Param(final String[] args) {
        if (5 != args.length) {
            System.err.println("Usage: java Main <//localhost:50000/witestdb> <//postgreHost/vqsDB> <postgre user> <postgre passwd> <yyyy-mm-dd>");
            throw new IllegalArgumentException("�����̐����Ⴄ");
        }
        _knjUrl = args[0];
        _vqsUrl = args[1];
        _vqsUser = args[2];
        _vqsPass = args[3];
        _date = args[4];
    }

    public void load(final Database db) throws SQLException {
        loadSemesterMst(db);
    }

    private void loadSemesterMst(final Database db) throws SQLException {
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
     * �X�V�����𓾂�B
     * @return �X�V����
     */
    public Date getUpdate() {
        return _now;
    }

    public String toString() {
        final String update = new SimpleDateFormat("yyyy-MM-dd H:m:s").format(_now);
        return "���=" + _date + ", �N�x=" + _year + ", �w��=" + _semester + ", ���s����=" + update;
    }
} // Param

// eof
