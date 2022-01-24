// kanji=漢字
/*
 * $Id: IGrade.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/10/21 15:18:49 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Collection;


/**
 * 学年のインタフェース。
 * @author tamura
 * @version $Id: IGrade.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public interface IGrade {
    /**
     * 学年コードを得る。
     * @return 学年コード
     */
    String getCode();

    /**
     * 年組の<code>Collection</code>を得る。
     * @return 年組の<code>Collection</code>
     */
    Collection<HomeRoom> getHomeRooms();

    /**
     * 講座の<code>Collection</code>を得る。
     * @return 講座の<code>Collection</code>
     */
    Collection<Chair> getChairs();
} // IGrade

// eof
