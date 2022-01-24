// kanji=漢字
/*
 * $Id: DaoUpdateAttendAbsenceDat.java 75473 2020-07-16 07:19:19Z maeshiro $
 *
 * 作成日: 2006/12/25 16:21:38 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.update;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.accumulate.option.Header;
import jp.co.alp.kenja.batch.dao.AbstractDaoUpdateAttendDat;
import jp.co.alp.kenja.batch.domain.Term;
import jp.co.alp.kenja.common.dao.KenjaPS;
import jp.co.alp.kenja.common.dao.update.AbstractDaoUpdator;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.lang.QuietlyClosableUtils;
import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * 欠席データを更新する。
 * @author maesiro
 * @version $Id: DaoUpdateAttendAbsenceDat.java 75473 2020-07-16 07:19:19Z maeshiro $
 */
public class DaoUpdateAttendAbsenceDat extends AbstractDaoUpdateAttendDat {
    /** テーブル名 */
    public static final String TABLE_NAME = "ATTEND_ABSENCE_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoUpdateAttendAbsenceDat.class);

    /**
     * コンストラクタ。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param options オプション
     */
    public DaoUpdateAttendAbsenceDat(
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
            final Collection<KenjaDateImpl> absenceDates
    ) throws SQLException {
        if (absenceDates.isEmpty()) {
            return;
        }
        Insert dao = null;
        try {
            dao = new Insert(_conn, _cm, _options.getKenjaParameters());
            dao.insert(header, student, absenceDates, _options.getStaffCd());
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
        DeleteByDate daoDel = null;

        try {
            final String nendo = header.getNendo().toString();
            final Term term = header.getTerm();

            for (final String schoolKind : _options.getSchoolKind()) {
                daoDel = new DeleteByDate(_conn, _cm, _options.getKenjaParameters(), schoolKind);
                log.info(" sql = " + daoDel.getUpdateSql());
                final int delCount = daoDel.delete(nendo, term.getSDate(), term.getEDate());
                log.info("欠席データの削除件数: " + delCount + "件。");
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
                    + " SCHREGNO,"
                    + " ABSENCE_DATE,"
                    + " YEAR,"
                    + " SEMESTER,"
                    + " REGISTERCD,"
                    + " UPDATED"
                    + ") values ("
                    + " ?,?,?,?,?,current timestamp"
                    + ")"
                ;
        }

        public int insert(
                final Header header,
                final Student student,
                final Collection<KenjaDateImpl> absenceDates,
                final String registerCd
        ) throws SQLException {

            final KenjaPS ps = ps();
            int count = 0;

            for (final KenjaDateImpl absenceDate : absenceDates) {

                int c = 1;
                ps.setString(c++, student.getCode());
                ps.setDate(c++, absenceDate);
                ps.setString(c++, header.getNendo().toString());
                ps.setString(c++, getControlMaster().getCurrentSemester().getCodeAsString());
                ps.setString(c++, registerCd);

                count += super.executeUpdate();
            }
            return count;
        }
    } // Insert

    //========================================================================

    /**
     * 年月指定のDelete用。
     */
    private class DeleteByDate extends AbstractDaoUpdator {
        private final String _schoolKind;
        public DeleteByDate(
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
                    + " where YEAR = ?"
                    + "  and ABSENCE_DATE BETWEEN ? AND ?"
                    + (StringUtils.isEmpty(_schoolKind) ? "" : "  and SCHREGNO IN (SELECT I1.SCHREGNO FROM SCHREG_REGD_DAT I1 INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE WHERE I1.YEAR = ? AND I2.SCHOOL_KIND = '" + _schoolKind + "') ")
                ;
        }

        public int delete(final String nendo, final KenjaDateImpl sdate, final KenjaDateImpl edate) throws SQLException {
            final KenjaPS ps = ps();
            set(ps, nendo, sdate, edate);
            return super.executeUpdate();
        }

        private void set(
                final KenjaPS ps,
                final String nendo,
                final KenjaDateImpl sdate,
                final KenjaDateImpl edate
        ) throws SQLException {
            int c = 1;

            ps.setString(c++, nendo);
            ps.setDate(c++, sdate);
            ps.setDate(c++, edate);
            if (null != _schoolKind) {
                ps.setString(c++, nendo);
            }
        }
    } // DeleteByMonth
} // DaoUpdateAttendSemesDat

// eof
