// kanji=����
/*
 * $Id: Schedule.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/24 14:43:39 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jp.co.alp.kenja.batch.otr.Attend2Dat;
import jp.co.alp.kenja.batch.otr.BatchTime;
import jp.co.alp.kenja.batch.otr.Param;
import jp.co.alp.kenja.batch.otr.PeriodTimeTable;
import jp.co.alp.kenja.batch.otr.RoomEnterExitData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ���Ԋ��B
 * @version $Id: Schedule.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Schedule {
    
    private static final Log log = LogFactory.getLog(Schedule.class);
    
    /** ���{���t */
    private final KenjaDateImpl _date;
    /** �Z�� */
    private final Period _period;
    /** �u���R�[�h */
    private final String _chairCd;
    /** ���{�t���O */
    private final boolean _executed;
    /** �{�� */
    private final List _facilities;
    /** �u�� */
    private Chair _chair;
    /** �搶�̑ō��f�[�^ (�����̏ꍇ�A��葁�������̃f�[�^) */
    private RoomEnterExitData _teacherData;
    /** ���̍u���̊w���̏o���f�[�^�̃��X�g */
    private List _attendanceList;

    /** �A�����Ƃ̎��Ԋ� */
    private Schedule _continuedSchedule;

    /**
     * ���Ԋ�
     * @param date ��
     * @param period �Z��
     * @param chairCd �u���R�[�h
     * @param executed ���s���ꂽ��
     */
    public Schedule(
            final KenjaDateImpl date, 
            final Period period, 
            final String chairCd, 
            final boolean executed) {
        _date = date;
        _period = period;
        _chairCd = chairCd;
        _executed = executed;
        _facilities = new ArrayList();
        _attendanceList = new LinkedList();
    }

    /**
     * ���𓾂�
     * @return ��
     */
    public KenjaDateImpl getDate() { return _date; }

    /**
     * �Z���𓾂�
     * @return �Z��
     */
    public Period getPeriod() { return _period; }

    /**
     * ���s���ꂽ��
     * @return ���s���ꂽ�Ȃ�true�A�����łȂ����false
     */
    public boolean isExecuted() { return _executed; }

    /**
     * �{�݂�ǉ�����
     */
    public void addFacility(final Facility fac) {
        _facilities.add(fac);
    }

    /**
     * �{�݂𓾂�
     * @return �{��
     */
    private List getFacilities() {
        // �{�݂��o�^����Ă��Ȃ���΁A�u���̎{�݂��擾����
        if (_facilities.size() == 0) return getChair().getFacilities();
        return _facilities;
    }

    /**
     * �{�݂��Q�[�gNo. ���܂�ł��邩
     * @param data 
     * @return �{�݂��Q�[�gNo. ���܂�ł���Ȃ�true�A�����łȂ����false
     */
    public boolean facilitiesContainGateNo(final String gateNo) {
        for (final Iterator it = getFacilities().iterator(); it.hasNext();) {
            final Facility fac = (Facility) it.next();
            if (fac.contain(gateNo)) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * �u���𓾂�
     * @return �u��
     */
    public Chair getChair() { return _chair; }

    /**
     * �u�����Z�b�g����
     * @param chair �u��
     */
    public void setChair(final Chair chair) { _chair = chair; }


    /**
     * �L�^�����̋ΑӃR�[�h�𓾂�
     * @param time �L�^����
     * @param param �p�����[�^
     * @return �L�^�����̋ΑӃR�[�h
     */
    public Kintai getKintai(final BatchTime time, final Param param) {
        
        PeriodTimeTable ptt = null;
        BatchTime beginTime = null;
        BatchTime teacherTime = null;
        Kintai kintai = null;
        int processId=0;
        try {
            processId=0;
            ptt = param.getPeriodTimeTable(_period);
            processId = 1;
            beginTime = ptt.getBeginTime();
            processId = 2;
            teacherTime = (getTeacherData() == null) ? null : getTeacherData().getTime();
            processId = 3;
            kintai = Kintai.getKintai(time, beginTime, teacherTime);
            processId = 4;
        } catch (Exception ex) {
            log.debug(" !!! �Αӂ̎擾�ߒ��ŃG���[���������܂����B("+processId+") ���Ԋ�=[" + this + "] , �Z���^�C���e�[�u��=" + ptt + " , �Z���J�n����=" + beginTime +" , �L�^����=" + time +" !!!", ex);
            kintai = null;
        }

        return kintai;
    }

    /**
     * �搶�̍ݐЃR�[�h�𓾂�
     * @return �搶�̍ݐЃR�[�h
     */
    public String getStaffCd() {
        return (_teacherData == null) ? "00999999" : _teacherData.getSchregno();
    }

    /**
     * �u������u���Ă���N�g�̒��ɍݐДԍ������邩
     * @param schregno �ݐДԍ�
     * @return �u������u���Ă���N�g�̒��ɍݐДԍ��������true�A�����łȂ����false
     */
    public boolean hasStudent(final String schregno) {
        return getStudentList().contains(schregno);
    }

    /**
     * �A�����Ƃ̎��Ԋ����Z�b�g����
     * @param schedule ���Ԋ�
     */
    public void setContinuedSchedule(final Schedule schedule) {
        _continuedSchedule = schedule;
        schedule._continuedSchedule = this;
    }

    /**
     * �A�����Ƃ̎��Ԋ��𓾂�
     * @return �A�����Ƃ̎��Ԋ�
     */
    public Schedule getContinuedSchedule() {
        return _continuedSchedule;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _date + ", �Z���R�[�h=[" + _period.getCode() + "], �u���R�[�h=[" + _chairCd + "]";
    }

    /**
     * �搶�̓��ގ��f�[�^�𓾂�
     * @return �搶�̓��ގ��f�[�^
     */
    public RoomEnterExitData getTeacherData() {
        return _teacherData;
    }

    /**
     * �搶�̓��ގ��f�[�^���Z�b�g����
     * �����A���̐搶�����ɑō����Ă�����
     * ���Ԃ̑����ꍇ�̂݃Z�b�g����
     * @param data �搶�̓��ގ��f�[�^
     */
    public void setTeacherData(final RoomEnterExitData data) {
        if (_teacherData == null || data.getTime().isBefore(_teacherData.getTime(), false)) {
            _teacherData = data;
        }
    }

    /**
     * �u������u����S�w���̃��X�g�𓾂�
     * @return �u������u����S�w���̃��X�g
     */
    private List getStudentList() {
        return _chair.getStudents();
    }


    /**
     * ���o���f�[�^��ǉ�����
     * @param attendance ���o���f�[�^
     */
    public void addAttendance(final Attend2Dat attendance) {
        _attendanceList.add(attendance);
    }

    /**
     * �o�����X�g����w��̍ݐДԍ��̃f�[�^�𓾂�
     * @param schregno �ݐДԍ�
     * @return �ݐДԍ��̃f�[�^
     */
    public Attend2Dat getAttendance(final String schregno) {
        for (final Iterator it = _attendanceList.iterator(); it.hasNext();) {
            final Attend2Dat attend = (Attend2Dat) it.next();
            if (attend.getSchregno().equals(schregno)) {
                return attend;
            }
        }
        return null;
    }

    /**
     * �ΑӃt�@�C���ɖ����o���f�[�^���ݒ肷��B
     * @param param �p�����[�^
     * @param roomEnterExitDataMap OTR�o�͓��ގ��f�[�^�̊w�Дԍ��ƃf�[�^�̃}�b�v
     */
    public void setAllAttendance(final Param param, final Map roomEnterExitDataMap) {
        final List studentList = getStudentList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final String schregno = (String) it.next();

            if (null != getAttendance(schregno)) {
                continue;
            }
            if (roomEnterExitDataMap != null) {
                boolean checked = false;
                for (final Iterator it2 = roomEnterExitDataMap.keySet().iterator(); it2.hasNext();) {
                    String dataSchregno = (String) it2.next();
                    if (dataSchregno.equals(schregno))  checked = true;
                }
                if (checked) continue;
            }
            // �o�����X�g�ɂȂ��A�������̓`�F�b�N����Ă��Ȃ��f�[�^�̂ݒǉ�����B

            final Attend2Dat attendance = new Attend2Dat(
                    schregno,
                    getDate(),
                    null, // �����͖��� = �Αӂ̓f�t�H���g�� ���̌�(��)(�J�[�h���͖�)
                    this,
                    param.getYear());

            addAttendance(attendance);
            
        }
    }

    /**
     * �o���f�[�^���X�g��Ԃ�
     * @return �o���f�[�^���X�g
     */
    public List getAttendanceList() {
        return _attendanceList;
    }
} // Schedule

// eof
