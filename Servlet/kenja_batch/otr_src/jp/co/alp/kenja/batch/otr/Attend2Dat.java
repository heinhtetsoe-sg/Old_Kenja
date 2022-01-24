package jp.co.alp.kenja.batch.otr;

import jp.co.alp.kenja.batch.otr.domain.KenjaDateImpl;
import jp.co.alp.kenja.batch.otr.domain.Kintai;
import jp.co.alp.kenja.batch.otr.domain.Schedule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ���o���f�[�^
 * @author maesiro
 * @version $Id: Attend2Dat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Attend2Dat {
    private static final Log log = LogFactory.getLog(Attend2Dat.class);
    
    /** �o���f�[�^�^�C�v �J�[�h���� */
    private static final int ATTEND_TYPE_INSERTED = 0;
    /** �o���f�[�^�^�C�v �J�[�h����(����) */
    private static final int ATTEND_TYPE_INSERTED_INVALID = 1;
    /** �o���f�[�^�^�C�v �J�[�h���͖� */
    private static final int ATTEND_TYPE_NOT_INSERTED = 2;
    
    private final String _schregno;
    private final KenjaDateImpl _date;
    private final BatchTime _time;
    private final Schedule _schedule;
    private final String _year;
    private String _registerCd;
    private Kintai _kintai;
    
    /** �o���f�[�^�^�C�v */
    private final int _attendType;

    /**
     * �R���X�g���N�^
     * @param data OTR��荞�݃f�[�^
     * @param schedule �Z��
     * @param year �N�x
     */
    public Attend2Dat(
            final RoomEnterExitData data,
            final Schedule schedule,
            final String year) {
        this(data.getSchregno(), data.getDate(), data.getTime(), schedule, Kintai.getDefault(), year,
                isInvalid(data, schedule) ? ATTEND_TYPE_INSERTED_INVALID : ATTEND_TYPE_INSERTED);
    }

    /**
     * �R���X�g���N�^(�J�[�h���͖�)
     * @param schregno �ݐДԍ�
     * @param date ���t
     * @param time ����
     * @param schedule �Z��
     * @param year �N�x
     */
    public Attend2Dat(
            final String schregno,
            final KenjaDateImpl date,
            final BatchTime time,
            final Schedule schedule,
            final String year) {
        this(schregno, date, time, schedule, Kintai.getDefault(), year,
                ATTEND_TYPE_NOT_INSERTED);
    }

    /**
     * �R���X�g���N�^
     * @param schregno �ݐДԍ�
     * @param date ���t
     * @param time ����
     * @param schedule �Z��
     * @param year �N�x
     */
    private Attend2Dat(
            final String schregno,
            final KenjaDateImpl date,
            final BatchTime time,
            final Schedule schedule,
            final Kintai kintai,
            final String year,
            final int attendType) {
        _schregno = schregno;
        _date = date;
        _time = time;
        _schedule = schedule;
        _year = year;
        _kintai = kintai;
        _attendType = attendType;
    }

    /**
     * �����𓾂�
     * @return ����
     */
    public KenjaDateImpl getDate() {
        return _date;
    }

    /**
     * �Αӂ𓾂�
     * @return �Α�
     */
    public Kintai getKintai() {
        return _kintai;
    }

    /**
     * �Z���𓾂�
     * @return �Z��
     */
    public Schedule getSchedule() {
        return _schedule;
    }

    /**
     * �o�^�R�[�h�𓾂�
     * @return �o�^�R�[�h
     */
    public String getRegisterCd() {
        return _registerCd;
    }

    /**
     * �ݐДԍ��𓾂�
     * @return �ݐДԍ�
     */
    public String getSchregno() {
        return _schregno;
    }

    /**
     * �N�x�𓾂�
     * @return �N�x
     */
    public String getYear() {
        return _year;
    }

    /**
     * �Αӂ��o�Ȃɂ���
     */
    public void setKintaiSeated() {
        _kintai = Kintai.getSeated();
    }

    /**
     * ���Ԋ�����Αӂ��Z�b�g����
     * @param schedule ���Ԋ�
     * @param param �p�����[�^
     */
    public void setKintai(final Schedule schedule, final Param param) {
        Kintai kintai = schedule.getKintai(_time, param); 

        if (kintai == null) {
            log.debug(" !!! �Αӂ��擾�ł��܂���ł����B["+Kintai.getDefault()+"]���Z�b�g���܂��B[" + this + "] !!!");
            kintai = Kintai.getDefault();
        }
        
        _kintai = kintai;
    }

    /**
     * �o�^�R�[�h���Z�b�g����
     * @param cd �o�^�R�[�h
     */
    public void setRegisterCd(final String cd) {
        _registerCd = cd;
    }

    /**
     * DB�ɃC���T�[�g����SQL��Ԃ�
     * @return DB�ɃC���T�[�g����SQL
     */
    public String getInsertSql() {
        final String sql = 
            "INSERT INTO ATTEND2_DAT (SCHREGNO, ATTENDDATE, PERIODCD, CHAIRCD, DI_CD, YEAR, REGISTERCD, UPDATED) values (" +
            "'" + getSchregno() + "'," + 
            "'" + getDate() + "'," + 
            "'" + _schedule.getPeriod().getCode() + "'," + 
            "'" + _schedule.getChair().getChairCd() + "'," + 
            "'" + _kintai.getResultCode() + "'," + 
            "'" + getYear() + "'," + 
            "'" + getRegisterCd() + "'," + 
            "current timestamp)" ; 
        return sql;
    }

    /**
     * DB����폜����SQL��Ԃ�
     * @return DB����폜����SQL
     */
    public String getDeleteSql() {
        final String sql = 
            "DELETE FROM ATTEND2_DAT WHERE " +
            " SCHREGNO = '" + getSchregno() + "' " + 
            " AND ATTENDDATE = '" + getDate() + "' " + 
            " AND PERIODCD = '" + _schedule.getPeriod().getCode() + "' ";
        return sql;
    }

    /**
     * DB�ɂ��̃f�[�^�����łɂ��邩��������SQL��Ԃ�
     * @return DB�ɂ��̃f�[�^�����łɂ��邩��������SQL
     */
    public String getSelectSql() {
        final String sql = 
            "SELECT SCHREGNO, ATTENDDATE, PERIODCD, CHAIRCD, DI_CD, UPDATED FROM ATTEND2_DAT WHERE " +
            " SCHREGNO = '" + getSchregno() + "' " + 
            " AND ATTENDDATE = '" + getDate() + "' " + 
            " AND PERIODCD = '" + _schedule.getPeriod().getCode() + "' ";
        return sql;
    }

    /**
     * OTR��荞�݃f�[�^��������
     * @param data OTR��荞�݃f�[�^
     * @param schedule ���肳�ꂽ���Ԋ�
     * @return OTR��荞�݃f�[�^�������Ȃ�true�A�����łȂ����false
     */
    private static boolean isInvalid(final RoomEnterExitData data, final Schedule schedule) {
        
        if(!schedule.facilitiesContainGateNo(data.getGateno())) {
            log.debug("�����ȃQ�[�gNo.�ł��B�w�Дԍ�=" + data.getSchregno() + " �Q�[�gNo.=" + data.getGateno() + "(���Ԋ�=" + schedule + ")");
            return true;
        }
        return false;
    }

    /**
     * OTR��荞�݌��f�[�^��������
     * @return �����Ȃ�true�A�����łȂ����false
     */
    public boolean isInvalid() {
        return _attendType == ATTEND_TYPE_INSERTED_INVALID;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return getSchregno() + " , "
            + getDate() + " , "
            + _schedule.getPeriod() + " , "
            + _schedule.getChair() + " , "
            + _kintai + " , "
            + getYear();
    }

}
