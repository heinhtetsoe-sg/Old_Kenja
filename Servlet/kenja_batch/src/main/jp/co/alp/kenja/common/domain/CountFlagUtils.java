// kanji=漢字
/*
 * $Id: CountFlagUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2010/06/16 13:46:24 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jp.co.alp.kenja.common.util.KenjaUtils;

import org.apache.commons.collections.CollectionUtils;

/**
 * カウントフラグ(集計フラグ)のユーティリティ・クラス。
 * @author tamura
 * @version $Id: CountFlagUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class CountFlagUtils {
    /** 集計フラグのデフォルト値 */
    public static final Boolean DEFAULT_COUNT_FLAG = Boolean.TRUE;

    private static final List<String> LIST_COUNT     = list(""  , "○", "集計する");
    private static final List<String> LIST_MIXED     = list("△", "△", "一部集計する");
    private static final List<String> LIST_NOT_COUNT = list("×", "×", "集計しない");

    private CountFlagUtils() {
        throw new Error();
    }

    private static List<String> list(final String s0, final String s1, final String s2) {
        return Collections.unmodifiableList(Arrays.asList(s0, s1, s2));
    }

    /**
     * 文字列表現の集計フラグを、二値化(「集計する」「集計しない」)する。
     * @param countFlag 文字列表現の集計フラグ
     * @return 集計するなら<code>true</code>
     */
    public static boolean booleanValue(final String countFlag) {
        // '0'以外=集計する。'0'=集計しない。
        return !"0".equals(countFlag);
    }

    /**
     * 集計フラグを「集計する」などの文字列に変換する。
     * @param countFlag 集計フラグ
     * @return 「"集計する"」「"集計しない"」のいずれか
     */
    public static String toString(final boolean countFlag) {
        return toString0(2, 2, countFlag);
    }

    private static String toString0(
            final int index0,
            final int index1,
            final Collection<Boolean> countFlags
    ) {
        if (null == countFlags || countFlags.isEmpty()) {
            return "-";
        }
        if (KenjaUtils.isMixed(countFlags)) {
            return LIST_MIXED.get(index0); // △:一部集計する
        }

        // assert countFlagが混在ではない。

        final Boolean c = (Boolean) CollectionUtils.get(countFlags, 0);
        if (!c.booleanValue()) {
            return LIST_NOT_COUNT.get(index0); // ×:集計しない
        }

        // assert countFlag 「集計する」のみ。

        if (index0 == index1) {

            // assert (index0 == index1) または (lessonMode は「授業のみ」)

            return LIST_COUNT.get(index0); // '':集計する(授業)
        }

        // assert lessonMode は「授業のみ」ではない

        return LIST_COUNT.get(index1); // ○:集計する(授業以外)
    }

    private static String toString0(
            final int index0,
            final int index1,
            final boolean countFlag
    ) {
        if (!countFlag) {
            return LIST_NOT_COUNT.get(index0); // ×:集計しない
        }
        if (index0 == index1) {
            return LIST_COUNT.get(index0); // '':集計する(授業)
        }
        return LIST_COUNT.get(index1); // ○:集計する(授業以外)
    }

} // CountFlagUtils
