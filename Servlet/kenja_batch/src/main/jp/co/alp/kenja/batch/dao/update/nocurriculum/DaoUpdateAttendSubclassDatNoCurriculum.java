// kanji=漢字
/*
 * $Id: DaoUpdateAttendSubclassDatNoCurriculum.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2006/12/18 14:59:56 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.update.nocurriculum;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import jp.co.alp.kenja.batch.accumulate.AccumulateSubclass;
import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.accumulate.option.Header;
import jp.co.alp.kenja.batch.dao.update.DaoUpdateAttendSubclassDat;
import jp.co.alp.kenja.batch.domain.HogeUtils;
import jp.co.alp.kenja.common.dao.KenjaPS;
import jp.co.alp.kenja.common.dao.update.AbstractDaoUpdator;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.util.KenjaParameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 科目別累積データを更新する。
 * @author takaesu
 * @version $Id: DaoUpdateAttendSubclassDatNoCurriculum.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class DaoUpdateAttendSubclassDatNoCurriculum extends DaoUpdateAttendSubclassDat {
    /** テーブル名 */
    public static final String TABLE_NAME = "ATTEND_SUBCLASS_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoUpdateAttendSubclassDatNoCurriculum.class);

    /**
     * コンストラクタ。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param options オプション
     */
    public DaoUpdateAttendSubclassDatNoCurriculum(
            final Connection conn,
            final ControlMaster cm,
            final AccumulateOptions options
    ) {
        super(conn, cm, options);
    }

    /**
     * 保存する。
     * @param ruiseki 累積カウンタ
     * @throws SQLException SQL例外
     */
    public void save(
            final Header header,
            final Student student,
            final Map<SubClass, AccumulateSubclass> data
    ) throws SQLException {
        Insert dao = null;
        try {
            dao = new Insert(_conn, _cm, _options.getKenjaParameters());
            for (final Map.Entry<SubClass, AccumulateSubclass> e : data.entrySet()) {
                dao.insert(header, student, e.getKey(), e.getValue(), _options.getStaffCd());
            }
        } finally {
            if (null != dao) {
                dao.closeQuietly();
            }
        }
    }

    //========================================================================

    /**
     * Insert用。
     */
    private static class Insert extends AbstractDaoUpdator {
        public Insert(
                final Connection conn,
                final ControlMaster cm,
                final KenjaParameters params
        ) {
            super(log, conn, cm, params);
        }

        /**
         * {@inheritDoc}
         */
        public String getUpdateSql() {
            final boolean useExtended = BatchSchoolMaster.getBatchSchoolMaster().useExtendedAccumulateTable();
            if (useExtended) {
                return "insert into " + TABLE_NAME + " ("
                    + " COPYCD,"
                    + " YEAR,"
                    + " MONTH,"
                    + " SEMESTER,"
                    + " SCHREGNO,"
                    + " CLASSCD,"
                    + " SUBCLASSCD,"
                    + " APPOINTED_DAY,"
                    + " LESSON,"
                    + " OFFDAYS,"
                    + " ABSENT,"
                    + " SUSPEND,"
                    + " MOURNING,"
                    + " ABROAD,"
                    + " SICK,"
                    + " NOTICE,"
                    + " NONOTICE,"
                    + " NURSEOFF,"
                    + " LATE,"
                    + " EARLY,"
                    + " VIRUS,"
                    + " REGISTERCD,"
                    + " UPDATED"
                    + ") values ("
                    + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp"
                    + ")"
                    ;
            }
            return "insert into " + TABLE_NAME + " ("
                + " COPYCD,"
                + " YEAR,"
                + " MONTH,"
                + " SEMESTER,"
                + " SCHREGNO,"
                + " CLASSCD,"
                + " SUBCLASSCD,"
                + " APPOINTED_DAY,"
                + " LESSON,"
                + " OFFDAYS,"
                + " ABSENT,"
                + " SUSPEND,"
                + " MOURNING,"
                + " ABROAD,"
                + " SICK,"
                + " NOTICE,"
                + " NONOTICE,"
                + " NURSEOFF,"
                + " LATE,"
                + " EARLY,"
                + " REGISTERCD,"
                + " UPDATED"
                + ") values ("
                + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp"
                + ")"
                ;
        }

        public int insert(
                final Header header,
                final Student student,
                final SubClass subClass,
                final AccumulateSubclass rrr,
                final String registerCd
        ) throws SQLException {
            final KenjaPS ps = ps();
            int c = 1;
            ps.setString(c++, COPYCD);
            ps.setString(c++, header.getNendo().toString());
            ps.setString(c++, header.getMonthAsString());
            ps.setString(c++, getControlMaster().getCurrentSemester().getCodeAsString());
            ps.setString(c++, student.getCode());
            ps.setString(c++, HogeUtils.getClazzCode(subClass));
            ps.setString(c++, subClass.getCode());
            ps.setString(c++, header.getDayAsString());

            ps.setInt(c++, rrr.getLesson());
            ps.setInt(c++, rrr.getOffdays());
            ps.setInt(c++, rrr.getAbsent());
            ps.setInt(c++, rrr.getSuspend());
            ps.setInt(c++, rrr.getMourning());
            ps.setInt(c++, rrr.getAbroad());
            ps.setInt(c++, rrr.getSick());
            ps.setInt(c++, rrr.getNotice());
            ps.setInt(c++, rrr.getNonotice());
            ps.setInt(c++, rrr.getNurseoff());
            ps.setInt(c++, rrr.getLate() + rrr.getLateNonotice());
            ps.setInt(c++, rrr.getEarly() + rrr.getEarlyNonotice());
            if (BatchSchoolMaster.getBatchSchoolMaster().useExtendedAccumulateTable()) {
                ps.setInt(c++, rrr.getVirus());
            }

            ps.setString(c++, registerCd);

            return super.executeUpdate();
        }
    } // Insert

    //========================================================================

} // DaoUpdateAttendSubclassDat

// eof
