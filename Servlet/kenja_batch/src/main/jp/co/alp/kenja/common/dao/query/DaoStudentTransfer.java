// kanji=漢字
/*
 * $Id: DaoStudentTransfer.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/11/25 14:57:57 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.util.KenjaMapUtils;

/*
 * db2 describe table SCHREG_TRANSFER_DAT
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
 * TRANSFERCD                     SYSIBM    VARCHAR                   2     0 いいえ
 * TRANSFER_SDATE                 SYSIBM    DATE                      4     0 いいえ
 * TRANSFER_EDATE                 SYSIBM    DATE                      4     0 はい
 * TRANSFERREASON                 SYSIBM    VARCHAR                  75     0 はい
 * TRANSFERPLACE                  SYSIBM    VARCHAR                  60     0 はい
 * TRANSFERADDR                   SYSIBM    VARCHAR                  75     0 はい
 * ABROAD_CLASSDAYS               SYSIBM    SMALLINT                  2     0 はい
 * ABROAD_CREDITS                 SYSIBM    SMALLINT                  2     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     11 レコードが選択されました。
 */

/**
 * 学籍異動データを読み込む。
 * @author tamura
 * @version $Id: DaoStudentTransfer.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoStudentTransfer extends AbstractDaoLoader<Student.Transfer> {
    /** テーブル名 */
    public static final String TABLE_NAME = "SCHREG_TRANSFER_DAT";

    private static final Log log = LogFactory.getLog(DaoStudentTransfer.class);
    private static final AbstractDaoLoader<Student.Transfer> INSTANCE = new DaoStudentTransfer();

    /*
     * コンストラクタ。
     */
    private DaoStudentTransfer() {
        super(log);
    }

    /**
     * インスタンスを得る。
     * @return インスタンス
     */
    public static AbstractDaoLoader<Student.Transfer> getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final Student std = Student.getInstance(_cm.getCategory(), MapUtils.getString(map, "code"));
        if (null == std) {
            return "不明な学籍番号(code)";
        }

        final Student.TransferCd tcd = Student.TransferCd.getInstance(MapUtils.getIntValue(map, "transferCd"));
        if (null == tcd) {
            return "不明な異動区分(transferCd)";
        }

        final KenjaDateImpl sdate = KenjaMapUtils.getKenjaDateImpl(map, "sdate");
        if (null == sdate) {
            return "不明な開始日付(sdate)";
        }

        final KenjaDateImpl edate = KenjaMapUtils.getKenjaDateImpl(map, "edate");
        if (Student.TransferCd.TRANSFER != tcd && null == edate) {
            return "不明な終了日付(edate)";
        }

        std.addTransfer(tcd, sdate, edate);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    t1.SCHREGNO as code,"
                + "    coalesce(int(t1.TRANSFERCD), 0) as transferCd,"
                + "    t1.TRANSFER_SDATE as sdate,"
                + "    t1.TRANSFER_EDATE as edate"
                + "  from " + TABLE_NAME + " t1"
                + "    inner join " + DaoStudent.TABLE_NAME2 + " t2"
                + "      on t1.SCHREGNO = t2.SCHREGNO"
                + "  where t2.YEAR = ?"
                + "    and t2.SEMESTER = ?"
                + "    and ("
                + "      (t1.TRANSFERCD in ('1','2','3') and ? <= t1.TRANSFER_EDATE and t1.TRANSFER_SDATE <= ?)"
                + "     or "
                + "      (t1.TRANSFERCD in ('4') and t1.TRANSFER_SDATE <= ?)"
                + "    )"
                + "  order by"
                + "    t1.SCHREGNO,"
                + "    t1.TRANSFER_SDATE"
                ;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        final java.sql.Date s = cm.getCurrentSemester().getSDate().getSQLDate();
        final java.sql.Date e = cm.getCurrentSemester().getEDate().getSQLDate();

        return new Object[] {
            cm.getCurrentYearAsString(),
            cm.getCurrentSemester().getCodeAsString(),
            s, e,
            e,
        };
    }
} // DaoStudentTransfer

// eof
