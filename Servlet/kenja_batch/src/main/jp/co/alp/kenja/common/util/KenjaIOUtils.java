// kanji=漢字
/*
 * $Id: KenjaIOUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/05/11
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * I/Oに関するユーティリティクラス。
 * @author tamura
 * @version $Id: KenjaIOUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class KenjaIOUtils {
    private KenjaIOUtils() {
    }

    /**
     * 非nullなら、InputStream を閉じる。
     * nullなら、何もしない。
     * @param is InputStream,null可
     * @throws IOException 例外
     */
    public static void close(final InputStream is) throws IOException {
        if (null != is) {
            is.close();
        }
    }

    /**
     * 非nullなら、静かに InputStream を閉じる。
     * nullなら、何もしない。
     * @param is InputStream,null可
     */
    public static void closeQuietly(final InputStream is) {
        try {
            close(is);
        } catch (final IOException e) {
            ; // nothing
        }
    }

    /**
     * 非nullなら、Readerを閉じる。
     * nullなら、何もしない。
     * @param r Reader,null可
     * @throws IOException 例外
     */
    public static void close(final Reader r) throws IOException {
        if (null != r) {
            r.close();
        }
    }

    /**
     * 非nullなら、静かに、Readerを閉じる。
     * nullなら、何もしない。
     * @param r Reader,null可
     */
    public static void closeQuietly(final Reader r) {
        try {
            close(r);
        } catch (final IOException e) {
            ; // nothing
        }
    }
}


// eof
