/*
 * $Id: DaoUpdateAttendSemesSublDat.java 75473 2020-07-16 07:19:19Z maeshiro $
 *
 * 作成日: 2011/10/14
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.update;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import jp.co.alp.kenja.batch.accumulate.AttendSemesSubl;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.accumulate.option.Header;
import jp.co.alp.kenja.batch.dao.AbstractDaoUpdateAttendDat;
import jp.co.alp.kenja.common.dao.KenjaPS;
import jp.co.alp.kenja.common.dao.update.AbstractDaoUpdator;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.lang.QuietlyClosableUtils;
import jp.co.alp.kenja.common.util.KenjaParameters;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 出欠月別大分類累積データを更新する。
 * @author maesiro
 * @version $Id: DaoUpdateAttendSemesSublDat.java 75473 2020-07-16 07:19:19Z maeshiro $
 */
public class DaoUpdateAttendSemesSublDat extends AbstractDaoUpdateAttendDat {
    /** テーブル名 */
    public static final String TABLE_NAME = "ATTEND_SEMES_SUBL_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoUpdateAttendAbsenceDat.class);

    /**
     * コンストラクタ。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param options オプション
     */
    public DaoUpdateAttendSemesSublDat(
            final Connection conn,
            final ControlMaster cm,
            final AccumulateOptions options
    ) {
        super(conn, cm, options);
    }

    /**
     * 保存する。
     * @param counter 月別累積カウンタ
     * @throws SQLException SQL例外
     */
    public void save(
            final Header header,
            final Student student,
            final List<AttendSemesSubl> list
    ) throws SQLException {
        Insert dao = null;
        try {
            dao = new Insert(_conn, _cm, _options.getKenjaParameters());
            dao.insert(header, student, list, _options.getStaffCd());
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
                log.info("月別大分類累積の削除件数: " + delCount + "件。");
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
            return "insert into " + TABLE_NAME + " ("
                + " COPYCD,"
                + " YEAR,"
                + " MONTH,"
                + " SEMESTER,"
                + " SCHREGNO,"
                + " DI_CD,"
                + " SUBL_CD,"
                + " CNT,"
                + " REGISTERCD,"
                + " UPDATED"
                + ") values ("
                + " ?,?,?,?,?,?,?,?,?,current timestamp"
                + ")";
        }

        public int insert(
                final Header header,
                final Student student,
                final List<AttendSemesSubl> list,
                final String registerCd
        ) throws SQLException {

            final KenjaPS ps = ps();
            int updates = 0;

            for (final AttendSemesSubl subl : list) {

                int c = 1;
                ps.setString(c++, COPYCD);
                ps.setString(c++, header.getNendo().toString());
                ps.setString(c++, header.getMonthAsString());
                ps.setString(c++, getControlMaster().getCurrentSemester().getCodeAsString());
                ps.setString(c++, student.getCode());
                ps.setString(c++, String.valueOf(subl.getKintai().getAltCode()));
                ps.setString(c++, subl.getSublCd());
                ps.setInt(c++, subl.getCount());

                ps.setString(c++, registerCd);

                updates += super.executeUpdate();
            }
            return updates;
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
}
