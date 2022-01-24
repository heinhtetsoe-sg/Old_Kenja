// kanji=����
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2009/03/18
 * �쐬��: maesiro
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import jp.co.alp.kenja.batch.otr.domain.KenjaDateImpl;
import jp.co.alp.kenja.batch.otr.domain.Period;
import jp.co.alp.kenja.batch.otr.domain.Semester;
import nao_package.db.DB2UDB;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �p�����[�^�B
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {

    /*pkg*/static final Log log = LogFactory.getLog(Param.class);

    /** SimpleDateFormat */
    static SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd");
    /** �v���p�e�B�[�t�@�C���� */
    private static final String PROPSNAME = "OtrRead.properties";

    private DB2UDB _db;

    private final String _dbUrl;

    /** �N���������� */
    private final int hour;
    /** �N����������(��) */
    private final int minute;
    /** �N���������t */
    private final Date today;

    /** ���Ԋ��f�[�^��ǂݍ��ލł��Â����t */
    private KenjaDateImpl _oldestDate;
    /** ���Ԋ��f�[�^��ǂݍ��ޓ��t */
    private KenjaDateImpl _date;
    /** ���Ԋ��f�[�^��ǂݍ��ލő�Z�� */
    private Period _period;

    /** �X�V�� */
    private String _update;

    /** �ΑӃt�@�C�� */
    private final File _kintaiFile;

    /** �w�� */
    private Semester _semester;

    /** �Z���f�[�^ */
    private Map _periods;

    /** �Z���^�C���e�[�u���f�[�^ */
    private Map _periodTimeTables;
    
    /** ����f�[�^�̍폜�t���O */
    private boolean _deletesOldDataInDB;

    /** �Αӂ�"�o��"�̃f�[�^�̏o�̓t���O */
    private boolean _outputSeatedToDB;

    /**
     * �R���X�g���N�^�B
     * @param args ����
     * @exception ParseException �p�[�X��O
     * @exception IOException ���o�͗�O
     */
    public Param(final String[] args) throws IOException, ParseException {
        if (args.length < 1) {
            log.error("Usage: java Main <//localhost:50000/dbname> ");
            throw new IllegalArgumentException("�����̐����Ⴄ");
        }

        // DB��URL��ǂݍ���
        _dbUrl = args[0];

        // �Z���^�C���e�[�u����ǂݍ���
        _periodTimeTables = PeriodTimeTable.load();

        // �f�o�b�O�p
        if (args.length >= 4 && "-debug".equals(args[1])) {
            log.debug("debug");
            today = java.sql.Date.valueOf(args[2]);
            final String[] hourMinute = StringUtils.split(args[3], ':');
            hour = Integer.parseInt(hourMinute[0]);
            minute = Integer.parseInt(hourMinute[1]);
        } else {
            today = new Date();
            final Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }
        // OTR���o�͂����ΑӃt�@�C�����Z�b�g����
        final Properties properties = new Properties();
        final File propertyFile = new File(PROPSNAME);
        log.debug("propertyFile path = " + propertyFile.getAbsolutePath());
        properties.load(new FileInputStream(propertyFile));
        final String kintaiFilePath = properties.getProperty("KintaiFilePath");
        log.debug("�ΑӃt�@�C�� path = " + kintaiFilePath);

        _kintaiFile = new File(kintaiFilePath);
        if (!_kintaiFile.isFile()) {
            throw new IllegalArgumentException("'" + _kintaiFile + "'�̓t�@�C���ł͂���܂���");
        }
        
        String deletesOldDataInDB = properties.getProperty("DeleteOldDataInDB");
        _deletesOldDataInDB = deletesOldDataInDB != null && "true".equals(deletesOldDataInDB.toLowerCase());
        
        String outputSeatedToDB = properties.getProperty("OutputSeatedToDB");
        _outputSeatedToDB = outputSeatedToDB != null && "true".equals(outputSeatedToDB.toLowerCase());
    }


    /**
     * DB��URL�𓾂�B
     * @return DBURL
     */
    public String getDbUrl() {
        return _dbUrl;
    }

    /**
     * �ΑӃt�@�C���𓾂�B
     * @return �ΑӃt�@�C��
     */
    public File getKintaiFile() {
        return _kintaiFile;
    }

    /**
     * �Â��f�[�^���폜���邩
     * @return �Â��f�[�^���폜����Ȃ�true�A�����łȂ����false
     */
    public boolean deletesOldDataInDB() {
        return _deletesOldDataInDB;
    }

    /**
     * �Αӂ�"�o��"�̃f�[�^�̏o�̓t���O
     * @return �Αӂ�"�o��"�̃f�[�^���o�͂���Ȃ�true�A�����łȂ����false
     */
    public boolean outputsSeatedToDB() {
        return _outputSeatedToDB;
    }

    /**
     * �K�v�ȏ������[�h
     * @param db DB
     * @throws IOException IO��O
     * @throws SQLException SQL��O
     */
    public void load(final DB2UDB db) throws IOException, SQLException {
        _periods = Period.load(db);

        final BatchTime time = BatchTime.create(hour, minute);
        // �v���O�����N�����̍Z������ɂ��Ă͔͈͂̊J�n���Ԃ��l�����Ȃ��B(���k�̍Z������Ƃ͋�ʂ���)
        final PeriodTimeTable tt = getPeriodTimeTable(time, false);
        if (tt == null) {
            throw new IllegalArgumentException("���Ԃ̎w�肪�Ԉ���Ă��܂��B" + time);
        }
        _period = getPeriod(tt).getPrevious();
        _update = sdf_.format(today);
        log.debug("�ΏۂƂ���N����:" + _update + " �Z��:" + (_period == null ? "0" : _period.getCode()));
        _date = KenjaDateImpl.getInstance(today);

        _semester = Semester.load(db, _update);
    }

    /**
     * (�w���}�X�^��)�N�x�𓾂�B
     * @return �N�x
     */
    public String getYear() {
        return _semester.getYear();
    }

    /**
     * (�w���}�X�^��)�w���𓾂�B
     * @return �w��
     */
    public String getSemester() {
        return _semester.getSemesterString();
    }

    /**
     * (�w���}�X�^��)�w���̊J�n���𓾂�B
     * @return �w���̊J�n��
     */
    public String getSemesterSdate() {
        return _semester.getSDate().toString();
    }

    /**
     * (�w���}�X�^��)�w���̏I�����𓾂�B
     * @return �w���̏I����
     */
    public String getSemesterEdate() {
        return _semester.getEDate().toString();
    }

    /**
     * ���Ԋ���ǂݍ��ޓ��t�𓾂�B
     * @return ���t
     */
    public KenjaDateImpl getTargetDate() {
        return KenjaDateImpl.getInstance(java.sql.Date.valueOf(_update));
    }
    
    /**
     * ���Ԋ���ǂݍ��ލł��Â����t�𓾂�
     * @return ���Ԋ���ǂݍ��ލł��Â����t
     */
    public KenjaDateImpl getOldestDate() {
        return _oldestDate;
    }

    /**
     * ���Ԋ���ǂݍ��ލł��Â����t���Z�b�g����
     */
    public void setOldestDate(final KenjaDateImpl oldestDate) {
        _oldestDate = oldestDate;
    }

    /**
     * ���Ԋ���ǂݍ��ލő�̍Z���𓾂�B
     * @return �Z��
     */
    public Period getTargetPeriod() {
        return _period;
    }

    /**
     * �Z���̃^�C���e�[�u���𓾂�B
     * @param period �Z��
     * @return �Z���̃^�C���e�[�u��
     */
    public PeriodTimeTable getPeriodTimeTable(final Period period) {
        return getPeriodTimeTable(period.getCode());
    }


    /**
     * �Z���R�[�h�̍Z���^�C���e�[�u���𓾂�B
     * @param cd �Z���R�[�h
     * @return �Z���R�[�h�̃^�C���e�[�u��
     */
    public PeriodTimeTable getPeriodTimeTable(final String cd) {
        return (PeriodTimeTable) _periodTimeTables.get(cd);
    }

    /**
     * �w�肳�ꂽ�����̍Z���^�C���e�[�u���𓾂�B
     * �L���Ȏ��Ԃ͈̔͂͊e���Ƃ̊J�n����10���O����I�������܂ŁB
     * �����w�肳��鎞�����Z�����ɓ��Ă͂܂�Ȃ��ꍇ�Anull��Ԃ��B
     * (��: 4�Z��=(11:35�J�n,12:25�I��)
     *      �ō�=11:35�ł͍Z����4�Z���A11:24��12:26�ł͍Z����null)
     * �ŏI�Z���ȍ~�̑ō���null��Ԃ�
     * @param time �w�肳��鎞��
     * @return �Z���^�C���e�[�u��
     */
    private PeriodTimeTable getPeriodTimeTable(final BatchTime time, final boolean doBeforeCheck) {
        final String[] cds = new String[_periodTimeTables.size()];
        int j = 0;
        for (final Iterator it = _periodTimeTables.keySet().iterator(); it.hasNext(); j++) {
            final String pcd = (String) it.next();
            cds[j] = pcd;
        }
        Arrays.sort(cds);
        for (int i = 0; i < cds.length; i++) {
            final PeriodTimeTable tt = getPeriodTimeTable(cds[i]);
            final BatchTime validBegin = tt.getBeginTime().add(0, -10); // �Z���̊J�n10���O�I�t�Z�b�g
            
            final boolean beforeCheck = doBeforeCheck ? validBegin.isBefore(time, true) : true;
            if (beforeCheck && time.isBefore(tt.getEndTime(), true)) {
                return tt;
            }
        }
        return (doBeforeCheck) ? null : PeriodTimeTable.ONE_DAY;
    }

    /**
     * �w�肳�ꂽ�����̍Z���^�C���e�[�u���𓾂�B(�Z���̊J�n���Ԃ��`�F�b�N����)
     * @param time �w�肳��鎞��
     * @return �Z���^�C���e�[�u��
     */
    public PeriodTimeTable getPeriodTimeTable(final BatchTime time) {
        return getPeriodTimeTable(time, true);
    }
    /**
     * �Z���^�C���e�[�u���̍Z���𓾂�B
     * @param tt �Z���^�C���e�[�u��
     * @return �Z��
     */
    public Period getPeriod(final PeriodTimeTable tt) {
        return (tt == null) ? null : getPeriod(tt.getPeriodCd());
    }

    /**
     * �Z���R�[�h�̍Z���𓾂�B
     * @param cd �Z���R�[�h
     * @return �Z��
     */
    public Period getPeriod(final String cd) {
        if (Period.LATEST_PERIOD_CODE.equals(cd)) return Period.LATEST_PERIOD;
        if (getPeriodTimeTable(cd)==null) return null;
        return (Period) _periods.get(cd);
    }
} // Param

// eof
