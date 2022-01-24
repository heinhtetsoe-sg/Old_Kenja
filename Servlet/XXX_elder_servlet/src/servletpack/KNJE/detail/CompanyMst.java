// kanji=漢字
/*
 * $Id: 1e092dcab1eafaa11d1813b74f8403cb3095b265 $
 *
 * 作成日: 2009/10/19 14:24:48 - JST
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
 * @version $Id: 1e092dcab1eafaa11d1813b74f8403cb3095b265 $
 */
public class CompanyMst {

    private static final Log log = LogFactory.getLog("CompanyMst.class");

    public String _companyCd;
    public String _companyName;
    public String _shushokuAddr;
    public String _shihonkin;
    public String _soninzu;
    public String _toninzu;
    public String _industryLcd;
    public String _industryLname;
    public String _industryMcd;
    public String _industryMname;
    public String _companySort;
    public String _targetSex;
    public String _zipcd;
    public String _addr1;
    public String _addr2;
    public String _telno;
    public String _remark;

    public CompanyMst(
            final String companyCd,
            final String companyName,
            final String shushokuAddr,
            final String shihonkin,
            final String soninzu,
            final String toninzu,
            final String industryLcd,
            final String industryLname,
            final String industryMcd,
            final String industryMname,
            final String companySort,
            final String targetSex,
            final String zipcd,
            final String addr1,
            final String addr2,
            final String telno,
            final String remark
    ) {
        _companyCd = companyCd;
        _companyName = companyName;
        _shushokuAddr = shushokuAddr;
        _shihonkin = shihonkin;
        _soninzu = soninzu;
        _toninzu = toninzu;
        _industryLcd = industryLcd;
        _industryLname = industryLname;
        _industryMcd = industryMcd;
        _industryMname = industryMname;
        _companySort = companySort;
        _targetSex = targetSex;
        _zipcd = zipcd;
        _addr1 = addr1;
        _addr2 = addr2;
        _telno = telno;
        _remark = remark;
    }
}

// eof
