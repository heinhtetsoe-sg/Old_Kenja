// kanji=漢字
/*
 * $Id: ClassUtilsTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2005/11/15 13:51:14 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.util.test;

import junit.framework.TestCase;

public class ClassUtilsTest extends TestCase {

    public ClassUtilsTest(String name) {
        super(name);
    }

    public void testNewInstance() throws Exception {
        Object object = ClassUtils.newInstance(ClassUtils.class);
        assertEquals(ClassUtils.class, object.getClass());
    }

} // ClassUtilsTest

// eof
