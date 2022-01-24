// kanji=漢字
/*
 * $Id: ChairsHolder.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2006/01/31 13:56:06 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Collection;

/**
 * 一つまたは複数の講座を持つインタフェース。
 * @author tamura
 * @version $Id: ChairsHolder.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public interface ChairsHolder {
    /**
     * このオブジェクトに属するすべての講座の<code>Collection</code>を得る。
     * @return <code>Collection&lt;Chair&gt;</code>
     */
    Collection<Chair> getChairs();
} // ChairsHolder

// eof
