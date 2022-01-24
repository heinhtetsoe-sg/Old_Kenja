// kanji=漢字
/*
 * $Id: TermTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/12/21 13:43:41 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.test;

import java.util.Iterator;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;

import jp.co.alp.kenja.batch.domain.Term;

import junit.framework.TestCase;

public class TermTest extends TestCase {

    final KenjaDateImpl _d20061110 = KenjaDateImpl.getInstance(2006, 11, 10);
    final KenjaDateImpl _d20061220 = KenjaDateImpl.getInstance(2006, 12, 20);

    Term _term;

    protected void setUp() throws Exception {
        super.setUp();
        _term = new Term(_d20061110, _d20061220);
    }

    protected void tearDown() throws Exception {
        _term = null;
    }

    public void testコンストラクタ() throws Exception {
        try {
            new Term(null, null);
            fail("例外が発生するので、ここには来ない");
        } catch (final Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    public void testIterator先頭の日() throws Exception {
        final Iterator<KenjaDateImpl> it = _term.iterator();
        assertEquals(_term.getSDate(), (KenjaDateImpl) it.next());  // 先頭は 2006-11-10
    }

    public void testIterator日数計算() throws Exception {
        int days = 0;

        KenjaDateImpl date = null;
        for (final Iterator<KenjaDateImpl> it = _term.iterator(); it.hasNext();) {
            days++;
            date = (KenjaDateImpl) it.next();
        }
        assertEquals(_term.getEDate(), date);   // 最後は 2006-12-20
        assertEquals(41, days);
    }

    public void testコンストラクタ_日付の大小_引数の順番() throws Exception {
        final Term term = new Term(_d20061220, _d20061110);
        assertEquals(_d20061110, term.getSDate());
        assertEquals(_d20061220, term.getEDate());
    }

    public void test同じ日付() throws Exception {
        final Term term = new Term(_d20061110, _d20061110);
        assertEquals(_d20061110, term.getSDate());
        assertEquals(_d20061110, term.getEDate());
    }
} // TermTest

// eof
