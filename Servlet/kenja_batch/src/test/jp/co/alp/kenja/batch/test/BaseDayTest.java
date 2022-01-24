// kanji=漢字
/*
 * $Id: BaseDayTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2007/02/08 14:18:52 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.test;

import jp.co.alp.kenja.batch.accumulate.option.BaseDay;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import junit.framework.TestCase;

public class BaseDayTest extends TestCase {
    private final BaseDay _normal = BaseDay.getInstance(1);
    private final BaseDay _kindai = BaseDay.getInstance(2);

    private final KenjaDateImpl _１日 = KenjaDateImpl.getInstance(2006, 11, 1);
    private final KenjaDateImpl _２日 = KenjaDateImpl.getInstance(2006, 11, 2);
    private final KenjaDateImpl _普通 = KenjaDateImpl.getInstance(2006, 11, 10);
    private final KenjaDateImpl _月末 = KenjaDateImpl.getInstance(2006, 11, 30);

    public void testGetMonthAsStringNormal() throws Exception {
        assertEquals("11", _normal.getMonthAsString(_１日));
        assertEquals("11", _normal.getMonthAsString(_２日));
        assertEquals("11", _normal.getMonthAsString(_普通));
        assertEquals("11", _normal.getMonthAsString(_月末));
    }

    public void testGetMonthAsStringKindai() throws Exception {
        assertEquals("10", _kindai.getMonthAsString(_１日));  // 前月

        assertEquals("11", _kindai.getMonthAsString(_２日));
        assertEquals("11", _kindai.getMonthAsString(_普通));
        assertEquals("11", _kindai.getMonthAsString(_月末));
    }

    public void testGetTermNormal() throws Exception {
        assertEquals("[2006-11-01(水) - 2006-11-30(木)]", _normal.getTerm(_１日).toString());
        assertEquals("[2006-11-01(水) - 2006-11-30(木)]", _normal.getTerm(_２日).toString());
        assertEquals("[2006-11-01(水) - 2006-11-30(木)]", _normal.getTerm(_普通).toString());
        assertEquals("[2006-11-01(水) - 2006-11-30(木)]", _normal.getTerm(_月末).toString());
    }

    public void testGetTermKindai() throws Exception {
        assertEquals("[2006-11-02(木) - 2006-12-01(金)]", _kindai.getTerm(_１日).toString());
        assertEquals("[2006-11-02(木) - 2006-12-01(金)]", _kindai.getTerm(_２日).toString());
        assertEquals("[2006-11-02(木) - 2006-12-01(金)]", _kindai.getTerm(_普通).toString());
        assertEquals("[2006-11-02(木) - 2006-12-01(金)]", _kindai.getTerm(_月末).toString());
    }
} // BaseDayTest

// eof
