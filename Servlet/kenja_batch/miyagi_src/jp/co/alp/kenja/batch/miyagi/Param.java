// kanji=����
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/30 14:23:11 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.miyagi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �p�����[�^�B
 * 
 * @author takaesu
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {
    /* pkg */static final Log log = LogFactory.getLog(Param.class);

    final String _knjUrl;

    final String _knjUser;

    final String _knjPass;

    final String _vqsUrl;

    final String _vqsUser;

    final String _vqsPass;

    final String _iinkaiUrl;

    final String _iinkaiUser;

    final String _iinkaiPass;

    /** ���. */
    final String _date;

    /** �X�V����. */
    private final Date _now = new Date();

    /** �N�x�B(�w���}�X�^) */
    protected String _year;

    /** �w���B(�w���}�X�^) */
    protected String _semester;

    /** �w�Z�R�[�h�B(�w�Z�ڍ׃f�[�^�O�O�W) */
    protected String _schoolcd;

    /** �ے��R�[�h�B(�w�Z�ڍ׃f�[�^�O�O�W) */
    protected String _course_cd;

    public Param(final String[] args) {
        if (10 != args.length) {
            System.err.println("Usage: java Main <//db2Host:50000/db2DB> <db2 user> <db2 passwd> <//postgreHost/postgreDB> <postgre user> <postgre passwd> <//iinkaiHost:50000/iinkaiDB> <iinkai user> <iinkai passwd> <yyyy-mm-dd>");
            throw new IllegalArgumentException("�����̐����Ⴄ");
        }
        _knjUrl = args[0];
        _knjUser = args[1];
        _knjPass = args[2];
        _vqsUrl = args[3];
        _vqsUser = args[4];
        _vqsPass = args[5];
        _iinkaiUrl = args[6];
        _iinkaiUser = args[7];
        _iinkaiPass = args[8];
        _date = args[9];
    }

    public void load(final Database db) throws SQLException {
        loadSemesterMst(db);
        loadSchoolDetailDat(db);
    }

    private void loadSemesterMst(final Database db) throws SQLException {
        try {
            db.query("SELECT year, semester FROM semester_mst WHERE '" + _date + "' BETWEEN sdate AND edate AND semester<>'9'");
            ResultSet rs = db.getResultSet();
            if (rs.next()) {
                _year = rs.getString("year");
                _semester = rs.getString("semester");
            }
            db.commit();
            rs.close();

            // �w���͈͊O�̏ꍇ�A���߂̊w�����Z�b�g
            if (_semester == null) {
                final String[] split = StringUtils.split(_date, "-");
                final String year = Integer.parseInt(split[1]) < 4 ? String.valueOf(Integer.parseInt(split[0]) - 1) : split[0];
                db.query("SELECT year, semester, sdate FROM semester_mst WHERE year = '" + year + "' AND semester<>'9' order by semester");
                ResultSet rs2 = db.getResultSet();
                while (rs2.next()) {
                    _year = rs2.getString("year");
                    _semester = rs2.getString("semester");
                    if (_date.compareTo(rs2.getString("sdate")) < 0) {
                        break;
                    }
                }
                db.commit();
                rs2.close();
                log.debug(_date + "���w���͈͊O�̂��߁A���߂̊w�����Z�b�g�B�N�x=" + _year + ", �w��=" + _semester);
            }
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

    private void loadSchoolDetailDat(final Database db) throws SQLException {
        try {
            final String sql = getDataSql();
            db.query(sql);
            ResultSet rs = db.getResultSet();
            if (rs.next()) {
                _schoolcd = rs.getString("SCHOOLCD");
                _course_cd = rs.getString("COURSE_CD");
            }
            db.commit();
            rs.close();
        } catch (final SQLException e) {
            log.fatal("�w�Z�R�[�h�A�ے��R�[�h�������Ȃ�! SCHOOL_DETAIL_DAT����擾�o���Ȃ�");
            throw e;
        }
        try {
            Integer.parseInt(_schoolcd);
            Integer.parseInt(_course_cd);
        } catch (final NumberFormatException e) {
            log.fatal("�w�Z�R�[�h�A�ے��R�[�h���������Ȃ�(SCHOOL_DETAIL_DAT����擾�o���Ȃ�)�ˊw�Z�R�[�h=" + _schoolcd + ", �ے��R�[�h=" + _course_cd);
            throw e;
        }
    }

    private String getDataSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.SCHOOL_REMARK1 AS SCHOOLCD, ");
        stb.append("     T1.SCHOOL_REMARK3 AS COURSE_CD ");
        stb.append(" FROM ");
        stb.append("     SCHOOL_DETAIL_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("         T1.YEAR        = '" + _year + "' ");
        stb.append("     AND T1.SCHOOL_SEQ  = '008' ");
        // NOT NULL�t�B�[���h����
        // stb.append("     AND T1.SCHOOL_REMARK1 IS NOT NULL ");
        // stb.append("     AND T1.SCHOOL_REMARK3 IS NOT NULL ");

        return stb.toString();
    }

    /**
     * �N�x�𓾂�B (�w���}�X�^)
     * 
     * @return �N�x
     */
    public String getYear() {
        return _year;
    }

    /**
     * �w���𓾂�B (�w���}�X�^)
     * 
     * @return �w��
     */
    public String getSemester() {
        return _semester;
    }

    /**
     * �X�V�����𓾂�B
     * 
     * @return �X�V����
     */
    public Date getUpdate() {
        return _now;
    }

    /** �e�e�[�u�����ʃt�B�[���h�����邩 */
    public boolean isNotNullCommonField() {
        return _schoolcd != null && _course_cd != null;
    }

    public String toString() {
        final String update = new SimpleDateFormat("yyyy-MM-dd H:m:s").format(_now);
        return "���=" + _date + ", �N�x=" + _year + ", �w��=" + _semester + ", ���s����=" + update + ", �w�Z�R�[�h=" + _schoolcd + ", �ے��R�[�h=" + _course_cd;
    }
} // Param

// eof
