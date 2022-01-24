/*
 * $Id: DaoUpdateAbsenceHighDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2009/07/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao.update;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import jp.co.alp.kenja.batch.accumulate.BatchSchoolMaster;
import jp.co.alp.kenja.batch.accumulate.SubClassScheduleCounter;
import jp.co.alp.kenja.batch.accumulate.SubclassAbsenceHigh;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.common.dao.KenjaPS;
import jp.co.alp.kenja.common.dao.update.AbstractDaoUpdator;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.SubClass;
import jp.co.alp.kenja.common.lang.QuietlyClosableUtils;
import jp.co.alp.kenja.common.util.KenjaParameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 欠課数上限値データを更新する。。
 * @author maesiro
 * @version $Id: DaoUpdateAbsenceHighDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class DaoUpdateAbsenceHighDat {
    /** テーブル名 */
    public static final String TABLE_NAME = "SCHREG_ABSENCE_HIGH_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoUpdateAbsenceHighDat.class);

    protected final Connection _conn;
    protected final ControlMaster _cm;
    protected final AccumulateOptions _options;
    protected final BatchSchoolMaster _schoolMaster;

    /**
     * コンストラクタ。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param options オプション
     * @param schoolMaster バッチ用学校マスタ
     */
    public DaoUpdateAbsenceHighDat(
            final Connection conn,
            final ControlMaster cm,
            final AccumulateOptions options,
            final BatchSchoolMaster schoolMaster
    ) {
        _conn = conn;
        _cm = cm;
        _options = options;
        _schoolMaster = schoolMaster;
    }

    /**
     * 保存する。
     * @param ssc 科目毎の講座カウンタ
     * @throws SQLException SQL例外
     * @return 保存したレコード数
     */
    public int save(
            final SubClassScheduleCounter ssc
    ) throws SQLException {
        if (ssc.isEmpty()) {
            return 0;
        }
        Insert dao = null;
        int count = 0;
        try {
            dao = new Insert(_conn, _cm, _options.getKenjaParameters());
            count = dao.insert(ssc, _options.getStaffCd(), _schoolMaster);
        } finally {
            if (null != dao) {
                dao.closeQuietly();
            }
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteByYear(final String nendo) throws SQLException {
        DeleteByYear daoDel = null;

        try {
            daoDel = new DeleteByYear(_conn, _cm, _options.getKenjaParameters());
            final int delCount = daoDel.delete(nendo);
            log.info("削除件数: " + delCount + "件。");
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
                    + " YEAR,"
                    + " DIV,"
                    + " CLASSCD,"
                    + " SCHOOL_KIND,"
                    + " CURRICULUM_CD,"
                    + " SUBCLASSCD,"
                    + " SCHREGNO,"
                    + " COMP_ABSENCE_HIGH,"
                    + " GET_ABSENCE_HIGH,"
                    + " LESSON,"
                    + " APPOINTED_DATE,"
                    + " REGISTERCD,"
                    + " UPDATED"
                    + ") values ("
                    + " ?,?,?,?,?,?,?,?,?,?,?,?,current timestamp"
                    + ")"
                ;
        }

        public int insert(
                final SubClassScheduleCounter ssc,
                final String registerCd,
                final BatchSchoolMaster sm
        ) throws SQLException {

            final int scale = sm.jougentiSansyutuhouIsJissu() ? 1 : 0;
            final int rounding;

            if (sm.jougentiSansyutuhouIsSisyaGonyu()) {
                rounding = BigDecimal.ROUND_HALF_UP;
            } else if (sm.jougentiSansyutuhouIsKiriage()) {
                rounding = BigDecimal.ROUND_CEILING;
            } else if (sm.jougentiSansyutuhouIsJissu()) {
                rounding = BigDecimal.ROUND_HALF_UP;
            } else {
                rounding = BigDecimal.ROUND_FLOOR;
            }

            int i = 0;
            for (final Iterator<SubClass> it = ssc.getSubClasses().iterator(); it.hasNext();) {
                final SubClass subClass = it.next();
                final SubclassAbsenceHigh ah = ssc.getAbsenceHigh(subClass);

                final KenjaPS ps = ps();
                int c = 1;
                ps.setString(c++, ssc.getYearAsString());
                ps.setString(c++, ssc.getDiv());
                ps.setString(c++, subClass.getClassCd());
                ps.setString(c++, subClass.getSchoolKind());
                ps.setString(c++, subClass.getCurriculumCd());
                ps.setString(c++, subClass.getSubClassCd());
                ps.setString(c++, ssc.getStudent().getCode());
                ps.setBigDecimal(c++, ah.getAbsenceHigh(sm.getRisyuBunshi(), sm.getRisyuBunbo(), scale, rounding));
                ps.setBigDecimal(c++, ah.getAbsenceHigh(sm.getSyutokuBunshi(), sm.getSyutokuBunbo(), scale, rounding));
                ps.setInt(c++, ah.getLessonCount());
                ps.setDate(c++, ssc.getDate());
                ps.setString(c++, registerCd);
                i += super.executeUpdate();
            }

            return i;
        }
    } // Insert

    //========================================================================

    /**
     * 年月指定のDelete用。
     */
    private class DeleteByYear extends AbstractDaoUpdator {
        public DeleteByYear(
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
            return "delete from " + TABLE_NAME
                    + " where YEAR = ?"
                ;
        }

        public int delete(final String nendo) throws SQLException {
            final KenjaPS ps = ps();
            set(ps, nendo);
            return super.executeUpdate();
        }

        private void set(
                final KenjaPS ps,
                final String nendo
        ) throws SQLException {
            int c = 1;

            ps.setString(c++, nendo);
        }
    } // DeleteByYear
} // DaoUpdateAbsentLimitDat

// eof
