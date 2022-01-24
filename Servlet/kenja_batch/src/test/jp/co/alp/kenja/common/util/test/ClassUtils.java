// kanji=漢字
/*
 * $Id: ClassUtils.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2005/11/15 13:44:16 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author tamura
 * @version $Id: ClassUtils.java 74557 2020-05-27 05:13:43Z maeshiro $
 */
public final class ClassUtils {
    private ClassUtils() {
    }

    /**
     * デフォルト（引数なし）コンストラクタでインスタンスを生成する。
     * コンストラクタは<code>private</code>でも可。
     * @param clazz クラス
     * @return 生成したインスタンス
     * @throws IllegalArgumentException 例外
     * @throws InstantiationException 例外
     * @throws IllegalAccessException 例外
     * @throws InvocationTargetException 例外
     */
    public static Object newInstance(final Class<?> clazz)
            throws  IllegalArgumentException,
                    InstantiationException,
                    IllegalAccessException,
                    InvocationTargetException
    {
        final Constructor<?>[] cs = clazz.getDeclaredConstructors();
        cs[0].setAccessible(true);
        return cs[0].newInstance(null);
    }
} // ClassUtils

// eof
