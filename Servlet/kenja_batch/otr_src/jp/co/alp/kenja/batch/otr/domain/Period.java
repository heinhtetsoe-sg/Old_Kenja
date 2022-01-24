// kanji=����
/*
 * $Id: Period.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2004/06/03 16:19:24 - JST
 * �쐬��: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �Z���B
 * @version $Id: Period.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class Period implements Comparable {
    /** log */
    private static final Log log = LogFactory.getLog(Period.class);
    private static final Class MYCLASS = Period.class;
    public static final String LATEST_PERIOD_CODE = "Z";
    public static final Period LATEST_PERIOD = Period.create(LATEST_PERIOD_CODE, "�S��");

    private static final int RADIX = 36;
    private static final int LIMIT_MIN = 0;
    private static final int LIMIT_MAX = 35;

    private final String _code;
    private final String _name;

    // �O�̍Z��
    private Period _previous;
    // ���̍Z��
    private Period _next;

    /*
     * �R���X�g���N�^�B
     */
    private Period(
            final String code,
            final String name
    ) {
        _code = code;
        _name = name;
    }


    /**
     * �Z���R�[�h�𓾂�B
     * @return �Z���R�[�h
     */
    public String getCode() {
        return _code;
    }

    /**
     * �Z�����̂𓾂�B
     * @return �Z������
     */
    public String getName() {
        return _name;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "�Z���R�[�h=[" + getCode() + "]�A�Z������=[" + getName() + "]";
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Object obj) {
        final Period that = (Period) obj;
        return getCode().compareTo(that.getCode());
    }

    /**
     * �Ȃ���΁A�Z���̃C���X�^���X���쐬����B
     * ���łɓ����Z���R�[�h�̃C���X�^���X������΁A�����̃C���X�^���X��Ԃ��B
     * @param code �Z���R�[�h("0","1","2"..."9","A","B"..."Z")
     * @param name �Z������
     * @return �Z���̃C���X�^���X
     */
    public static Period create(
            final String code,
            final String name
    ) {
        final int codeint = Integer.parseInt(code, RADIX);
        if (codeint < LIMIT_MIN || LIMIT_MAX < codeint) { throw new IllegalArgumentException("�������s��(code)"); }

        if (null == name)      { throw new IllegalArgumentException("�������s��(name)"); }

        return new Period(code, name);
    }

    /**
     * �Z�������[�h����
     * @param db DB
     * @return periodMap �Z���}�b�v
     * @exception SQLException SQL��O
     */
    public static Map load(final DB2UDB db) throws SQLException {

        final Map periodMap = new TreeMap();

        final String sql = " SELECT "
            + "  NAMECD2 AS CODE, "
            + "  NAME1 AS NAME "
            + " FROM NAME_MST "
            + " WHERE "
            + "  NAMECD1 = 'B001' ";

        db.query(sql);
        final ResultSet rs = db.getResultSet();
        log.debug("�Z���ǂݍ��݊J�n�B");
        while (rs.next()) {
            final String code = rs.getString("CODE");
            final String name = rs.getString("NAME");

            final Period period = Period.create(code, name);
            periodMap.put(period.getCode(), period);
            log.debug(period);
        }
        log.debug("�Z���ǂݍ��ݏI���B");

        // �O��̍u���̐ݒ�
        Period mappedLatestPeriod = null; 
        for (final Iterator it = periodMap.keySet().iterator(); it.hasNext();) {
            final String cdA = (String) it.next();
            final Period periodA = (Period) periodMap.get(cdA);
            final int pAcd = Integer.parseInt(periodA.getCode(), RADIX);
            if (mappedLatestPeriod == null || mappedLatestPeriod.isBefore(periodA)) { mappedLatestPeriod = periodA; }

            for (final Iterator it2 = periodMap.keySet().iterator(); it2.hasNext();) {
                final String cdB = (String) it2.next();
                final Period periodB = (Period) periodMap.get(cdB);
                final int pBcd = Integer.parseInt(periodB.getCode(), RADIX);

                if (pAcd == pBcd - 1) {
                    periodA.setNext(periodB);
                } else if (pAcd == pBcd + 1) {
                    periodA.setPrevious(periodB);
                }
            }
        }
        // �v���p�e�B�[�t�@�C�����̍Ō�̍Z����LATEST_PERIOD�̑O�̍Z���ɃZ�b�g����B
        LATEST_PERIOD.setPrevious(mappedLatestPeriod);

//        for (final Iterator it = periodMap.keySet().iterator(); it.hasNext();) {
//            final String cdA = (String) it.next();
//            final Period periodA = (Period) periodMap.get(cdA);
//            log.debug(periodA + "�ߖT�̍Z�� => " + periodA.getPrevious() + " , " + periodA.getNext());
//        }

        return periodMap;
    }

    /**
     * ���̍Z���𓾂�
     * @return ���̍Z��
     */
    public Period getNext() {
        return _next;
    }

    /**
     * ���̍Z�����Z�b�g����
     * @param next ���̍Z��
     */
    public void setNext(final Period next) {
        _next = next;
    }

    /**
     * �O�̍Z���𓾂�
     * @return �O�̍Z��
     */
    public Period getPrevious() {
        return _previous;
    }

    /**
     * �O�̍Z�����Z�b�g����
     * @param previous �O�̍Z��
     */
    public void setPrevious(final Period previous) {
        _previous = previous;
    }

    /**
     * �w��̍Z���͌ォ
     * @param period �Z��
     * @return �w��̍Z������Ȃ�true�A�����łȂ����false
     */
    public boolean isAfter(final Period period) {
        return Integer.valueOf(getCode(), RADIX).compareTo(Integer.valueOf(period.getCode(), RADIX)) > 0;
    }

    /**
     * �w��̍Z���͐悩
     * @param period �Z��
     * @return �w��̍Z������Ȃ�true�A�����łȂ����false
     */
    public boolean isBefore(final Period period) {
        return Integer.valueOf(getCode(), RADIX).compareTo(Integer.valueOf(period.getCode(), RADIX)) < 0;
    }

} // Period

// eof
