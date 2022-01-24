// kanji=漢字
/*
 * $Id: KenjaUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/05/11
 * 作成者: tamura
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 雑多なユーティリティクラス。
 * @author tamura
 * @version $Id: KenjaUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class KenjaUtils {
    /** 改行 */
    public static final String LINE_SEPA = System.getProperty("line.separator", "\n");

    private static final Log log = LogFactory.getLog(KenjaUtils.class);

    private KenjaUtils() {
    }

    /**
     * リソースファイルをURLとして得る。
     * @param clazz クラスローダーのためのオブジェクトのクラス
     * @param resourceName リソースファイル名
     * @return URL
     */
    public static URL getResourceAsURL(final Class<?> clazz, final String resourceName) {
//        assert null != clazz;
//        assert null != resourceName && 0 < resourceName.length();

        final ClassLoader loader = clazz.getClassLoader();
        if (null == loader) {
            log.error("loader is NULL");
            return null;
        }
        return loader.getResource(resourceName);
    }

    /**
     * <code>coll</code>に重複した要素があるか判定する。
     * @param coll コレクション
     * @return 重複があるなら<code>true</code>を返す
     */
    public static boolean isMixed(final Collection<?> coll) {
        if (1 == coll.size()) {
            return false;
        }
        final Set<?> set = new HashSet<Object>(coll);
        return 1 != set.size();
    }
} // KenjaUtils

// eof
