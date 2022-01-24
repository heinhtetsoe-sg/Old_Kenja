// kanji=����
/*
 * $Id: MkJikanwariTouroku.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/12 16:13:05 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.nbi.groupware.domain.Chair;
import jp.co.alp.kenja.batch.nbi.groupware.domain.HomeRoom;
import jp.co.alp.kenja.batch.nbi.groupware.domain.Schedule;
import jp.co.alp.kenja.batch.nbi.groupware.domain.SubClass;

/**
 * ���Ԋ��o�^CSV�B
 * @author takaesu
 * @version $Id: MkJikanwariTouroku.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class MkJikanwariTouroku extends Mk {
    /*pkg*/static final Log log = LogFactory.getLog(MkJikanwariTouroku.class);

    private final static String _FILE = "group0501.csv";

    /** ���Ԋ����������擾���邩? */
    public final int DAYS = 7;

    /** ���Ԋ��f�[�^ */
    private List _schedules = new ArrayList();

    /** �u���}�X�^ */
    private Map _chairs;

    /** �Q�}�X�^ */
    private Map _gunMaster;

    public MkJikanwariTouroku(final DB2UDB db, final Param param, final String title) throws SQLException {
        super(db, param, title);

        loadMasterData(db);
        loadSchedules();
        _db.commit();
        _db.close();

        final List list = new ArrayList();
        for (final Iterator it = _schedules.iterator(); it.hasNext();) {    // TAKAESU: ���̃C�e���[�g���ƌQ�Ɣ�Q���������Ă��܂�
            final Schedule schedule = (Schedule) it.next();

            if ("1".equals(schedule.getPeriodCd())) {
                continue;   // TAKAESU: NBI �̗v�]�u1�����ڂ�SHR�����疳�����ė~�����v
            }

            final List data = createData(schedule);
            list.addAll(data);
        }
        log.info("���Ԋ��o�^�̃��R�[�h��=" + list.size());

        // CSV�t�@�C���ɏ���
        toCsv("���Ԋ��o�^", _FILE, list);
    }

    private List createData(final Schedule schedule) {
        final List rtn = new ArrayList();

        for (final Iterator it = schedule.getChair().getStaffCodes().iterator(); it.hasNext();) {
            final String staffCd = (String) it.next();

            final List homeRooms;
            final String gunCode;
            final String gunName;
            final String gunAbbv;
            if (schedule.getChair().isGun()) {
                final Gun gun = (Gun) _gunMaster.get(schedule.getChair().getGroupCd());
                gunCode = gun._code;
                gunName = gun._name;
                gunAbbv = gun._abbv;
                homeRooms = gun._homeRooms;
            } else {
                gunCode = null;
                gunName = null;
                gunAbbv = null;
                homeRooms = schedule.getChair().getHomeRooms();
            }

            for (final Iterator it2 = homeRooms.iterator(); it2.hasNext();) {
                final HomeRoom hr = (HomeRoom) it2.next();

                final String[] fields = {
                        _param.getSchoolDiv(),
                        _param.getYear(),
                        Param.SDF.format(schedule.getDate()),
                        schedule.getPeriodCd(),
                        hr.getCode(),
                        gunCode,    // �Q�R�[�h
                        gunName,    // �Q����
                        gunAbbv,    // �Q����
                        schedule.getChair().getSubclass().getCode(),
                        schedule.getChair().getSubclass().getName(),
                        schedule.getChair().getSubclass().getAbbv(),
                        staffCd,    // �E���R�[�h
                        _param._sdate,  // �K�p�J�n�N����
                        _param._edate,  // �K�p�I���N����
                };
                rtn.add(fields);
            }
        }
        return rtn;
    }

    private void loadMasterData(final DB2UDB db) throws SQLException {
        try {
            final Map homeRooms = HomeRoom.load(db, _param.getYear(), _param.getSemester());
            log.debug("���Ԋ��o�^: �g�ݖ��̂̏��: schreg_regd_hdat �̑���=" + homeRooms.size());

            final Map subClasses = SubClass.load(db, _param.getYear(), _param.getSemester());
            log.debug("���Ԋ��o�^: �Ȗڂ̏��: subclass_mst �̑���=" + subClasses.size());

            _chairs = Chair.load(_db, _param.getYear(), _param.getSemester(), subClasses);
            log.debug("���Ԋ��o�^: �u���}�X�^�̐�=" + _chairs.size());
            Chair.loadStaffs(_chairs, db, _param.getYear(), _param.getSemester());
            Chair.loadClasses(_chairs, db, _param.getYear(), _param.getSemester(), homeRooms);

            // �Q�}�X�^
            _gunMaster = createGun();
            log.debug("���Ԋ��o�^: �Q�̏��: electclass_mst �̑���=" + _gunMaster.size());
        } catch (final SQLException e) {
            log.fatal("���Ԋ��o�^�̃}�X�^�n�擾�ŃG���[");
            throw e;
        } finally {
            _db.commit();
        }

        // �Q�}�X�^�ɔN�g���Ԃ牺����
        final MultiMap gunClasses = Chair.getGunClasses();
        for (final Iterator it = gunClasses.keySet().iterator(); it.hasNext();) {
            final String groupcd = (String) it.next();
            final Gun gun = (Gun) _gunMaster.get(groupcd);
            final Collection hrs = (Collection) gunClasses.get(groupcd);
            gun._homeRooms.addAll(hrs);
        }
    }

    private Map createGun() throws SQLException {
        final String sql;
        sql = "SELECT DISTINCT"
            + "  t1.groupcd,"
            + "  t2.groupname,"
            + "  t2.groupabbv"
            + " FROM"
            + "  chair_dat t1 inner join electclass_mst t2 on t1.groupcd = t2.groupcd"
            + " WHERE"
            + "  year='" + _param.getYear() + "' AND"
            + "  semester='" + _param.getSemester() + "'"
            ;

        final Map rtn = new HashMap();
        ResultSet rs = null;
        _db.query(sql);
        rs = _db.getResultSet();
        while(rs.next()) {
            final String code = rs.getString("groupcd");
            final String name = rs.getString("groupname");
            final String abbv = rs.getString("groupabbv");
            final Gun gun = new Gun(code, name, abbv);
            rtn.put(code, gun);
        }
        
        return rtn;
    }

    private void loadSchedules() throws SQLException {
        final String sql = scheduleSql();
        ResultSet rs = null;
        _db.query(sql);
        rs = _db.getResultSet();
        while(rs.next()) {
            final Date date = rs.getDate("executedate");
            final String periodCd = rs.getString("periodcd");
            final String chairCd = rs.getString("chaircd");
            final String executed = rs.getString("executed");

            final boolean b = "1".equals(executed);
            final Schedule schedule = new Schedule(date, periodCd, chairCd, b);
            final Chair chair = (Chair) _chairs.get(chairCd);
            if (null == chair) {
                log.warn("�u���̑��݂��Ȃ����Ԋ��͖���: " + schedule);
                continue;
            }
            schedule.setChair(chair);
            _schedules.add(schedule);
        }
        log.info("���Ԋ��̃R�}��=" + _schedules.size());
    }

    private String scheduleSql() {
        final Calendar startDate = _param.getCalDate();
        final Calendar endDate = addDate(startDate, DAYS);
        final String endDateStr = Param.SDF.format(endDate.getTime());

        final String sql;
        sql = "SELECT"
            + "  executedate,"
            + "  periodcd,"
            + "  chaircd,"
            + "  executed"
            + " FROM"
            + "  sch_chr_dat"
            + " WHERE"
            + "  year='" + _param.getYear() + "' AND"
            + "  semester='" + _param.getSemester() + "' AND"
            + "  executedate BETWEEN '" + _param.getDate() + "' AND '" + endDateStr + "'"
            + " ORDER BY executedate, periodcd, chaircd"
            ;

        log.debug("���Ԋ��擾�̊J�n��=" + _param.getDate());
        log.debug("���Ԋ��擾�̏I����=" + endDateStr);
        log.debug("���Ԋ��擾��SQL=" + sql);
        return sql;
    }

    private Calendar addDate(final Calendar base, final int day) {
        final Calendar cal = (Calendar) base.clone();
        cal.add(Calendar.DATE, day);
        return cal;
    }

    private class Gun {
        private final String _code;
        private final String _name;
        private final String _abbv;
        private final List _homeRooms = new ArrayList();

        public Gun(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;
        }
    }
} // MkJikanwariTouroku

// eof
