// kanji=漢字
/*
 * $Id: DaoUpdateAttendDayDat.java 75473 2020-07-16 07:19:19Z maeshiro $
 *
 * 作成日: 2006/12/25 16:21:38 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.update;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.AttendDayDat;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.accumulate.option.Header;
import jp.co.alp.kenja.common.dao.KenjaPS;
import jp.co.alp.kenja.common.dao.SQLUtils;
import jp.co.alp.kenja.common.dao.update.AbstractDaoUpdator;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.lang.QuietlyClosableUtils;
import jp.co.alp.kenja.common.util.KenjaParameters;

/**
 * 日別データを更新する。
 * @author takaesu
 * @version $Id: DaoUpdateAttendDayDat.java 75473 2020-07-16 07:19:19Z maeshiro $
 */
public class DaoUpdateAttendDayDat {
    /** テーブル名 */
    public static final String TABLE_NAME = "ATTEND_DAY_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoUpdateAttendDayDat.class);

    final Connection _conn;
    final ControlMaster _cm;
    final AccumulateOptions _options;

    /**
     * コンストラクタ。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param options オプション
     */
    public DaoUpdateAttendDayDat(
            final Connection conn,
            final ControlMaster cm,
            final AccumulateOptions options
    ) {
        _conn = conn;
        _cm = cm;
        _options = options;
    }

    /**
     * 保存する。
     * @param adc 日別カウンタ
     * @throws SQLException SQL例外
     */
    public void save(
            final Header header,
            final Student student,
            final List<AttendDayDat> list
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
            final KenjaDateImpl sdate = header.getTerm().getSDate();
            final KenjaDateImpl edate = header.getTerm().getEDate();

            for (final String schoolKind : _options.getSchoolKind()) {
                daoDel = new DeleteByMonth(_conn, _cm, _options.getKenjaParameters(), schoolKind, _options.getAttendDayDatUpdateRegisterCd());
                log.info(" sql = " + daoDel.getUpdateSql());
                final int delCount = daoDel.delete(nendo, sdate, edate);
                log.info("日別の削除件数: " + delCount + "件。");
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
                    + " ATTENDDATE,"
                    + " DI_CD,"
                    + " DI_REMARK,"
                    + " YEAR,"
                    + " REGISTERCD,"
                    + " UPDATED"
                    + ") values ("
                    + " ?,?,?,?,?,?,current timestamp"
                    + ")"
                ;
        }

        public int insert(
                final Header header,
                final Student student,
                final List<AttendDayDat> list,
                final String registerCd
        ) throws SQLException {
            int count = 0;
            for (final AttendDayDat add : list) {
                final KenjaPS ps = ps();
                int c = 1;
                ps.setString(c++, student.getCode());
                ps.setDate(c++, add.getDate());
                ps.setString(c++, add.getKintai().getAltCode().toString());
                ps.setString(c++, add.getRemark());
                ps.setString(c++, header.getNendo().toString());
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
    private class DeleteByMonth extends AbstractDaoUpdator {
        private final String _schoolKind;
        private final String[] _updateRegistercd;
        public DeleteByMonth(
                final Connection conn,
                final ControlMaster cm,
                final KenjaParameters params,
                final String schoolKind,
                final String[] updateRegistercd
        ) {
            super(log, conn, cm, params);
            _schoolKind = schoolKind;
            _updateRegistercd = updateRegistercd;
        }

        /**
         * {@inheritDoc}
         */
        public String getUpdateSql() {
            return "delete from " + TABLE_NAME
                    + " where YEAR = ?"
                    + "  and ATTENDDATE between ? and ?"
                    + (StringUtils.isEmpty(_schoolKind) ? "" : "  and SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT I1 INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE WHERE I1.YEAR = ? AND I2.SCHOOL_KIND = '" + _schoolKind + "') ")
                    + (ArrayUtils.isEmpty(_updateRegistercd) ? "" : "  and REGISTERCD IN " + SQLUtils.whereIn(true, _updateRegistercd))
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
            if (!StringUtils.isEmpty(_schoolKind)) {
                ps.setString(c++, nendo);
            }
        }
    } // DeleteByMonth
} // DaoUpdateAttendSemesDat

// eof
