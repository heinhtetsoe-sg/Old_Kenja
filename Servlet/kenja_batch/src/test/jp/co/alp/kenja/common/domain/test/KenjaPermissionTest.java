// kanji=漢字
/*
 * $Id: KenjaPermissionTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2005/07/19 14:45:06 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import jp.co.alp.kenja.common.domain.KenjaPermission;

import junit.framework.TestCase;

public class KenjaPermissionTest extends TestCase {
    public KenjaPermissionTest(String name) {
        super(name);
    }

    public void testIsWritable() {
        assertEquals(true,  KenjaPermission.WRITABLE.isWritable());
        assertEquals(false, KenjaPermission.RESTRICTED_WRITABLE.isWritable());
        assertEquals(false, KenjaPermission.READABLE.isWritable());
        assertEquals(false, KenjaPermission.RESTRICTED_READABLE.isWritable());
        assertEquals(false, KenjaPermission.DENIED.isWritable());
    }

    public void testIsRestrictedWritable() {
        assertEquals(false, KenjaPermission.WRITABLE.isRestrictedWritable());
        assertEquals(true,  KenjaPermission.RESTRICTED_WRITABLE.isRestrictedWritable());
        assertEquals(false, KenjaPermission.READABLE.isRestrictedWritable());
        assertEquals(false, KenjaPermission.RESTRICTED_READABLE.isRestrictedWritable());
        assertEquals(false, KenjaPermission.DENIED.isRestrictedWritable());
    }

    public void testIsReadable() {
        assertEquals(false, KenjaPermission.WRITABLE.isReadable());
        assertEquals(false, KenjaPermission.RESTRICTED_WRITABLE.isReadable());
        assertEquals(true,  KenjaPermission.READABLE.isReadable());
        assertEquals(false, KenjaPermission.RESTRICTED_READABLE.isReadable());
        assertEquals(false, KenjaPermission.DENIED.isReadable());
    }

    public void testIsRestrictedReadable() {
        assertEquals(false, KenjaPermission.WRITABLE.isRestrictedReadable());
        assertEquals(false, KenjaPermission.RESTRICTED_WRITABLE.isRestrictedReadable());
        assertEquals(false, KenjaPermission.READABLE.isRestrictedReadable());
        assertEquals(true,  KenjaPermission.RESTRICTED_READABLE.isRestrictedReadable());
        assertEquals(false, KenjaPermission.DENIED.isRestrictedReadable());
    }

    public void testIsDenied() {
        assertEquals(false, KenjaPermission.WRITABLE.isDenied());
        assertEquals(false, KenjaPermission.RESTRICTED_WRITABLE.isDenied());
        assertEquals(false, KenjaPermission.READABLE.isDenied());
        assertEquals(false, KenjaPermission.RESTRICTED_READABLE.isDenied());
        assertEquals(true,  KenjaPermission.DENIED.isDenied());
    }

    /*
     * String toString のテスト()
     */
    public void testToString() {
        assertEquals("0:更新可能",          KenjaPermission.WRITABLE.toString());
        assertEquals("1:更新可能制限付き",  KenjaPermission.RESTRICTED_WRITABLE.toString());
        assertEquals("2:参照可能",          KenjaPermission.READABLE.toString());
        assertEquals("3:参照可能制限付き",  KenjaPermission.RESTRICTED_READABLE.toString());
        assertEquals("9:権限なし",          KenjaPermission.DENIED.toString());
    }

    public void testGetInstance() {
        assertEquals(KenjaPermission.WRITABLE,              KenjaPermission.getInstance("0"));
        assertEquals(KenjaPermission.RESTRICTED_WRITABLE,   KenjaPermission.getInstance("1"));
        assertEquals(KenjaPermission.READABLE,              KenjaPermission.getInstance("2"));
        assertEquals(KenjaPermission.RESTRICTED_READABLE,   KenjaPermission.getInstance("3"));
        assertEquals(KenjaPermission.DENIED,                KenjaPermission.getInstance("9"));
        assertEquals(KenjaPermission.DENIED,                KenjaPermission.getInstance("X"));
    }

} // KenjaPermissionTest

// eof
