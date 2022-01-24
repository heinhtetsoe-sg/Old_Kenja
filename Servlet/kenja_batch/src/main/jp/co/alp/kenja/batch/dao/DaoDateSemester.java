// kanji=漢字
/*
 * $Id: DaoDateSemester.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2006/12/21 15:56:55 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.dao;

import java.util.List;
import java.util.Map;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.query.AbstractDaoLoader;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.util.KenjaUtils;

/*
 * describe table SEMESTER_MST
 *                                タイプ・
 * 列名                           スキーマ  タイプ名           長さ    位取り Null
 * ------------------------------ --------- ------------------ -------- ----- -----
 * YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
 * SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
 * SEMESTERNAME                   SYSIBM    VARCHAR                  15     0 はい
 * SDATE                          SYSIBM    DATE                      4     0 はい
 * EDATE                          SYSIBM    DATE                      4     0 はい
 * REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
 * UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
 *
 *     7 レコードが選択されました。
 */

/**
 * <<クラスの説明>>。
 * @author takaesu
 * @version $Id: DaoDateSemester.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class DaoDateSemester extends AbstractDaoLoader<Semester> {
    /** テーブル名 */
    public static final String TABLE_NAME = "SEMESTER_MST";

    /*pkg*/static final Log log = LogFactory.getLog(DaoDateSemester.class);

    private static final int THROUGH_THE_YEAR = 9;

    private final KenjaDateImpl _date;
    private int _year;
    private int _semester;

    /**
     * コンストラクタ。
     * @param date 日付
     */
    public DaoDateSemester(final KenjaDateImpl date) {
        super(log);
        _date = date;
    }

    /**
     * 年度を得る。
     * @return 年度
     */
    public int getYear() {
        return _year;
    }

    /**
     * 学期を得る。
     * @return 学期
     */
    public int getSemester() {
        return _semester;
    }

    /**
     * {@inheritDoc}
     */
    public String getQuerySql() {
        return "select"
                + "    YEAR as year,"
                + "    int(SEMESTER) as code,"
                + "    SEMESTERNAME as name,"
                + "    SDATE as sdate,"
                + "    EDATE as edate"
                + "  from " + TABLE_NAME
                + "  where"
                + "    ? between SDATE AND EDATE"
                + "  and SEMESTER <> '" + THROUGH_THE_YEAR + "'"
                ;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getQueryParams(final ControlMaster cm) {
        return new Object[] {
            _date.getSQLDate(),
        };
    }

    /*
     * ControlMaster を必要としない
     */
    private Object[] getQueryParams() {
        return getQueryParams(null);
    }

    /**
     * {@inheritDoc}
     */
    public Object mapToInstance(final Map<String, Object> map) {
        final String yearStr = MapUtils.getString(map, "year");
        _year = Integer.parseInt(yearStr);

        _semester = MapUtils.getIntValue(map, "code", -1);

        return yearStr;
    }

    /**
     * DBから読み込む。
     * @param conn コネクション
     * @throws SQLException 例外
     */
    public void load(final Connection conn) throws SQLException {
        _conn = conn;

        try {
            // loadの前処理
            preLoad();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
        }

        final long start = System.currentTimeMillis();

        final String sql = getQuerySql() + KenjaUtils.LINE_SEPA;
        final QueryRunner qr = new QueryRunner();
        final ResultSetHandler rsh = new MapListHandler();
        List list = null;
        try {
            list = (List) qr.query(conn, sql, getQueryParams(), rsh);

            // 「読み込み専用」ならコミットする。※パフォーマンス改善のため。
            if (_conn.isReadOnly()) {
                _conn.commit();
            }

            final int size = convert(list);
            final long elapsed = System.currentTimeMillis() - start;
            final String strElapsed = StringUtils.leftPad(String.valueOf(elapsed), 4) + "ミリ秒 :";
            if (0 < size) {
                log.fatal(strElapsed + StringUtils.leftPad(String.valueOf(size), 4) + "件:" + _name);
            } else {
                log.fatal(strElapsed + "ZERO件:" + _name);
            }

            // loadの後処理
            postLoad();
        } finally {
            if (null != list) {
                list.clear();
            }
        }
    }
} // DaoSemester

// eof
