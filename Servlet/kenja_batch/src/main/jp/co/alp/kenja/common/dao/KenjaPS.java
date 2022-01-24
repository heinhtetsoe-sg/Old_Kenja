// kanji=漢字
/*
 * $Id: KenjaPS.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/11/24 11:57:59 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;

/**
 * Integer,Short,KenjaDateImplをsetできる様にした PreparedStatement の実装。
 * @author tamura
 * @version $Id: KenjaPS.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class KenjaPS extends DelegatePS {
    /**
     * コンストラクタ。
     * @param ps ps
     */
    public KenjaPS(final PreparedStatement ps) {
        super(ps);
    }

    //========================================================================

    /**
     * {@inheritDoc}
     */
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scale) throws SQLException {
        if (x instanceof KenjaDateImpl) {
            setDate(parameterIndex, (KenjaDateImpl) x);
            return;
        }
        setObject(parameterIndex, x, targetSqlType, scale);
    }

    /**
     * {@inheritDoc}
     */
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        if (x instanceof KenjaDateImpl) {
            setDate(parameterIndex, (KenjaDateImpl) x);
            return;
        }
        setObject(parameterIndex, x, targetSqlType);
    }

    /**
     * {@inheritDoc}
     */
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        if (x instanceof KenjaDateImpl) {
            setDate(parameterIndex, (KenjaDateImpl) x);
            return;
        }
        setObject(parameterIndex, x);
    }

    //========================================================================

    /**
     * Shortを設定する
     * @param parameterIndex カラム位置
     * @param x 値。null可。
     * @throws SQLException SQL例外
     */
    public void setShort(final int parameterIndex, final Short x) throws SQLException {
        if (null == x) {
            setNull(parameterIndex, Types.SMALLINT);
        } else {
            setShort(parameterIndex, x.shortValue());
        }
    }

    /**
     * Integerを設定する
     * @param parameterIndex カラム位置
     * @param x 値。null可。
     * @throws SQLException SQL例外
     */
    public void setInt(final int parameterIndex, final Integer x) throws SQLException {
        if (null == x) {
            setNull(parameterIndex, Types.INTEGER);
        } else {
            setInt(parameterIndex, x.intValue());
        }
    }

    /**
     * KenjaDateImplを設定する。
     * @param parameterIndex カラム位置
     * @param x 値。null可。
     * @throws SQLException SQL例外
     */
    public void setDate(final int parameterIndex, final KenjaDateImpl x) throws SQLException {
        if (null == x) {
            setNull(parameterIndex, Types.DATE);
        } else {
            setDate(parameterIndex, x.getSQLDate());
        }
    }
} // KenjaPS

// eof
