// kanji=漢字
/*
 * $Id: KenjaExceptionTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/01/10 16:08:39 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.lang.test;

import jp.co.alp.kenja.common.lang.KenjaException;

import junit.framework.TestCase;

public class KenjaExceptionTest extends TestCase {
    public KenjaExceptionTest(String name) {
        super(name);
    }

    public void test_カバレッジ対策() {
        new KenjaException();
        new KenjaException("message");
        new KenjaException("message", new RuntimeException());
        new KenjaException(new RuntimeException());
    }
} // KenjaExceptionTest

// eof
