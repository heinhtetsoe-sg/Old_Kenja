// kanji=漢字
/*
 * $Id: Closable.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/08/02 15:55:54 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.lang;

/**
 * 閉じる。
 * @author tamura
 * @version $Id: Closable.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public interface Closable {
    /**
     * 閉じる。
     * @throws ClosingException 例外
     */
    void close() throws ClosingException;
} // Closable

// eof
