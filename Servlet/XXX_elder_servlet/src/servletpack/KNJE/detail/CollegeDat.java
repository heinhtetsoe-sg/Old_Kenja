// kanji=漢字
/*
 * $Id: 542fc0c1587ad21f49b2b2ff1322724fb037f54c $
 *
 * 作成日: 2009/10/19 14:24:11 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE.detail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 542fc0c1587ad21f49b2b2ff1322724fb037f54c $
 */
public class CollegeDat {

    private static final Log log = LogFactory.getLog("CollegeDat.class");

    public String _schoolCd;
    public String _schoolName;
    public String _facultyCd;
    public String _facultyName;
    public String _departmentCd;
    public String _departmentName;
    public String _schoolTelno;

    /**
     * コンストラクタ。
     */
    public CollegeDat(
            final String schoolCd,
            final String schoolName,
            final String facultyCd,
            final String facultyName,
            final String departmentCd,
            final String departmentName,
            final String schoolTelno
    ) {
        _schoolCd = schoolCd;
        _schoolName = schoolName;
        _facultyCd = facultyCd;
        _facultyName = facultyName;
        _departmentCd = departmentCd;
        _departmentName = departmentName;
        _schoolTelno = schoolTelno;
    }
}

// eof
