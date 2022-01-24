// kanji=漢字
/*
 * $Id: KenjaMapUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/07/06 10:13:40 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;

/**
 * 賢者のMapUtils。
 * {@link org.apache.commons.collections.MapUtils} を参考にした。
 * @author tamura
 * @version $Id: KenjaMapUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class KenjaMapUtils {
    /*
     * コンストラクタ。
     */
    private KenjaMapUtils() {
    }

    /**
     * mapからKenjaDateImplのインスタンスを得る。
     * @param map map
     * @param key キー
     * @return KenjaDateImplのインスタンス
     */
    public static KenjaDateImpl getKenjaDateImpl(final Map map, final Object key) {
        if (null == map) {
            return null;
        }

        final Object answer = map.get(key);
        if (null == answer) {
            return null;
        }

        KenjaDateImpl rtn = null;
        if (answer instanceof Date) {
            rtn = KenjaDateImpl.getInstance((Date) answer);
        } else if (answer instanceof Calendar) {
            rtn = KenjaDateImpl.getInstance((Calendar) answer);
        } else if (answer instanceof Long) {
            rtn = KenjaDateImpl.getInstance(((Long) answer).longValue());
        } else if (answer instanceof KenjaDateImpl) {
            rtn = (KenjaDateImpl) answer;
        }
        return rtn;
    }
} // KenjaMapUtils

// eof
