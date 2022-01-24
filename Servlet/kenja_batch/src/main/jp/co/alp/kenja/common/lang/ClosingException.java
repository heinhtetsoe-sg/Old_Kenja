// kanji=漢字
/*
 * $Id: ClosingException.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/08/02 17:24:46 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.lang;

import org.apache.commons.lang.exception.NestableException;

/**
 * 閉じる際の例外。
 * @author tamura
 * @version $Id: ClosingException.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public class ClosingException extends NestableException {
    /**
     * コンストラクタ。
     */
    public ClosingException() {
        super();
    }

    /**
     * コンストラクタ。
     * @param msg エラーメッセージ
     */
    public ClosingException(final String msg) {
        super(msg);
    }

    /**
     * コンストラクタ。
     * @param cause 他の例外
     */
    public ClosingException(final Throwable cause) {
        super(cause);
    }

    /**
     * コンストラクタ。
     * @param msg エラーメッセージ
     * @param cause 他の例外
     */
    public ClosingException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
} // ClosingException

// eof
