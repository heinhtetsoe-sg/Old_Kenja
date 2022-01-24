// kanji=漢字
/*
 * $Id: KenjaPermission.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/02/25 15:38:45 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import org.apache.commons.lang.enums.Enum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 権限の区分。
 * @author tamura
 * @version $Id: KenjaPermission.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class KenjaPermission extends Enum {
    /** 更新可能(0,DEF_UPDATABLE) */
    public static final KenjaPermission WRITABLE            = new KenjaPermission("0", "更新可能");
    /** 更新可能制限付き(1,UPDATE_RESTRICT) */
    public static final KenjaPermission RESTRICTED_WRITABLE = new KenjaPermission("1", "更新可能制限付き");
    /** 参照可能(2,REFERER) */
    public static final KenjaPermission READABLE            = new KenjaPermission("2", "参照可能");
    /** 参照可能制限付き(3,REFERER_RESTRICT) */
    public static final KenjaPermission RESTRICTED_READABLE = new KenjaPermission("3", "参照可能制限付き");
    /** 権限なし(9,NO_AUTH) */
    public static final KenjaPermission DENIED              = new KenjaPermission("9", "権限なし");

    private static final Log log = LogFactory.getLog(KenjaPermission.class);
    private static final Class<KenjaPermission> MYCLASS = KenjaPermission.class;

    private final String _desc;

    /*
     * コンストラクタ。
     * @param code 権限の区分のコード("0","1","2"...)
     * @param desc 説明
     */
    private KenjaPermission(final String code, final String desc) {
        super(code);
        _desc = desc;
    }

    /**
     * 更新可能か判定する。
     * @return 可能なら<code>true</code>を返す
     */
    public boolean isWritable() {
        return this.equals(WRITABLE);
    }

    /**
     * 更新可能制限付きか判定する。
     * @return 可能なら<code>true</code>を返す
     */
    public boolean isRestrictedWritable() {
        return this.equals(RESTRICTED_WRITABLE);
    }

    /**
     * 参照可能か判定する。
     * @return 可能なら<code>true</code>を返す
     */
    public boolean isReadable() {
        return this.equals(READABLE);
    }

    /**
     * 参照可能制限付きか判定する。
     * @return 可能なら<code>true</code>を返す
     */
    public boolean isRestrictedReadable() {
        return this.equals(RESTRICTED_READABLE);
    }

    /**
     * 権限なしか判定する。
     * @return 権限なしなら<code>true</code>を返す
     */
    public boolean isDenied() {
        return this.equals(DENIED);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return getName() + ":" + _desc;
    }

    /**
     * 権限の区分のインスタンスを得る。
     * @param code 権限の区分のコード("0","1","2"...)
     * @return 権限の区分のインスタンス
     */
    public static KenjaPermission getInstance(final String code) {
        final KenjaPermission perm = (KenjaPermission) Enum.getEnum(KenjaPermission.MYCLASS, code);
        if (null == perm) {
            log.warn("不明なコード(" + code + ")");
            return KenjaPermission.DENIED;
        }
        return perm;
    }
} // KenjaPermission

// eof
