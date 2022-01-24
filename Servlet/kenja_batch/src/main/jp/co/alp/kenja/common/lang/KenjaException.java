// kanji=漢字
/*
 * $Id: KenjaException.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/10/05 15:13:11 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.lang;

import org.apache.commons.lang.exception.NestableException;

/**
 * 賢者アプリ用の例外ベースクラス。
 * @author tamura
 * @version $Id: KenjaException.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class KenjaException extends NestableException {
    /**
     * コンストラクタ。
     */
    public KenjaException() {
        super();
    }

    /**
     * コンストラクタ。
     * @param msg 詳細メッセージ
     */
    public KenjaException(final String msg) {
        super(msg);
    }

    /**
     * コンストラクタ。
     * @param cause 原因となる例外
     */
    public KenjaException(final Throwable cause) {
        super(cause);
    }

    /**
     * コンストラクタ。
     * @param msg 詳細メッセージ
     * @param cause 原因となる例外
     */
    public KenjaException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
} // KenjaException

// eof
