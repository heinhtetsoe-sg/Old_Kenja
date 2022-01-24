// kanji=����
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 * �쐬��: 2008/03/18
 * �쐬��: maesiro
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jp.co.alp.kenja.batch.otr.domain.KenjaDateImpl;
import jp.co.alp.kenja.batch.otr.domain.Period;
import jp.co.alp.kenja.batch.otr.domain.Schedule;
import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * OTR(�J�[�h���[�_�[)�Ǎ�
 * @author maesiro
 * @version $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class Main {
    /*pkg*/static final Log log = LogFactory.getLog(Main.class);

    /**
     * ���C��
     * @param args ����
     * @throws Exception ��O
     */
    public static void main(final String[] args) throws Exception {

        final Param param = new Param(args);

        final DB2UDB db = new DB2UDB(param.getDbUrl(), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        db.open();

        param.load(db);

        // �ΑӃt�@�C����ǂݍ��ޏ���
        final List data = RoomEnterExitData.load(param.getKintaiFile());

        // �������̍Z�����̓��ގ��f�[�^�}�b�v��Ԃ��B
        final Map datePeriodMap = getDatePeriodMap(data, param);

        // ���Ԋ��ǂݍ���
        final ScheduleLoader schLoader = new ScheduleLoader(db, param);

        // ���ގ��f�[�^�����Ԋ��Ɋ֘A�t����
        setScheduleMap(datePeriodMap, schLoader, param);

        // �Αӂ��Z�b�g����
        setKintai(datePeriodMap, schLoader, param);

        // DB�Ƀf�[�^���C���T�[�g����
        insertData(db, datePeriodMap, schLoader, param);

        log.fatal("Done.");
    }

    /**
     * �������̍Z�����̓��ގ��f�[�^�}�b�v��Ԃ��B
     * �����Ȏ��Ԃ̃f�[�^�͊܂߂Ȃ��悤�ɂ���B
     * ����Z���̓���̐l���̓��͂��������ꍇ�A�x����\�����āA���Ԃ������f�[�^�ōX�V����B
     * @param roomEnterExitDataList ���ގ��f�[�^���X�g
     * @param param �p�����[�^
     * @return �������̍Z�����̓��ގ��f�[�^�}�b�v
     */
    private static Map getDatePeriodMap(final List roomEnterExitDataList, final Param param) {
        final Map rtn = new TreeMap();
        int count = 0;
        for (final Iterator it = roomEnterExitDataList.iterator(); it.hasNext();) {
            final RoomEnterExitData data = (RoomEnterExitData) it.next();
            final KenjaDateImpl date = data.getDate();

            KenjaDateImpl oldestDate = param.getOldestDate();
            if (oldestDate == null || date.compareTo(oldestDate) < 0) {
                param.setOldestDate(date);
            }

            if (rtn.get(date) == null) {
                rtn.put(date, new TreeMap());
            }
            final Map periods = (Map) rtn.get(date);

            final BatchTime time = data.getTime();
            final Period period = param.getPeriod(param.getPeriodTimeTable(time));
            if (period == null) {
                log.error("�Z������`�O�ł��B[" + data + "]");
                continue;
            }

            if (periods.get(period) == null) {
                periods.put(period, new TreeMap());
            }
            final Map students = (Map) periods.get(period);

            final RoomEnterExitData inputbefore = (RoomEnterExitData) students.get(data.getSchregno());
            if (inputbefore == null) {
                students.put(data.getSchregno(), data);
            } else {
                log.warn("����Z����1��ȏ�̓��͂ł��B [" + data + "], �O��̓���[" + inputbefore + "]");
                final BatchTime timeBefore = inputbefore.getTime();
                if (timeBefore.isAfter(time, false)) {
                    log.warn("     [" + data + "] �ōX�V���܂��B");
                    students.put(data.getSchregno(), data);
                }
                count++;
            }
        }
        log.debug("����Z�����͂̔p���f�[�^�� = " + count);

        return rtn;
    }

    /**
     * ���ގ��f�[�^�����Ԋ��Ɋ֘A�t����
     * @param dates �������̍Z�����̓��ގ��f�[�^�}�b�v
     * @param schLoader ���Ԋ����[�_�[
     */
    private static void setScheduleMap(final Map dates, final ScheduleLoader schLoader, final Param param) {
        int dataCount = 0;
        int notFound = 0;
        for (final Iterator dateIt = dates.keySet().iterator(); dateIt.hasNext();) {
            final KenjaDateImpl date = (KenjaDateImpl) dateIt.next();
            log.debug(" ���t = " + date);

            final Map periods = (Map) dates.get(date);
            for (final Iterator periodIt = periods.keySet().iterator(); periodIt.hasNext();) {
                final Period period = (Period) periodIt.next();
                log.debug("   �Z�� = " + period);

                final Map dataMap = (Map) periods.get(period);
                for (final Iterator dataIt = dataMap.keySet().iterator(); dataIt.hasNext();) {
                    final String schregno = (String) dataIt.next();
                    dataCount++;
                    log.debug("    " + dataMap.get(schregno));

                    final RoomEnterExitData data = (RoomEnterExitData) dataMap.get(schregno);
                    final boolean addSucceed = schLoader.addAttendance(date, period, data, param);
                    if (!addSucceed) {
                        notFound++;
                    }
                }
            }
        }
        log.debug(notFound + "���̍u�����Ԋ���������܂���ł����B");
    }

    /**
     * �ΑӂƓo�^�R�[�h���Z�b�g����
     * @param schLoader ���Ԋ����[�_�[
     * @param roomEnterExitDataList OTR�o�͓��ގ��f�[�^���X�g
     * @param param �p�����[�^
     */
    private static void setKintai(final Map datePeriodMap, final ScheduleLoader schLoader, final Param param) {
        for (final Iterator schIt = schLoader.getAttendanceList(param, datePeriodMap).iterator(); schIt.hasNext();) {
            final Attend2Dat attend = (Attend2Dat) schIt.next();

            final Schedule sch = attend.getSchedule();
            attend.setRegisterCd(sch.getStaffCd());
            if(attend.isInvalid()) continue; // �����ȃf�[�^�͋ΑӔ�������Ȃ�(�f�t�H���g��"���̌�(��) (�J�[�h���͖�)")

            attend.setKintai(sch, param);
            //log.debug(" �o���f�[�^ = " + attend);

            // �A�����Ƃ̈���
            setSecondScheduleAttendanceSeated(attend.getSchregno(), sch);
        }
    }
    
    /**
     * �w��̎��Ԋ��ɏ�������w�Дԍ��̘A�����Ƃ�T���A
     * 2���Ԗڂ̎��Ԋ��̏o����"�o��"�Ƃ���B
     * @param schregno �w�Дԍ� 
     * @param sch1 ���Ԋ�
     */
    private static void setSecondScheduleAttendanceSeated(final String schregno, final Schedule sch1) {
        final Schedule sch2 = sch1.getContinuedSchedule();
        if (sch2 == null) {
            return;
        }
        
        Attend2Dat att1 = sch1.getAttendance(schregno);
        Attend2Dat att2 = sch2.getAttendance(schregno);
        if (att1 == null || att2 == null) {
            return;
        }
        
        Attend2Dat secondAttend = null;
        if (sch1.getPeriod().isBefore(sch2.getPeriod()) && !att1.getKintai().isNonotice()) {
            // sch1��1���Ԗڂ̎���
            // => sch1�����̌�(��)(�J�[�h���͖���)�łȂ����sch2�͏o�ȂƂ���
            secondAttend = att2;
        } else if (sch1.getPeriod().isAfter(sch2.getPeriod()) && !att2.getKintai().isNonotice()) {
            // sch2��1���Ԗڂ̎���
            // => sch2�����̌�(��)(�J�[�h���͖���)�łȂ����sch1�͏o�ȂƂ���
            secondAttend = att1;
        }

        if (secondAttend == null) { 
            return;
        }

        secondAttend.setKintaiSeated();
        secondAttend.setRegisterCd(sch2.getStaffCd());
    }

    /**
     * DB�Ƀf�[�^���C���T�[�g����
     * @param db2 DB
     */
    private static void insertData(final DB2UDB db, final Map datePeriodMap, final ScheduleLoader schLoader, final Param param) {
        int count = 0;
        log.debug("DB�ւ̃f�[�^�o�͊J�n�B");
        for (final Iterator schIt = schLoader.getAttendanceList(param, datePeriodMap).iterator(); schIt.hasNext();) {
            final Attend2Dat attend = (Attend2Dat) schIt.next();

            if (!param.outputsSeatedToDB() && attend.getKintai().isSeated()) {
                log.debug("�Αӂ�'�o��'�Ȃ̂�DB�ɏo�͂��Ȃ� = " + attend);
                continue;
            }
            log.debug(" DB�o�� �o���f�[�^ = " + attend);
            try {
                db.query(attend.getSelectSql()); // DB�ɂ��̃f�[�^�����łɂ��邩��������B
                final ResultSet rs = db.getResultSet();
                final boolean found = rs.next();
                
                // DB�ɓ���̃f�[�^�����݂���ꍇ
                if (found) {
                    String beforeOutputData = "(�ݐДԍ�=["+rs.getString("SCHREGNO")+"]�A���t=["+rs.getString("ATTENDDATE")+"]�A�Z���R�[�h=["+rs.getString("PERIODCD")+"]�A�u���R�[�h=["+rs.getString("CHAIRCD")+"]�A�ΑӃR�[�h=["+rs.getString("DI_CD")+"]�A�X�V����=[" + rs.getString("UPDATED") + "])";
                    log.debug("   ���ł�DB�ɓ���̃f�[�^�����݂��܂��B" + beforeOutputData);
                    if (!param.deletesOldDataInDB()) {
                        log.debug("   �o�͂𒆎~���܂��B");
                        continue;
                    }
                    log.debug("   �폜���ďo�͂��܂��B");
                    db.executeUpdate(attend.getDeleteSql()); // ����̌��f�[�^���폜����
                }

                count += db.executeUpdate(attend.getInsertSql()); // �f�[�^���o�͂���
            } catch (SQLException ex) {
                String message = "DB�o�̓G���[�B";
                if (ex.getErrorCode() == -803) {
                    message += "::�f�[�^�d���G���[  �d���f�[�^=";
                }
                log.warn(message);
            }
            db.commit();
        }
        log.debug("DB�ւ̃f�[�^�o�͏I���B�o�̓f�[�^�� = " + count);
    }
} // Main

// eof
