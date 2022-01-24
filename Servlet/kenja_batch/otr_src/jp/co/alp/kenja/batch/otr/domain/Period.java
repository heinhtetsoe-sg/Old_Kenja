// kanji=漢字
/*
 * $Id: Period.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2004/06/03 16:19:24 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 校時。
 * @version $Id: Period.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class Period implements Comparable {
    /** log */
    private static final Log log = LogFactory.getLog(Period.class);
    private static final Class MYCLASS = Period.class;
    public static final String LATEST_PERIOD_CODE = "Z";
    public static final Period LATEST_PERIOD = Period.create(LATEST_PERIOD_CODE, "全て");

    private static final int RADIX = 36;
    private static final int LIMIT_MIN = 0;
    private static final int LIMIT_MAX = 35;

    private final String _code;
    private final String _name;

    // 前の校時
    private Period _previous;
    // 次の校時
    private Period _next;

    /*
     * コンストラクタ。
     */
    private Period(
            final String code,
            final String name
    ) {
        _code = code;
        _name = name;
    }


    /**
     * 校時コードを得る。
     * @return 校時コード
     */
    public String getCode() {
        return _code;
    }

    /**
     * 校時名称を得る。
     * @return 校時名称
     */
    public String getName() {
        return _name;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "校時コード=[" + getCode() + "]、校時名称=[" + getName() + "]";
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Object obj) {
        final Period that = (Period) obj;
        return getCode().compareTo(that.getCode());
    }

    /**
     * なければ、校時のインスタンスを作成する。
     * すでに同じ校時コードのインスタンスがあれば、既存のインスタンスを返す。
     * @param code 校時コード("0","1","2"..."9","A","B"..."Z")
     * @param name 校時名称
     * @return 校時のインスタンス
     */
    public static Period create(
            final String code,
            final String name
    ) {
        final int codeint = Integer.parseInt(code, RADIX);
        if (codeint < LIMIT_MIN || LIMIT_MAX < codeint) { throw new IllegalArgumentException("引数が不正(code)"); }

        if (null == name)      { throw new IllegalArgumentException("引数が不正(name)"); }

        return new Period(code, name);
    }

    /**
     * 校時をロードする
     * @param db DB
     * @return periodMap 校時マップ
     * @exception SQLException SQL例外
     */
    public static Map load(final DB2UDB db) throws SQLException {

        final Map periodMap = new TreeMap();

        final String sql = " SELECT "
            + "  NAMECD2 AS CODE, "
            + "  NAME1 AS NAME "
            + " FROM NAME_MST "
            + " WHERE "
            + "  NAMECD1 = 'B001' ";

        db.query(sql);
        final ResultSet rs = db.getResultSet();
        log.debug("校時読み込み開始。");
        while (rs.next()) {
            final String code = rs.getString("CODE");
            final String name = rs.getString("NAME");

            final Period period = Period.create(code, name);
            periodMap.put(period.getCode(), period);
            log.debug(period);
        }
        log.debug("校時読み込み終了。");

        // 前後の講座の設定
        Period mappedLatestPeriod = null; 
        for (final Iterator it = periodMap.keySet().iterator(); it.hasNext();) {
            final String cdA = (String) it.next();
            final Period periodA = (Period) periodMap.get(cdA);
            final int pAcd = Integer.parseInt(periodA.getCode(), RADIX);
            if (mappedLatestPeriod == null || mappedLatestPeriod.isBefore(periodA)) { mappedLatestPeriod = periodA; }

            for (final Iterator it2 = periodMap.keySet().iterator(); it2.hasNext();) {
                final String cdB = (String) it2.next();
                final Period periodB = (Period) periodMap.get(cdB);
                final int pBcd = Integer.parseInt(periodB.getCode(), RADIX);

                if (pAcd == pBcd - 1) {
                    periodA.setNext(periodB);
                } else if (pAcd == pBcd + 1) {
                    periodA.setPrevious(periodB);
                }
            }
        }
        // プロパティーファイル中の最後の校時をLATEST_PERIODの前の校時にセットする。
        LATEST_PERIOD.setPrevious(mappedLatestPeriod);

//        for (final Iterator it = periodMap.keySet().iterator(); it.hasNext();) {
//            final String cdA = (String) it.next();
//            final Period periodA = (Period) periodMap.get(cdA);
//            log.debug(periodA + "近傍の校時 => " + periodA.getPrevious() + " , " + periodA.getNext());
//        }

        return periodMap;
    }

    /**
     * 次の校時を得る
     * @return 次の校時
     */
    public Period getNext() {
        return _next;
    }

    /**
     * 次の校時をセットする
     * @param next 次の校時
     */
    public void setNext(final Period next) {
        _next = next;
    }

    /**
     * 前の校時を得る
     * @return 前の校時
     */
    public Period getPrevious() {
        return _previous;
    }

    /**
     * 前の校時をセットする
     * @param previous 前の校時
     */
    public void setPrevious(final Period previous) {
        _previous = previous;
    }

    /**
     * 指定の校時は後か
     * @param period 校時
     * @return 指定の校時が後ならtrue、そうでなければfalse
     */
    public boolean isAfter(final Period period) {
        return Integer.valueOf(getCode(), RADIX).compareTo(Integer.valueOf(period.getCode(), RADIX)) > 0;
    }

    /**
     * 指定の校時は先か
     * @param period 校時
     * @return 指定の校時が先ならtrue、そうでなければfalse
     */
    public boolean isBefore(final Period period) {
        return Integer.valueOf(getCode(), RADIX).compareTo(Integer.valueOf(period.getCode(), RADIX)) < 0;
    }

} // Period

// eof
