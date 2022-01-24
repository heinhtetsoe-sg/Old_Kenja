// kanji=漢字
/*
 * $Id: DaoUpdateAttendSubclassDat.java 75473 2020-07-16 07:19:19Z maeshiro $
 *
 * 作成日: 2006/12/18 14:59:56 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.update;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.AccumulateSubclass;
import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.accumulate.option.Header;
import jp.co.alp.kenja.batch.dao.AbstractDaoUpdateAttendDat;
import jp.co.alp.kenja.common.dao.KenjaPS;
import jp.co.alp.kenja.common.dao.update.AbstractDaoUpdator;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.lang.QuietlyClosableUtils;
import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * 科目別累積データを更新する。
 * @author takaesu
 * @version $Id: DaoUpdateAttendSubclassDat.java 75473 2020-07-16 07:19:19Z maeshiro $
 */
public class DaoUpdateAttendSubclassDat extends AbstractDaoUpdateAttendDat {
    /** テーブル名 */
    public static final String TABLE_NAME = "ATTEND_SUBCLASS_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoUpdateAttendSubclassDat.class);

    /**
     * コンストラクタ。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param options オプション
     */
    public DaoUpdateAttendSubclassDat(
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

    /**
     * {@inheritDoc}
     */
    public void delete(final Header header) throws SQLException {
        DeleteByMonth daoDel = null;

        try {
            final String nendo = header.getNendo().toString();
            final String month = header.getMonthAsString();

            for (final String schoolKind : _options.getSchoolKind()) {
                daoDel = new DeleteByMonth(_conn, _cm, _options.getKenjaParameters(), schoolKind);
                log.info(" sql = " + daoDel.getUpdateSql());
                final int delCount = daoDel.delete(nendo, month);
                log.info("科目別累積の削除件数: " + delCount + "件。");
                QuietlyClosableUtils.closeQuietly(daoDel);
            }
        } finally {
            QuietlyClosableUtils.closeQuietly(daoDel);
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
                    + " SCHOOL_KIND,"
                    + " CURRICULUM_CD,"
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
                    + " KOUDOME,"
                    + " REGISTERCD,"
                    + " UPDATED"
                    + ") values ("
                    + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp"
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
                + " SCHOOL_KIND,"
                + " CURRICULUM_CD,"
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
                + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp"
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
            ps.setString(c++, subClass.getClassCd());
            ps.setString(c++, subClass.getSchoolKind());
            ps.setString(c++, subClass.getCurriculumCd());
            ps.setString(c++, subClass.getSubClassCd());
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
                ps.setInt(c++, rrr.getKoudome());
            }

            ps.setString(c++, registerCd);

            return super.executeUpdate();
        }
    } // Insert

    //========================================================================

    /**
     * 年月指定のDelete用。
     */
    private class DeleteByMonth extends AbstractDaoUpdator {
        private final String _schoolKind;
        public DeleteByMonth(
                final Connection conn,
                final ControlMaster cm,
                final KenjaParameters params,
                final String schoolKind
        ) {
            super(log, conn, cm, params);
            _schoolKind = schoolKind;
        }

        /**
         * {@inheritDoc}
         */
        public String getUpdateSql() {
            return "delete from " + TABLE_NAME
                    + " where COPYCD = ?"
                    + "  and YEAR = ?"
                    + "  and MONTH = ?"
                    + "  and SEMESTER = ?"
                    + (StringUtils.isEmpty(_schoolKind) ? "" : "  and SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT I1 INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE WHERE I1.YEAR = ? AND I2.SCHOOL_KIND = '" + _schoolKind + "') ")
                ;
        }

        public int delete(final String nendo, final String month) throws SQLException {
            final KenjaPS ps = ps();
            set(ps, nendo, month);
            return super.executeUpdate();
        }

        private void set(
                final KenjaPS ps,
                final String nendo,
                final String month
        ) throws SQLException {
            int c = 1;

            ps.setString(c++, COPYCD);
            ps.setString(c++, nendo);
            ps.setString(c++, month);
            ps.setString(c++, getControlMaster().getCurrentSemester().getCodeAsString());
            if (null != _schoolKind) {
                ps.setString(c++, nendo);
            }
        }
    } // DeleteByMonth
} // DaoUpdateAttendSubclassDat

// eof
