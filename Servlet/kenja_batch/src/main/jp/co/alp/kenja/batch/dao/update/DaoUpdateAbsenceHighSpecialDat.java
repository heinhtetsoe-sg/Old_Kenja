/*
 * $Id: DaoUpdateAbsenceHighSpecialDat.java 74552 2020-05-27 04:41:22Z maeshiro $
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
import jp.co.alp.kenja.batch.accumulate.SubClassGroupScheduleCounter;
import jp.co.alp.kenja.batch.accumulate.SubClassScheduleCounter;
import jp.co.alp.kenja.batch.accumulate.SubclassAbsenceHighSpecial;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.common.dao.KenjaPS;
import jp.co.alp.kenja.common.dao.update.AbstractDaoUpdator;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.lang.QuietlyClosableUtils;
import jp.co.alp.kenja.common.util.KenjaParameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 特別科目グループの欠課数上限値データを更新する。
 * @author maesiro
 * @version $Id: DaoUpdateAbsenceHighSpecialDat.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class DaoUpdateAbsenceHighSpecialDat {
    /** テーブル名 */
    public static final String TABLE_NAME = "SCHREG_ABSENCE_HIGH_SPECIAL_DAT";
    /*pkg*/static final Log log = LogFactory.getLog(DaoUpdateAbsenceHighSpecialDat.class);

    final Connection _conn;
    final ControlMaster _cm;
    final AccumulateOptions _options;
    final BatchSchoolMaster _schoolMaster;

    /**
     * コンストラクタ。
     * @param conn コネクション
     * @param cm コントロール・マスタ
     * @param options オプション
     * @param schoolMaster バッチ用学校マスタ
     */
    public DaoUpdateAbsenceHighSpecialDat(
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
     * @param sgsc 科目グループ毎の講座カウンタ
     * @throws SQLException SQL例外
     * @return 保存したレコード数
     */
    public int save(
            final SubClassGroupScheduleCounter sgsc
    ) throws SQLException {
        if (sgsc.isEmpty()) {
            return 0;
        }
        Insert dao = null;
        int count = 0;
        try {
            dao = new Insert(_conn, _cm, _options.getKenjaParameters());
            count = dao.insert(sgsc, _options.getStaffCd(), _schoolMaster);
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
                    + " SPECIAL_GROUP_CD,"
                    + " SCHREGNO,"
                    + " COMP_ABSENCE_HIGH,"
                    + " GET_ABSENCE_HIGH,"
                    + " LESSON,"
                    + " APPOINTED_DATE,"
                    + " REGISTERCD,"
                    + " UPDATED"
                    + ") values ("
                    + " ?,?,?,?,?,?,?,?,?,current timestamp"
                    + ")"
                ;
        }

        public int insert(
                final SubClassGroupScheduleCounter sgsc,
                final String registerCd,
                final BatchSchoolMaster sm
        ) throws SQLException {

            final int scale = sm.jougentiSansyutuhouIsJissu() ? 1 : 0;
            final int rounding = getRounding(sm);

            int i = 0;
            final SubClassScheduleCounter ssc = sgsc.getSubClassScheduleCounter();

            int lesson999 = 0;
            int mlesson999 = 0;

            final int risyuBunshiSpecial = sm.getRisyuBunshiSpecial();
            final int risyuBunboSpecial = sm.getRisyuBunboSpecial();
            final int syutokuBunshiSpecial = sm.getSyutokuBunshiSpecial();
            final int syutokuBunboSpecial = sm.getSyutokuBunboSpecial();

            for (final Iterator<String> it = sgsc.getSubClassGroupCodes().iterator(); it.hasNext();) {
                final String specialGroupCd = it.next();
                final SubclassAbsenceHighSpecial ah = sgsc.getSubclassAbsenceHighSpecial(specialGroupCd);

                final int lesson = ah.getLesson(sm.getJituJifunSpecial());
                final int mlesson = ah.getLessonMustBeAttended(sm.getJituJifunSpecial());
                lesson999 += lesson;
                mlesson999 += mlesson;
                final BigDecimal absenceHighRishu =
                    SubclassAbsenceHighSpecial.getAbsenceHighSpecial(mlesson, risyuBunshiSpecial, risyuBunboSpecial, scale, rounding);
                final BigDecimal absenceHighShutoku =
                    SubclassAbsenceHighSpecial.getAbsenceHighSpecial(mlesson, syutokuBunshiSpecial, syutokuBunboSpecial, scale, rounding);

                setPs(ps(), ssc, ah.getSpecialGroupCd(), absenceHighRishu, absenceHighShutoku, lesson, registerCd);
                i += super.executeUpdate();
            }

            final String specialGroupCd = "999";
            final BigDecimal absenceHighRishu =
                SubclassAbsenceHighSpecial.getAbsenceHighSpecial(mlesson999, risyuBunshiSpecial, risyuBunboSpecial, scale, rounding);
            final BigDecimal absenceHighShutoku =
                SubclassAbsenceHighSpecial.getAbsenceHighSpecial(mlesson999, syutokuBunshiSpecial, syutokuBunboSpecial, scale, rounding);

            setPs(ps(), ssc, specialGroupCd, absenceHighRishu, absenceHighShutoku, lesson999, registerCd);
            i += super.executeUpdate();

            return i;
        }

        private int getRounding(final BatchSchoolMaster sm) {
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
            return rounding;
        }

        private void setPs(final KenjaPS ps,
                final SubClassScheduleCounter ssc,
                final String specialGroupCd,
                final BigDecimal absenceHighRishu,
                final BigDecimal absenceHighShutoku,
                final int lesson,
                final String registerCd) throws SQLException {
            int c = 1;
            ps.setString(c++, ssc.getYearAsString());
            ps.setString(c++, ssc.getDiv());
            ps.setString(c++, specialGroupCd);
            ps.setString(c++, ssc.getStudent().getCode());
            ps.setBigDecimal(c++, absenceHighRishu);
            ps.setBigDecimal(c++, absenceHighShutoku);
            ps.setInt(c++, lesson);
            ps.setDate(c++, ssc.getDate());
            ps.setString(c++, registerCd);
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
