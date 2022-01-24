// kanji=漢字
/*
 * $Id: KenjaCommandLineParametersTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2004/07/21 18:17:36 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util.test;

import java.net.MalformedURLException;
import java.net.URL;

import java.awt.Image;

import jp.co.alp.kenja.common.KenjaProgramInfo;
import jp.co.alp.kenja.common.djtest.KenjaProgramInfoDJTest;
import jp.co.alp.kenja.common.domain.KenjaPermission;
import jp.co.alp.kenja.common.util.KenjaCommandLineParameters;

import junit.framework.TestCase;

public class KenjaCommandLineParametersTest extends TestCase {
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

    public KenjaCommandLineParametersTest(String name) {
        super(name);
    }

    protected void setUp() {
        _p = new KenjaCommandLineParameters(array);
    }

    protected void tearDown() {
        _p = null;
    }

    public void testToString() {
        String str = "{dbhost=127.0.0.1, dbname=junit, staffcd=12345678, key=value, key   with       spaces=value        with        spaces, =startsWithEQ, endsWithEQ=, duplexKey=two, SensitiveCase=YES, sensitivecase=yesyes, multipleEQ=valueA=valueB}";
        assertEquals(str, _p.toString());
    }

    public void testGetResourceURL() {
        assertNull(_p.getResourceURL("xxx"));
    }

    public void testGetDbHost() {
        assertEquals("127.0.0.1", _p.getDbHost());
    }

    public void testGetDbName() {
        assertEquals("junit", _p.getDbName());
    }

    public void testGetStaffCd() {
        assertEquals("12345678", _p.getStaffCd());
    }

    public void testGetStaffCdDefault() {
        final KenjaCommandLineParameters pppp = new KenjaCommandLineParameters(null);
        assertEquals("99999999", pppp.getStaffCd());
    }

    public void testGetParam() {
        assertEquals("127.0.0.1", _p.getParameter("dbhost"));
        assertEquals("junit", _p.getParameter("dbname"));
        assertEquals("12345678", _p.getParameter("staffcd"));
        assertEquals("value", _p.getParameter("key"));
    }

    public void testGetParamWithSpace() {
        assertEquals("value        with        spaces", _p.getParameter("key   with       spaces"));
    }

    public void testGetParamWithoutEQ() {
        assertNull(_p.getParameter("withoutEQ"));
    }

    public void testGetParamStartsWithEQ() {
        assertEquals("startsWithEQ", _p.getParameter(""));
    }

    public void testGetParamEndsWithEQ() {
        assertEquals("", _p.getParameter("endsWithEQ"));
    }

    public void testGetParamDuplexKey() {
        assertEquals("two", _p.getParameter("duplexKey"));
    }

    public void testGetParamCaseSensitive() {
        assertEquals("YES", _p.getParameter("SensitiveCase"));
        assertEquals("yesyes", _p.getParameter("sensitivecase"));
    }

    public void testGetParamMultipleEQ() {
        assertEquals("valueA=valueB", _p.getParameter("multipleEQ"));
    }

    public void testValidate() {
        assertNull(_p.validate());
    }

    public void testIsDebug() {
        assertEquals(false, _p.isDebug());
    }

    public void testIsDebugTrue() {
        final KenjaCommandLineParameters params = new KenjaCommandLineParameters(new String[] { "debug=debug", });
        assertEquals(true, params.isDebug());
    }

    public void testSetProgramInfo() {
        assertNull(_p.getProgramInfo());

        assertNotNull(_p.getProgramInfo());
        _p.setProgramInfo(null);
        assertNotNull(_p.getProgramInfo());
    }

    public void testGetProgramInfo() {
        assertNull(_p.getProgramInfo());

        assertNotNull(_p.getProgramInfo());
        assertEquals("時間割作成", _p.getProgramInfo().getProgramName());
        assertEquals("KNJB0040", _p.getProgramInfo().getProgramId());
        assertEquals("まず一回", true, _p.getProgramInfo().getProgramVersion().startsWith("version="));
        assertEquals("もう一回", true, _p.getProgramInfo().getProgramVersion().startsWith("version="));
    }

    public void testSetPermission() {
        assertEquals(KenjaPermission.DENIED, _p.getPermission());
        _p.setPermission(KenjaPermission.WRITABLE);

        assertNotNull(_p.getPermission());
        _p.setPermission(null);
        assertNotNull(_p.getPermission());
    }

    public void testGetPermission() {
        assertEquals(KenjaPermission.DENIED, _p.getPermission());
        _p.setPermission(KenjaPermission.WRITABLE);

        assertNotNull(_p.getPermission());
        assertEquals(KenjaPermission.WRITABLE, _p.getPermission());
    }

    public void testGetImage() throws MalformedURLException {
        final URL url = new URL("file:");

        final Image image = _p.getImage(url);
        assertNotNull(image);
        assertTrue(image instanceof Image);
    }
} // KenjaCommandLineParametersTest

// eof
