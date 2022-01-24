// kanji=漢字
/*
 * $Id: QuietlyClosable.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/08/02 16:11:16 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.lang;

/**
 * 静かに閉じる。
 * @author tamura
 * @version $Id: QuietlyClosable.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public interface QuietlyClosable {
    /**
     * 静かに閉じる。
     */
    void closeQuietly();
} // QuietlyClosable

// eof
