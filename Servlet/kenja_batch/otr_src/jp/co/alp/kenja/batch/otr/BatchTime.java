package jp.co.alp.kenja.batch.otr;

import java.text.DecimalFormat;

/**
 * ����
 * @version $Id1.0v$
 */
public final class BatchTime implements Comparable {

    private static final DecimalFormat TIME_FORMAT = new DecimalFormat("00");
    private final Integer _hour;
    private final Integer _minute;

    private BatchTime(final Integer hour, final Integer minute) {
        _hour = hour;
        _minute = minute;
    }

    /**
     * �����C���X�^���X���쐬����B
     * @param hour ~��
     * @param minute ~��
     * @return �����C���X�^���X
     * @throws IllegalArgumentException (0 <= hour < 24) ���� (0 <= minute < 60) �łȂ��Ƃ�
     */
    public static BatchTime create(final int hour, final int minute) throws IllegalArgumentException {
        if (hour < 0 || hour >= 24 || minute < 0 || minute >= 60) {
            throw new IllegalArgumentException();
        }
        return new BatchTime(new Integer(hour), new Integer(minute));
    }

    /**
     * �����Ɉ����̎��Ԃ𑫂���������Ԃ�
     * @param plusHour �v���X~����
     * @param plusMinute �v���X~��
     * @return �����Ɉ����̎��Ԃ𑫂�������
     */
    public BatchTime add(final int plusHour, final int plusMinute) {
        int hour = _hour.intValue() + plusHour;
        int minute = _minute.intValue() + plusMinute;

        if (minute < 0) {
            hour -= 1;
            minute += 60;
        } else if (minute >= 60) {
            hour += 1;
            minute -= 60;
        }
        return create(hour, minute);
    }

    /**
     * �w�莞�����O�̎��Ԃ�
     * @param batchTime �w�莞��
     * @param contains �w�莞�����܂ނȂ�true�A����ȊO�Ȃ�false
     * @return �w�莞�����O�̎��ԂȂ�true�A����ȊO�Ȃ�false
     */
    public boolean isBefore(final BatchTime batchTime, final boolean contains) {
        final int cmp = compareTo(batchTime);
        return (contains && cmp == 0) || cmp < 0;
    }

    /**
     * �w�莞������̎��Ԃ�
     * @param batchTime �w�莞��
     * @param contains �w�莞�����܂ނȂ�true�A����ȊO�Ȃ�false
     * @return �w�莞������̎��ԂȂ�true�A����ȊO�Ȃ�false
     */
    public boolean isAfter(final BatchTime batchTime, final boolean contains) {
        final int cmp = compareTo(batchTime);
        return (contains && cmp == 0) || cmp > 0;
    }

    /*
     * �����̔�r
     * hour�Ŕ�r����Bhour������Ȃ�minute�Ŕ�r����B
     *
     * TODO: ���̓���00:00�ƑO�̓���23:59�ł͌�҂��傫�����ƂɂȂ�(=���ƂɂȂ�)�̂�
     *       �O��Ƃ��ē��t�ƈꏏ�Ɏg�p����
     * TODO: ������Calendar�g���Ƃ�
     */
    /**
     * {@inheritDoc}
     */
    public int compareTo(final Object o) {
        if (!(o instanceof BatchTime)) {
            return 0;
        }
        final BatchTime otherTime = (BatchTime) o;

        int rtn = _hour.compareTo(otherTime._hour);
        if (rtn == 0) {
            rtn = _minute.compareTo(otherTime._minute);
        }
        return rtn;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return TIME_FORMAT.format(_hour) + ":" + TIME_FORMAT.format(_minute);
    }
}
