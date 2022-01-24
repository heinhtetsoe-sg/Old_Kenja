package jp.co.alp.kenja.batch.otr;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import jp.co.alp.kenja.batch.otr.domain.Period;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �Z���̃^�C���e�[�u��
 * @version $Id: PeriodTimeTable.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class PeriodTimeTable {

    private static Log log = LogFactory.getLog(PeriodTimeTable.class);
    private static final String PROPSNAME = "PeriodTimeTable.properties";
    public static final PeriodTimeTable ONE_DAY =
        new PeriodTimeTable(Period.LATEST_PERIOD_CODE , BatchTime.create(0,0), BatchTime.create(23,59));

    /** �Z���R�[�h */
    private final String _code;
    /** �J�n���� */
    private final BatchTime _beginTime;
    /** �I������ */
    private final BatchTime _endTime;    

    /**
     * �R���X�g���N�^
     * @param code �Z���R�[�h
     * @param beginTime �J�n����
     * @param endTime �I������
     */
    private PeriodTimeTable(final String code, final BatchTime beginTime, final BatchTime endTime) {
        _code = code;
        _beginTime = beginTime;
        _endTime = endTime;
    }

    /**
     * �Z���A�J�n�����A�I�������̕����񂩂�Z���^�C���e�[�u�����쐬����
     * @param cd �Z���R�[�h
     * @param beginStr �J�n�����̕�����
     * @param endStr   �I�������̕�����
     * @return �Z���^�C���e�[�u��
     * @throws IOException
     */
    private static PeriodTimeTable create(final String cd, final String beginStr, final String endStr) throws IOException {
        final BatchTime begin = getBatchTime(beginStr);
        final BatchTime end = getBatchTime(endStr);
        return new PeriodTimeTable(cd, begin, end);
    }

    /**
     * ������Ԃ�
     * @param timeStr �����t�H�[�}�b�g������
     * @return ����
     */
    private static BatchTime getBatchTime(final String timeStr) {
        final String[] time = StringUtils.split(timeStr, ":");
        final int hour = Integer.valueOf(time[0].trim()).intValue();
        final int minute = Integer.valueOf(time[1].trim()).intValue();
        return BatchTime.create(hour, minute);
    }

    /**
     * �v���p�e�B�[����Z���^�C���e�[�u����ǂݍ���
     * @throws IOException �t�@�C���ǂݍ��ݗ�O
     * @return �Z���^�C���e�[�u���}�b�v
     */
    public static Map load() throws IOException {

        final Properties props = new Properties();
        props.load(new FileInputStream(PROPSNAME));

        final Map periodTimeTables = new TreeMap();

        for (final Iterator it = props.keySet().iterator(); it.hasNext();) {
            final String cd = (String) it.next();
            final String times = (String) props.get(cd.toString());
            final String[] timesStr = StringUtils.split(times, ",");
            if (timesStr.length != 2) {
                throw new IOException("�v���p�e�B�[�t�@�C���̃t�H�[�}�b�g������������܂���B" + times);
            }
            final PeriodTimeTable tt = PeriodTimeTable.create(cd, timesStr[0], timesStr[1]);
            log.debug(" PeriodTimeTable.properties [" + cd + " , " + tt + "]");
            periodTimeTables.put(cd, tt);
        }
        return periodTimeTables;
    }

    /**
     * �J�n������Ԃ�
     * @return �J�n����
     */
    public BatchTime getBeginTime() {
        return _beginTime;
    }

    /**
     * �I��������Ԃ�
     * @return �I������
     */
    public BatchTime getEndTime() {
        return _endTime;
    }

    /**
     * �Z���R�[�h��Ԃ�
     * @return �Z���R�[�h
     */
    public String getPeriodCd() {
        return _code;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return " �Z���^�C���e�[�u�� (�Z��=" + _code + " �J�n����=" + _beginTime + " �I������=" + _endTime + ")";
    }
}
