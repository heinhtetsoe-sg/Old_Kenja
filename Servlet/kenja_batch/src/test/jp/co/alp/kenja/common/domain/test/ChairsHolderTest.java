// kanji=漢字
/*
 * $Id: ChairsHolderTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/01/31 15:43:23 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import org.apache.commons.lang.ClassUtils;

import jp.co.alp.kenja.common.domain.Chair;
import jp.co.alp.kenja.common.domain.ChairsHolder;
import jp.co.alp.kenja.common.domain.GroupClass;

import junit.framework.TestCase;

public class ChairsHolderTest extends TestCase {

    public ChairsHolderTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void test_Chair() {
        assertTrue(ClassUtils.isAssignable(Chair.class, ChairsHolder.class));
    }

    public void test_GroupClass() {
        assertTrue(ClassUtils.isAssignable(GroupClass.class, ChairsHolder.class));
    }

} // ChairsHolderTest

// eof
