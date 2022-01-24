// kanji=����
/*
 * $Id: KenjaDateImpl.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2009/03/18 11:00:00 - JST
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
//TODO: �����[�X���͈ȉ��̃��C�u������jar�C���|�[�g��!!
import org.apache.commons.lang.time.DateUtils;


/**
 * ���҃p�b�P�[�W�̓��t�̎����B
 * @version $Id: KenjaDateImpl.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class KenjaDateImpl implements Comparable {
    private static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy/MM/dd z");

    static {
        SDF_YMD.setLenient(false);
    }

    private static final Map INSTANCES = new HashMap();

    protected final Calendar    _cal;

    protected final long        _millis;
    protected final long        _elapseDays;
    protected final int         _y;
    protected final int         _m;
    protected final int         _d;

    protected final String      _str;   //��F"2004-12-31(��)"
    private final int           _hash;
    private final java.sql.Date _sqlDate;

    /*
     * �R���X�g���N�^�B
     * @param timeInMillis �o�߃~���b
     */
    private KenjaDateImpl(final long timeInMillis) {
        _cal = calendar();
        _millis = round(timeInMillis);
        _elapseDays = (long) Math.floor(_millis / DateUtils.MILLIS_IN_DAY);
        _cal.setTime(new Date(_millis));

        _y = _cal.get(Calendar.YEAR);
        _m = _cal.get(Calendar.MONTH) + 1;
        _d = _cal.get(Calendar.DATE);

        _cal.clear();
        _cal.set(_y, _m - 1, _d, 0, 0, 0);

        _sqlDate = new java.sql.Date(_cal.getTime().getTime());

    string:
        {
            final StringBuffer sb = new StringBuffer(16);
            sb.append(_y).append('-');
            if (_m < 10) {
                sb.append('0');
            }
            sb.append(_m).append('-');
            if (_d < 10) {
                sb.append('0');
            }
            sb.append(_d);
            _str = sb.toString();
        }

        _hash = _str.hashCode();
    }

    /*
     */
    private static long round(final long timeInMillis) {
        return ((long) Math.floor(timeInMillis / DateUtils.MILLIS_IN_DAY)) * DateUtils.MILLIS_IN_DAY;
    }

    /**
     * ���t�̃C���X�^���X�𓾂�B
     * @param timeInMillis �o�߃~���b
     * @return ���t�̃C���X�^���X
     */
    public static KenjaDateImpl getInstance(final long timeInMillis) {
        final long millis = round(timeInMillis);
        final String key = String.valueOf(millis);
        KenjaDateImpl found = (KenjaDateImpl) INSTANCES.get(key);
        if (null == found) {
            found = new KenjaDateImpl(millis);
            INSTANCES.put(key, found);
        }
        return found;
    }

    /**
     * ���t�̃C���X�^���X�𓾂�B
     * @param cal �J�����_�[
     * @return ���t�̃C���X�^���X
     */
    public static KenjaDateImpl getInstance(final Calendar cal) {
        return getInstance(cal.get(Calendar.YEAR), 1 + cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * ���t�̃C���X�^���X�𓾂�B
     * @param year ����N
     * @param month ��(1...12)
     * @param dayOfMonth ���̓�(1...28,29,30,31)
     * @return ���t�̃C���X�^���X
     */
    public static KenjaDateImpl getInstance(
            final int year,
            final int month,
            final int dayOfMonth
    ) {
        final Calendar cal = calendar(year, month, dayOfMonth);
        return getInstance(
                cal.getTime().getTime()
        );
    }

    /*
     * Calendar�̃C���X�^���X�𓾂�B
     * @return Calendar�̃C���X�^���X
     */
    private static Calendar calendar() {
        final Calendar cal = Calendar.getInstance(DateUtils.UTC_TIME_ZONE);
        cal.setFirstDayOfWeek(Calendar.MONDAY); // �T�̎n�܂�́u���j���v
        cal.clear();
        cal.setLenient(false);
        return cal;
    }

    /**
     * �N��������Calendar�̃C���X�^���X�𓾂�B
     * @param y ����N
     * @param m ��(1...12)
     * @param d ���̓�(1...28,29,30,31)
     * @return Calendar�̃C���X�^���X
     */
    public static Calendar calendar(
            final int y,
            final int m,
            final int d
    ) {
        final Calendar cal = calendar();
        cal.set(y, m - 1, d, 0, 0, 0);
        return cal;
    }

    /**
     * ���t�̃C���X�^���X�𓾂�B
     * @param date ���t�Bjava.util.Date�܂���java.sql.Date�̂ǂ���ł��B
     * @return ���t�̃C���X�^���X
     */
    public static KenjaDateImpl getInstance(final Date date) {
        return getInstance(dateTime(date));
    }

    private static long dateTime(final Date date) {
        if (null == date) {
            throw new IllegalArgumentException("�������s��");
        }
        if (date instanceof java.sql.Date) {
            return date.getTime() + 9L * DateUtils.MILLIS_IN_HOUR;
        } else {
            return date.getTime();
        }
    }

    /**
     * ������ɕϊ�����B
     * @return ������
     */
    public String toString() { return _str; }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object obj) {
        if (obj == this) { return true; }
        if (!(obj instanceof KenjaDateImpl)) { return false; }

        final KenjaDateImpl that = (KenjaDateImpl) obj;
        return _cal.equals(that._cal);
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Object o) {
        if (!(o instanceof KenjaDateImpl)) {
            return -1;
        }

        final KenjaDateImpl that = (KenjaDateImpl) o;

        int rtn = 0;
    check:
        {
            rtn = _y - that._y;
            if (0 != rtn) { break check; }

            rtn = _m - that._m;
            if (0 != rtn) { break check; }

            rtn = _d - that._d;
            if (0 != rtn) { break check; }
        } // check:

        return rtn;
    }

    /**
     * ���̓��t�̃n�b�V���R�[�h��Ԃ��܂��B
     * {@inheritDoc}
     */
    public int hashCode() {
        return _hash;
    }

    /**
     * ����N�𓾂�B
     * @return ����N
     */
    public int getYear() { return _y; }

    /**
     * ���𓾂�B
     * @return ��(1...12)
     */
    public int getMonth() { return _m; }

    /**
     * ���𓾂�B
     * @return ��(1...28,29,30,31)
     */
    public int getDay() { return _d; }

    /**
     * SQL-Date�𓾂�B
     * @return SQLDate�̃C���X�^���X
     */
    public java.sql.Date getSQLDate() {
        return _sqlDate;
    }

    /**
     * 1970 �N 1 �� 1 �� 00:00:00 GMT ����̃~���b���𓾂�B
     * @return 1970 �N 1 �� 1 �� 00:00:00 GMT ����̃~���b��
     * @see java.util.Calendar#getTimeInMillis()
     */
    public long getTimeInMillis() { return _millis; }

    /**
     * ���݂̓��t�� <code>days</code>�������������t��\���C���X�^���X��Ԃ��B
     * ���A<code>days</code> ���[���Ȃ�A���̃C���X�^���X���g��Ԃ��B
     * <code>days</code>�����Ȃ�A�������������t��\���C���X�^���X��Ԃ��B
     * @param days ����������i���̏ꍇ�͌���������j
     * @return ���݂̓��t�� <code>days</code>�������������t��\���C���X�^���X
     */
    public KenjaDateImpl add(final int days) {
        if (0 == days) {
            return this;
        }
        return getInstance(this.getTimeInMillis() + days * DateUtils.MILLIS_IN_DAY);
    }

    /**
     * �傫��(����)���̓��t��Ԃ��B
     * @param that ������̓��t
     * @return �傫��(����)���̓��t
     */
    public KenjaDateImpl max(final KenjaDateImpl that) {
        if (null == that) {
            return this;
        }
        if (this.compareTo(that) < 0) {
            return that;
        } else {
            return this;
        }
    }
} // KenjaDateImpl

// eof
