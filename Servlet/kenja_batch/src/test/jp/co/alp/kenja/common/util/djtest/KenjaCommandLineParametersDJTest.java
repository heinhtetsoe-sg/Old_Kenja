// kanji=漢字
/*
 * $Id: KenjaCommandLineParametersDJTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2004/07/21 18:17:36 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util.djtest;

import jp.co.alp.kenja.common.util.KenjaCommandLineParameters;

import jp.co.dgic.testing.framework.DJUnitTestCase;

public class KenjaCommandLineParametersDJTest extends DJUnitTestCase {
    static String[] array = {
            "dbhost=127.0.0.1",
            "dbname=junit",
            "staffcd=12345678",
            "key=value",
            "      key   with       spaces     =      value        with        spaces   ",
            "withoutEQ",
            "=startsWithEQ",
            "endsWithEQ=",
            "duplexKey=one",
            "duplexKey=two",
            "SensitiveCase=YES",
            "sensitivecase=yesyes",
            "multipleEQ=valueA=valueB",
    };
    KenjaCommandLineParameters _p;

    public KenjaCommandLineParametersDJTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        addReturnValue("org.apache.commons.logging.Log", "isDebugEnabled", Boolean.TRUE);
        _p = new KenjaCommandLineParameters(array);
    }

    protected void tearDown() throws Exception {
        try {
            _p = null;
        } finally {
            super.tearDown();
        }
    }

    public void testValidateBadDbHost() {
        addReturnNull("jp.co.alp.kenja.common.util.KenjaParameters", "getDbHost");
        assertSame(IllegalArgumentException.class, _p.validate().getClass());
    }

    public void testValidateBadDbName() {
        addReturnNull("jp.co.alp.kenja.common.util.KenjaParameters", "getDbName");
        assertSame(IllegalArgumentException.class, _p.validate().getClass());
    }
} // KenjaCommandLineParametersDJTest

// eof
