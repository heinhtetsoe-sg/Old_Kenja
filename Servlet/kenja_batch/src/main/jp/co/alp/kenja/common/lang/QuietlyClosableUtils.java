// kanji=漢字
/*
 * $Id: QuietlyClosableUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/08/02 16:26:09 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.lang;

import java.util.Collection;

/**
 * 静かに閉じるユーティリティクラス。
 * @author tamura
 * @version $Id: QuietlyClosableUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class QuietlyClosableUtils {
    /*
     * コンストラクタ。
     */
    private QuietlyClosableUtils() {
    }

    /**
     * オブジェクトを静かに閉じる。
     * @param obj 閉じるオブジェクト。
     */
    public static void closeQuietly(final QuietlyClosable obj) {
        if (null != obj) {
            obj.closeQuietly();
        }
    }

    /**
     * オブジェクトを静かに閉じる。
     * @param obj 閉じるオブジェクト。
     */
    public static void closeQuietly(final Closable obj) {
        if (null != obj) {
            try {
                obj.close();
            } catch (final ClosingException e) {
                ;
            }
        }
    }

    /**
     * 配列の各オブジェクトを静かに閉じる。
     * @param objs 閉じるオブジェクトの配列。
     */
    public static void closeQuietly(final QuietlyClosable[] objs) {
        if (null != objs) {
            for (int i = 0; i < objs.length; i++) {
                closeQuietly(objs[i]);
            }
        }
    }

    /**
     * 配列の各オブジェクトを静かに閉じる。
     * @param objs 閉じるオブジェクトの配列。
     */
    public static void closeQuietly(final Closable[] objs) {
        if (null != objs) {
            for (int i = 0; i < objs.length; i++) {
                closeQuietly(objs[i]);
            }
        }
    }

    /**
     * コレクションの各オブジェクトを静かに閉じる。
     * @param coll 閉じるオブジェクトのコレクション
     */
    public static void closeQuietly(final Collection<Object> coll) {
        if (null != coll && !coll.isEmpty()) {
            for (final Object obj : coll) {
                if (obj instanceof Closable) {
                    closeQuietly((Closable) obj);
                } else if (obj instanceof QuietlyClosable) {
                    closeQuietly((QuietlyClosable) obj);
                }
            }
        }
    }
} // QuietlyClosableUtils

// eof
