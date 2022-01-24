// kanji=漢字
/*
 * $Id: 0f9620ed7996d2ff140c7010c2f88f62ffb86759 $
 *
 * 作成日: 2007/05/28 15:10:59 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail.test;

import servletpack.KNJZ.detail.KNJServletUtils;
import junit.framework.TestCase;

public class KNJServletUtilsTest extends TestCase {
    private static final double DELTA = 0.01;

    public void test四捨五入() throws Exception {
        final double value = KNJServletUtils.roundHalfUp(1.234, 0);
        assertEquals(1.0, value, DELTA);
        assertEquals(1, value, DELTA);

        assertEquals(1.23, KNJServletUtils.roundHalfUp(1.234, 2), DELTA);
        assertEquals(1.35, KNJServletUtils.roundHalfUp(1.345, 2), DELTA);

        assertEquals(1.46, KNJServletUtils.roundHalfUp(1.456789, 2), DELTA);
        assertEquals(1.457, KNJServletUtils.roundHalfUp(1.456789, 3), DELTA);

        assertEquals(2, KNJServletUtils.roundHalfUp(1.99999, 3), DELTA);
    }
} // KNJServletUtilsTest

// eof
