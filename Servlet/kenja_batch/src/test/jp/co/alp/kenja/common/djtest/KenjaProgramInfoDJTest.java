// kanji=漢字
/*
 * $Id: KenjaProgramInfoDJTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/01/16 14:52:36 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.djtest;

import java.io.IOException;

import jp.co.alp.kenja.common.KenjaProgramInfo;

import jp.co.dgic.testing.framework.DJUnitTestCase;

public class KenjaProgramInfoDJTest extends DJUnitTestCase {
    KenjaProgramInfo _pinfo;

    public KenjaProgramInfoDJTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        _pinfo = new MyKenjaProgramInfo();
    }

    protected void tearDown() throws Exception {
        try {
            _pinfo = null;
        } finally {
            super.tearDown();
        }
    }

    public void testGetProgramName() {
        assertEquals("時間割作成", _pinfo.getProgramName());
    }

    public void testGetProgramId() {
        assertEquals("KNJB0040", _pinfo.getProgramId());
    }

    public void testGetProgramVersion() {
        assertEquals("まず一回", true, _pinfo.getProgramVersion().startsWith("version="));
        assertEquals("もう一回", true, _pinfo.getProgramVersion().startsWith("version="));
    }

    public void testGetProgramVersion_カバレッジ対策_その1() {
        addReturnNull("KenjaUtils", "getResourceAsURL");
        assertEquals("unknown", _pinfo.getProgramVersion());
    }

    public void testGetProgramVersion_カバレッジ対策_その2() {
        addReturnValue("URL", "openStream", new IOException("カバレッジ対策"));
        assertEquals("unknown", _pinfo.getProgramVersion());
    }
    //========================================================================

    public static class MyKenjaProgramInfo extends KenjaProgramInfo {
        public MyKenjaProgramInfo() {
            super();
        }

        public String getProgramName() {
            return "時間割作成";
        }

        public String getProgramId() {
            return "KNJB0040";
        }
    }
}
// eof
