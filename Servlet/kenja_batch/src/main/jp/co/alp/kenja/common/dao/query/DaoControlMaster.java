// kanji=漢字
/*
 * $Id: DaoControlMaster.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/05 11:02:15 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.text.ParseException;
import java.util.Map;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.DbConnection;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.KenjaTableStatus;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import jp.co.alp.kenja.common.util.KenjaMapUtils;
import jp.co.alp.kenja.common.util.KenjaParameters;
import jp.co.alp.kenja.common.util.KenjaUtils;

/*
 * describe table CONTROL_MST
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * CTRL_NO                        SYSIBM    VARCHAR                   2     0 いいえ
 * CTRL_YEAR                      SYSIBM    VARCHAR                   4     0 はい
 * CTRL_SEMESTER                  SYSIBM    VARCHAR                   1     0 はい
 * CTRL_DATE                      SYSIBM    DATE                      4     0 はい
 * ATTEND_CTRL_DATE               SYSIBM    DATE                      4     0 はい
 * ATTEND_TERM                    SYSIBM    SMALLINT                  2     0 はい
 * IMAGEPATH                      SYSIBM    VARCHAR                  60     0 はい
 * EXTENSION                      SYSIBM    VARCHAR                   4     0 はい
 * MESSAGE                        SYSIBM    VARCHAR                2898     0 はい
 * PWDVALIDTERM                   SYSIBM    SMALLINT                  2     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     11 レコードが選択されました。
 */

/*
 * db2 describe table NAME_YDAT
 *
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * NAMECD1                        SYSIBM    VARCHAR                   4     0 いいえ
 * NAMECD2                        SYSIBM    VARCHAR                   4     0 いいえ
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     5 レコードが選択されました。
 */

