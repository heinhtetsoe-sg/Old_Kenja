// kanji=����
/*
 * $Id: ScheduleLoader.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/12 16:13:05 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jp.co.alp.kenja.batch.otr.domain.Chair;
import jp.co.alp.kenja.batch.otr.domain.Facility;
import jp.co.alp.kenja.batch.otr.domain.HomeRoom;
import jp.co.alp.kenja.batch.otr.domain.KenjaDateImpl;
import jp.co.alp.kenja.batch.otr.domain.Period;
import jp.co.alp.kenja.batch.otr.domain.Schedule;
import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ���Ԋ����[�_�[
 * @version $Id: ScheduleLoader.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class ScheduleLoader {

    /*pkg*/static final Log log = LogFactory.getLog(ScheduleLoader.class);
    /** ���Ԋ��f�[�^ */
    private Map _schedules = new TreeMap();
    /** �u���}�X�^ */
    private Map _chairs;
    /** DB */
    private final DB2UDB _db;
    /** �p�����[�^ */
    private final Param _param;

    /**
     * �R���X�g���N�^
     * @param db DB
     * @param param �p�����[�^�[
     * @throws SQLException SQL��O
     */
    public ScheduleLoader(final DB2UDB db, final Param param) throws SQLException {
        _db = db;
        _param = param;
        loadMasterData(_db);
        loadSchedules();
        debugDisplaySchedule();
        _db.commit();
    }

    /**
     * DB�̃f�[�^(HR�N���X�A�u���A�u����u�N���X�A�u���S���E���f�[�^)�����[�h����
     * @param db DB
     * @throws SQLException SQL��O
     */
    private void loadMasterData(final DB2UDB db) throws SQLException {
        try {
            final String semester = _param.getSemester();
            final Map facilities = Facility.load(db);

            final Map homeRooms = HomeRoom.load(db, _param.getYear(), semester);
            log.debug("���Ԋ��o�^: �g�ݖ��̂̏��: schreg_regd_hdat �̑���=" + homeRooms.size());
            for (final Iterator it = homeRooms.keySet().iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) homeRooms.get(it.next());
                log.debug("  HomeRoom " + hr.getCode() + " �w���� = " + hr.getStudentCount());
            }

            _chairs = Chair.load(_db, _param.getYear(), semester);
            log.debug("���Ԋ��o�^: �u���}�X�^�̐�=" + _chairs.size());
            Chair.loadStaffs(_chairs, db, _param.getYear(), semester);
            Chair.loadStudents(_chairs, db, _param.getYear(), semester, _param.getTargetDate().toString());
            Chair.loadFacilities(_chairs, db, _param.getYear(), semester, facilities);

        } catch (final SQLException e) {
            log.fatal("���Ԋ��o�^�̃}�X�^�n�擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
        }
    }

    private void loadSchedules() throws SQLException {
        final Map facilities = Facility.load(_db);
        final String sql = scheduleSql();
        log.debug("********* sql = " + sql);
        _db.query(sql);
        final ResultSet rs = _db.getResultSet();
        while (rs.next()) {
            final KenjaDateImpl date = KenjaDateImpl.getInstance(rs.getDate("executedate"));
            if (_schedules.get(date) == null) {
                _schedules.put(date, new TreeMap());
            }
            final Map periodMap = (Map) _schedules.get(date);
            final Period period = _param.getPeriod(rs.getString("periodcd"));

            // �Z���R�[�h���Z���^�C���e�[�u���ɖ����Ȃ炱�̎��Ԋ���ǂݍ��܂Ȃ�
            if (period == null) {
                continue;
            }

            if (periodMap.get(period) == null) {
                periodMap.put(period, new ArrayList());
            }
            final List schedules = (List) periodMap.get(period);
            final String chairCd = rs.getString("chaircd");
            final String executed = rs.getString("executed");
            final boolean b = "1".equals(executed);
            final Schedule schedule = new Schedule(date, period, chairCd, b);
            final Chair chair = (Chair) _chairs.get(chairCd);
            if (null == chair) {
                log.warn("�u���̑��݂��Ȃ����Ԋ��͖���: " + schedule);
                continue;
            }
            schedule.setChair(chair);
            final String faccd = rs.getString("faccd");
            final Facility facility = faccd != null ? (Facility) facilities.get(faccd) : null;
            if (facility != null) {
                schedule.addFacility(facility);
            }
            schedules.add(schedule);
            
            // �A�����Ƃ̐ݒ�
            for (final Iterator it = getNeighboorPeriodScheduleList(date, period).iterator(); it.hasNext();) {
                final Schedule anotherSchedule = (Schedule) it.next();
                if (!anotherSchedule.getChair().equals(chair)) {
                    continue;
                }
                schedule.setContinuedSchedule(anotherSchedule);
                log.debug("  �A������ [" + schedule + "] <==> [" + anotherSchedule + "]");
            }
        }
        log.info("���Ԋ��ǂݍ��ݏI���B���Ԋ��̓���=" + _schedules.size());
    }

    /**
     * ���Ԋ����擾����sql�𓾂�
     * �o���������{�̂��̂�ΏۂƂ���
     * @return ���Ԋ����擾����sql
     */
    private String scheduleSql() {
        Period targetPeriod = _param.getTargetPeriod();
        String periodcd = (targetPeriod == null) ? "0" : targetPeriod.getCode();
        final String sql = "SELECT"
            + "  t1.executedate,"
            + "  t1.periodcd,"
            + "  t1.chaircd,"
            + "  t1.executed,"
            + "  t2.faccd"
            + " FROM"
            + "  sch_chr_dat t1"
            + " LEFT JOIN sch_fac_dat t2 ON "
            + "  t1.year = t2.year AND"
            + "  t1.semester = t2.semester AND"
            + "  t1.executedate = t2.executedate AND"
            + "  t1.periodcd = t2.periodcd AND"
            + "  t1.chaircd = t2.chaircd "
            + " WHERE"
            + "  t1.year='" + _param.getYear() + "' AND"
            + "  t1.semester='" + _param.getSemester() + "' AND"
            + "  ((t1.executedate='" + _param.getTargetDate() + "' AND t1.periodcd <= '" + periodcd + "' ) OR "
            + "   (t1.executedate BETWEEN '" + _param.getOldestDate() + "' AND '" + _param.getTargetDate().add(-1) + "')) AND"
            + "  t1.executed = '0'"
            + " ORDER BY t1.executedate, t1.periodcd, t1.chaircd"
            ;
        return sql;
    }

    private void debugDisplaySchedule() {

        for (final Iterator it = _schedules.keySet().iterator(); it.hasNext();) {
            final KenjaDateImpl date = (KenjaDateImpl) it.next();
            log.debug("�����t = " + date);

            final Map periodMap = (Map) _schedules.get(date);
            for (final Iterator it2 = periodMap.keySet().iterator(); it2.hasNext();) {
                final Period period = (Period) it2.next();
                log.debug("  ���Z�� = " + period);

                final List scheduleList = (List) periodMap.get(period);
                for (final Iterator it3 = scheduleList.iterator(); it3.hasNext();) {
                    final Schedule schedule = (Schedule) it3.next();
                    final Chair chair = schedule.getChair();
                    if (chair.getStudents().size() != 0) {
                        log.debug("      " + chair);
                    }
                }
            }
        }
    }

    /**
     * �N���ƍZ���̎��Ԋ��ɓ��ގ��f�[�^���Z�b�g����B
     * @param date �N��
     * @param period �Z��
     * @param data ���ގ��f�[�^
     * @param param �p�����[�^
     * @return �f�[�^���Z�b�g���ꂽ��true�A�����łȂ����false��Ԃ�
     */
    public boolean addAttendance(final KenjaDateImpl date, final Period period, final RoomEnterExitData data, final Param param) {
        final List schedules = getSchedules(date, period);
        if (schedules.isEmpty()) {
            log.debug("���̌����Z���̎��Ԋ��͎w��͈̔͊O�ł��B" + date + " �A " + period + "(" + data + ")");
            return false;
        }

        for (final Iterator it = schedules.iterator(); it.hasNext();) {
            final Schedule schedule = (Schedule) it.next();
            final String schregno = data.getSchregno();

            if (data.isStaff() && schedule.getChair().hasTeacher(schregno)) {
                // �搶�̃f�[�^�Ȃ玞�Ԋ��ɓo�^����
                schedule.setTeacherData(data);
                log.debug("  �X�^�b�t�̃f�[�^ ���Ԋ�=[" + schedule + "] �A �搶�̑ō��f�[�^ = [" + data + "]");
                return true;
            } else if (schedule.hasStudent(schregno)) {

                // ���k�̃f�[�^�����Ԋ��̍u���ɓo�^����Ă���΁A�o���f�[�^�Ƃ��ēo�^����
                final Attend2Dat attend = new Attend2Dat(
                    data,
                    schedule,
                    param.getYear()
                );
                schedule.addAttendance(attend);
                return true;
            }
        }
        return false;
    }


    /**
     * �w�肳�ꂽ�����Z���̎��Ԋ����X�g��Ԃ��B
     * @param date ����
     * @param period �Z��
     * @return �w�肳�ꂽ�����Z���̎��Ԋ����X�g
     */
    private List getSchedules(final KenjaDateImpl date, final Period period) {
        final Map periodMap = (Map) _schedules.get(date);
        if (null == period || null == periodMap || null == periodMap.get(period)) {
            return java.util.Collections.EMPTY_LIST;
        }
        return (List) periodMap.get(period);
    }
    /**
     * �w�肳�ꂽ�����́A�O�̍Z���Ǝ��̍Z���̎��Ԋ����X�g��Ԃ��B
     * @param date ����
     * @param period �Z��
     * @return �w�肳�ꂽ�����́A�O�̍Z���Ǝ��̍Z���̎��Ԋ����X�g
     */
    private List getNeighboorPeriodScheduleList(final KenjaDateImpl date, final Period period) {
        final List schedules = new ArrayList();
        schedules.addAll(getSchedules(date, period.getPrevious()));
        schedules.addAll(getSchedules(date, period.getNext()));
        return schedules;
    }

    /**
     * �ΑӃt�@�C���ɖ����f�[�^���܂߂��S�o���f�[�^��Ԃ��B
     * @param param �p�����[�^
     * @param param roomEnterExitDataList OTR�o�͓��ގ��f�[�^���X�g
     * @return �S�o���f�[�^
     */
    public List getAttendanceList(final Param param, final Map datePeriodMap) {
        final List attendanceList = new LinkedList();

        for (final Iterator it = _schedules.keySet().iterator(); it.hasNext();) {
            final KenjaDateImpl date = (KenjaDateImpl) it.next();

            final Map dataPeriodMap = (Map) datePeriodMap.get(date);
            final Map periodMap = (Map) _schedules.get(date);
            for (final Iterator it2 = periodMap.keySet().iterator(); it2.hasNext();) {
                final Period period = (Period) it2.next();
                
                final Map roomEnterExitDataMap = (dataPeriodMap==null) ? null : (Map) dataPeriodMap.get(period);

                final List schedules = (List) periodMap.get(period);
                for (final Iterator it3 = schedules.iterator(); it3.hasNext();) {
                    final Schedule sch = (Schedule) it3.next();
                    sch.setAllAttendance(param, roomEnterExitDataMap);
                    attendanceList.addAll(sch.getAttendanceList());
                }
            }
        }
        return attendanceList;
    }
} // MkJikanwariTouroku

// eof
