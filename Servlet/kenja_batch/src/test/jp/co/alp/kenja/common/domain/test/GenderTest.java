// kanji=漢字
/*
 * $Id: GenderTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2006/02/02 20:15:40 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain.test;

import java.net.MalformedURLException;
import java.net.URL;

import java.awt.image.BufferedImage;

import jp.co.alp.kenja.common.domain.Gender;
import jp.co.alp.kenja.common.util.KenjaParameters;

import junit.framework.TestCase;

public class GenderTest extends TestCase {

    public GenderTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void test_MALE() {
        assertEquals("男性", Gender.MALE, Gender.getInstance("1"));
    }

    public void test_FEMALE() {
        assertEquals("女性", Gender.FEMALE, Gender.getInstance("2"));
    }

    public void test_UNKNOWN() {
        assertEquals("性別不詳", Gender.UNKNOWN, Gender.getInstance(null));
        assertEquals("性別不詳", Gender.UNKNOWN, Gender.getInstance("-"));
        assertEquals("性別不詳", Gender.UNKNOWN, Gender.getInstance("0"));
        assertEquals("性別不詳", Gender.UNKNOWN, Gender.getInstance("3"));
        assertEquals("性別不詳", Gender.UNKNOWN, Gender.getInstance("x"));
    }

    public void testGetCode() {
        assertEquals("男性", "1", Gender.MALE.getCode());
        assertEquals("女性", "2", Gender.FEMALE.getCode());
        assertEquals("性別不詳", "-", Gender.UNKNOWN.getCode());
    }

    public void testToString() {
        assertEquals("男性", Gender.MALE.toString());
        assertEquals("女性", Gender.FEMALE.toString());
        assertEquals("性別不詳", Gender.UNKNOWN.toString());
    }

    //========================================================================

    private static class MyParams extends KenjaParameters {
        private final boolean _urlIsNull;
        private final boolean _imageIsNull;

        public MyParams(
                final boolean urlIsNull,
                final boolean imageIsNull
        ) {
            _urlIsNull = urlIsNull;
            _imageIsNull = imageIsNull;
            this.getParameter("");
        }

        public MyParams() {
            this(false, false);
        }

        public String getParameter(String key) {
            return null;
        }

        public URL getResourceURL(String filename) {
            if (_urlIsNull) {
                return null;
            }
            try {
                return new URL(filename);
            } catch (MalformedURLException e) {
                return null;
            }
        }

    } // MyParams

    //========================================================================

    private static class MyImage extends BufferedImage {
        public MyImage() {
            super(1, 1, TYPE_INT_RGB);
        }
    } // MyImage
} // GenderTest

// eof
