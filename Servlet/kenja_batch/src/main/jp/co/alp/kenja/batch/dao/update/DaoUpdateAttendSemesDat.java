// kanji=漢字
/*
 * $Id: DaoUpdateAttendSemesDat.java 75473 2020-07-16 07:19:19Z maeshiro $
 *
 * 作成日: 2006/12/25 16:21:38 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.update;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.AccumulateSemes;
import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.accumulate.option.Header;
import jp.co.alp.kenja.batch.dao.AbstractDaoUpdateAttendDat;
import jp.co.alp.kenja.common.dao.KenjaPS;
import jp.co.alp.kenja.common.dao.update.AbstractDaoUpdator;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.lang.QuietlyClosableUtils;
import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * 月別累積データを更新する。。
 * @author takaesu
 * @version $Id: DaoUpdateAttendSemesDat.java 75473 2020-07-16 07:19:19Z maeshiro $
 */
public class DaoUpdateAttendSemesDat extends AbstractDaoUpdateAttendDat {
    /** テーブル名 */
    public static final String TABLE_NAME = "ATTEND_SEMES_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoUpdateAttendSemesDat.class);

    /**
     * コンストラクタ。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param options オプション
     */
    public DaoUpdateAttendSemesDat(
            final Connection conn,
            final ControlMaster cm,
            final AccumulateOptions options
    ) {
        super(conn, cm, options);
    }

    /**
     * 保存する。
     * @param tuki 月別累積カウンタ
     * @throws SQLException SQL例外
     */
    public void save(
            final Header header,
            final Student student,
            final AccumulateSemes rrr
    ) throws SQLException {
        Insert dao = null;
        try {
            dao = new Insert(_conn, _cm, _options.getKenjaParameters());
            dao.insert(header, student, rrr, _options.getStaffCd());
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
                log.info("月別累積の削除件数: " + delCount + "件。");
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
            if (BatchSchoolMaster.getBatchSchoolMaster().useExtendedAccumulateTable()) {
                return "insert into " + TABLE_NAME + " ("
                    + " COPYCD,"
                    + " YEAR,"
                    + " MONTH,"
                    + " SEMESTER,"
                    + " SCHREGNO,"
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
                    + " LATE,"
                    + " EARLY,"
                    + " KEKKA_JISU,"
                    + " KEKKA,"
                    + " LATEDETAIL,"
                    + " VIRUS,"
                    + " KOUDOME,"
                    + " REGISTERCD,"
                    + " UPDATED"
                    + ") values ("
                    + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp"
                    + ")"
                    ;
            }
            return "insert into " + TABLE_NAME + " ("
                    + " COPYCD,"
                    + " YEAR,"
                    + " MONTH,"
                    + " SEMESTER,"
                    + " SCHREGNO,"
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
                    + " LATE,"
                    + " EARLY,"
                    + " REGISTERCD,"
                    + " UPDATED"
                    + ") values ("
                    + " ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp"
                    + ")"
                ;
        }

        public int insert(
                final Header header,
                final Student student,
                final AccumulateSemes rrr,
                final String registerCd
        ) throws SQLException {
            final KenjaPS ps = ps();
            int c = 1;
            ps.setString(c++, COPYCD);
            ps.setString(c++, header.getNendo().toString());
            ps.setString(c++, header.getMonthAsString());
            ps.setString(c++, getControlMaster().getCurrentSemester().getCodeAsString());
            ps.setString(c++, student.getCode());
            ps.setString(c++, header.getDayAsString());

            ps.setInt(c++, rrr.getLesson().size());
            ps.setInt(c++, rrr.getOffdays().size());
            ps.setInt(c++, rrr.getAbsent().size());
            ps.setInt(c++, rrr.getSuspend().size());
            ps.setInt(c++, rrr.getMourning().size());
            ps.setInt(c++, rrr.getAbroad().size());
            ps.setInt(c++, rrr.getSick().size());
            ps.setInt(c++, rrr.getNotice().size());
            ps.setInt(c++, rrr.getNonotice().size());
            ps.setInt(c++, rrr.getLate().size() + rrr.getLateNonotice().size());
            ps.setInt(c++, rrr.getEarly().size() + rrr.getEarlyNonotice().size());
            if (BatchSchoolMaster.getBatchSchoolMaster().useExtendedAccumulateTable()) {
                ps.setInt(c++, rrr.getKekkaJisu().size());
                ps.setInt(c++, rrr.getKekka().size());
                ps.setInt(c++, rrr.getLateDetail().size());
                ps.setInt(c++, rrr.getVirus().size());
                ps.setInt(c++, rrr.getKoudome().size());
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
} // DaoUpdateAttendSemesDat

// eof