/**
 * コントロール・マスタを読み込む。
 * @author tamura
 * @version $Id: DaoControlMaster.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoControlMaster {
    /** テーブル名 */
    public static final String TABLE_NAME = "CONTROL_MST";
    /** 現在処理年度 */
    public static final String CTRL_M_CTRL_YEAR = "ctrl_m.ctrl_year";

    private static final Log log = LogFactory.getLog(DaoControlMaster.class);
    private static final DaoControlMaster INSTANCE = new DaoControlMaster();

    private static final String CTRL_M_CTRL_SEME = "ctrl_m.ctrl_semester";
    private static final String CTRL_M_CTRL_DATE = "ctrl_m.ctrl_date";
    private static final String CTRL_M_CTRL_ATTD = "ctrl_m.attend_ctrl_date";

    protected final String _name = ClassUtils.getShortClassName(this.getClass());

    private MyEnum.Category _category;

    /*
     * コンストラクタ。
     */
    private DaoControlMaster() {
        super();
    }

    /**
     * インスタンスを得る。
     * @param category カテゴリー
     * @return インスタンス
     */
    public static DaoControlMaster getInstance(
            final MyEnum.Category category
    ) {
        INSTANCE._category = category;
        return INSTANCE;
    }

    private Object mapToInstance(final Map<String, Object> map) {
        try {
            return new ControlMaster(
                    _category,
                    MapUtils.getIntValue(map, "year", -1),
                    MapUtils.getIntValue(map, "semester", -1),
                    KenjaMapUtils.getKenjaDateImpl(map, "date"),
                    KenjaMapUtils.getKenjaDateImpl(map, "attend_date"),
                    MapUtils.getString(map, "subClassOrChair"),
                    MapUtils.getString(map, "attendTerm")
            );
        } catch (final NullPointerException e) {
            log.warn("インスタンス化失敗(NPE:" + e.getMessage() + ")" + _name + ":" + map);
        } catch (final IllegalArgumentException e) {
            log.warn("インスタンス化失敗(IArE:" + e.getMessage() + ")" + _name + ":" + map);
        }
        return null;
    }

    private ControlMaster loadFromDB(final Connection conn) throws SQLException {
        final String sql = "select"
                + "    int(CM.CTRL_YEAR) as year,"
                + "    int(CM.CTRL_SEMESTER) as semester,"
                + "    CM.CTRL_DATE as date,"
                + "    CM.ATTEND_CTRL_DATE as attend_date,"
                + "    (select"
                + "         distinct coalesce(max(NY.NAMECD2), '1')"
                + "       from NAME_YDAT as NY "
                + "       where CM.CTRL_YEAR = NY.YEAR and NY.NAMECD1 = 'C000') as subClassOrChair"
                + (KenjaTableStatus.getInstance().isValidControlMasterAttendanceTerm() ? " ,   CM.ATTEND_TERM as attendTerm " : "")
                + "  from " + TABLE_NAME + " as CM"
                + "  where"
                + "    CM.CTRL_NO = '01'"
                + KenjaUtils.LINE_SEPA
            ;

        final QueryRunner qr = new QueryRunner();
        final ResultSetHandler rsh = new MapHandler();
        Map map = null;
        try {
            map = (Map) qr.query(conn, sql, null, rsh);
            return (ControlMaster) mapToInstance(map);
        } finally {
            if (null != map) {
                map.clear();
            }
        }
    }

    private String loadAttendTerm(
            final Connection conn,
            final String strYear
    ) throws SQLException {
        final String sql = "select"
                + "    CM.ATTEND_TERM as attendTerm "
                + "  from " + TABLE_NAME + " as CM"
                + "  where"
                + "    CM.CTRL_NO = '01'"
                + KenjaUtils.LINE_SEPA
            ;

        final QueryRunner qr = new QueryRunner();
        final ResultSetHandler rsh = new ScalarHandler("attendTerm");
        final Integer attendTerm = (Integer) qr.query(conn, sql, null, rsh);
        return null == attendTerm ? null : attendTerm.toString();
    }

    private String loadSubClassOrChair(
            final Connection conn,
            final String strYear
    ) throws SQLException {
        final String sql = "select"
                + "    distinct coalesce(max(NY.NAMECD2), '1') as subClassOrChair"
                + "  from NAME_YDAT as NY "
                + "  where NY.YEAR = ?"
                + "  and   NY.NAMECD1 = 'C000'"
                + KenjaUtils.LINE_SEPA
            ;

        final QueryRunner qr = new QueryRunner();
        final ResultSetHandler rsh = new ScalarHandler("subClassOrChair");
        final String subClassOrChair = (String) qr.query(conn, sql, new String[] {strYear, }, rsh);
        return subClassOrChair;
    }

    private ControlMaster getFromParams(
            final KenjaParameters params,
            final Connection conn
    ) throws SQLException {
    check:
        {
            // 年
            final String strYear;
            final int year;
            try {
                strYear = params.getParameter(CTRL_M_CTRL_YEAR);
                if (StringUtils.isEmpty(strYear)) { break check; }
                year = Integer.parseInt(strYear);
            } catch (final NumberFormatException e) {
                break check;
            }

            // 学期
            final int seme;
            try {
                final String strSeme = params.getParameter(CTRL_M_CTRL_SEME);
                seme = Integer.parseInt(strSeme);
                if (StringUtils.isEmpty(strSeme)) { break check; }
            } catch (final NumberFormatException e) {
                break check;
            }

            // 制御日付
            final KenjaDateImpl ctrlDate;
            try {
                final String strCtrlDate = params.getParameter(CTRL_M_CTRL_DATE);
                if (StringUtils.isEmpty(strCtrlDate)) { break check; }
                ctrlDate = KenjaDateImpl.getInstance(strCtrlDate);
            } catch (final ParseException e) {
                break check;
            }

            // 出欠制御日付
            final KenjaDateImpl attdDate;
            try {
                final String strAttdDate = params.getParameter(CTRL_M_CTRL_ATTD);
                if (StringUtils.isEmpty(strAttdDate)) { break check; }
                attdDate = KenjaDateImpl.getInstance(strAttdDate);
            } catch (final ParseException e) {
                break check;
            }

            // 科目名or講座名
            final String subClassOrChair = loadSubClassOrChair(conn, strYear);

            // 入力期間
            String attendTerm = null;
            if (KenjaTableStatus.getInstance().isValidControlMasterAttendanceTerm()) {
                attendTerm = loadAttendTerm(conn, strYear);
            }
            
            // コントロール・マスタを生成
            return new ControlMaster(_category, year, seme, ctrlDate, attdDate, subClassOrChair, attendTerm);
        } // check:

        return null;
    }

    /**
     * コントロールマスタを、パラメータまたはデータベースから得る。
     * @param dbcon DB接続情報
     * @return コントロール・マスタ
     * @throws SQLException SQL例外
     */
    public ControlMaster load(final DbConnection dbcon) throws SQLException {
        Connection conn = null;
        try {
            conn = dbcon.getROConnection();
            try {
                // パラメータからコントロール・マスタを得る
                final ControlMaster cm = getFromParams(dbcon.getParameters(), conn);
                if (null != cm) {
                    log.fatal("コントロールマスタ:パラメータ:" + cm);
                    return cm;
                }
            } catch (final NullPointerException e) {
                log.warn("インスタンス化失敗(NPE:" + e.getMessage() + ")" + _name);
            } catch (final IllegalArgumentException e) {
                log.warn("インスタンス化失敗(IArE:" + e.getMessage() + ")" + _name);
            }

            // パラメータから得られないので、DBからコントロール・マスタを得る
            final ControlMaster cm = loadFromDB(conn);
            log.fatal("コントロールマスタ:DB:" + cm);
            return cm;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }
} // DaoControlMaster

// eof
