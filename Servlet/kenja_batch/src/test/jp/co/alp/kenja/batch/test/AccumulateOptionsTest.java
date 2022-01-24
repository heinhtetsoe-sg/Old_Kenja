// kanji=漢字
/*
 * $Id: AccumulateOptionsTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2007/01/11 14:32:37 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.test;

import java.util.List;
import java.util.Properties;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Semester;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import jp.co.alp.kenja.common.lang.enums.MyEnum.Category;
import jp.co.alp.kenja.batch.accumulate.option.AccumulateOptions;
import jp.co.alp.kenja.batch.accumulate.option.Header;
import junit.framework.TestCase;

public class AccumulateOptionsTest extends TestCase {

    final static String[] _ArgsOfBaseday1 = {
            "dbname=taka",
            "dbhost=lion",
            "staffcd=takaesu",
            "baseday=1",
            "date=2004-09-28",
    };
    final static Properties _PropertiesOfSemester = new Properties();
    final static Properties _PropertiesOfMonth = new Properties();

    static {
        _PropertiesOfSemester.put("execute.unit", "semester");
        _PropertiesOfMonth.put("execute.unit", "month");

        final Category category = new MyEnum.Category();
        final KenjaDateImpl sDate = KenjaDateImpl.getInstance(2004, 9, 1);
        final KenjaDateImpl eDate = KenjaDateImpl.getInstance(2004, 12, 31);
        _Semester = Semester.create(category, 2, "2学期", sDate, eDate);
    }

    private static final Semester _Semester;

    public void testGetDate() throws Exception {
        final AccumulateOptions options = new AccumulateOptions(_ArgsOfBaseday1, _PropertiesOfSemester);
        final KenjaDateImpl date = KenjaDateImpl.getInstance(2004, 9, 28);
        assertEquals(date, options.getDate());
    }

    public void testGetKenjaParameter() throws Exception {
        final AccumulateOptions options = new AccumulateOptions(_ArgsOfBaseday1, _PropertiesOfSemester);
        assertNotNull(options.getKenjaParameters());
    }

    public void testGetHeaderList月単位BaseDay1() throws Exception {
        final AccumulateOptions options = new AccumulateOptions(_ArgsOfBaseday1, _PropertiesOfMonth);
        final List<Header> headList = options.getHeaderList(_Semester, 2004);
        assertEquals(1, headList.size());
        assertEquals("[2004-09-01(水) - 2004-09-28(火)], 2004", headList.get(0).toString());  // 月末(9月30日)とはならない
    }

    public void testGetHeaderList月単位BaseDay2() throws Exception {
        String[] args = {
            "dbname=taka",
            "dbhost=lion",
            "staffcd=takaesu",
            "baseday=2",
            "date=2004-09-28",
        };
        AccumulateOptions options;
        List<Header> headerList;

        // 2日から始まる
        options = new AccumulateOptions(args, _PropertiesOfMonth);
        headerList = options.getHeaderList(_Semester, 2004);
        assertEquals(1, headerList.size());
        assertEquals("[2004-09-02(木) - 2004-09-28(火)], 2004", headerList.get(0).toString());

        // 2日まで
        args[4] = "date=2004-09-02";
        options = new AccumulateOptions(args, _PropertiesOfMonth);
        headerList = options.getHeaderList(_Semester, 2004);
        assertEquals("[2004-09-02(木) - 2004-09-02(木)], 2004", headerList.get(0).toString());

        // 1日で終わる
        args[4] = "date=2004-10-01";
        options = new AccumulateOptions(args, _PropertiesOfMonth);
        headerList = options.getHeaderList(_Semester, 2004);
        assertEquals("[2004-09-02(木) - 2004-10-01(金)], 2004", headerList.get(0).toString());
    }

    public void test学期の範囲超え() throws Exception {
        final Category category = new MyEnum.Category();
        final KenjaDateImpl sDate = KenjaDateImpl.getInstance(2004, 9, 8);
        final KenjaDateImpl eDate = KenjaDateImpl.getInstance(2004, 12, 31);
        final Semester semester = Semester.create(category, 2, "2学期", sDate, eDate);

        AccumulateOptions options;
        List<Header> headerList;

        // 月単位
        options = new AccumulateOptions(_ArgsOfBaseday1, _PropertiesOfMonth);
        headerList = options.getHeaderList(semester, 2004);
        assertEquals("[2004-09-08(水) - 2004-09-28(火)], 2004", headerList.get(0).toString());

        // 学期単位
        String[] args = {
                "dbname=taka",
                "dbhost=lion",
                "staffcd=takaesu",
                "baseday=1",
                "date=2004-12-28",
        };
        options = new AccumulateOptions(args, _PropertiesOfSemester);
        headerList = options.getHeaderList(semester, 2004);
        assertEquals(4, headerList.size());   // 9月から12月は4ヶ月

        assertEquals("[2004-09-08(水) - 2004-09-30(木)], 2004", headerList.get(0).toString());
        assertEquals("[2004-10-01(金) - 2004-10-31(日)], 2004", headerList.get(1).toString());
        assertEquals("[2004-11-01(月) - 2004-11-30(火)], 2004", headerList.get(2).toString());
        assertEquals("[2004-12-01(水) - 2004-12-28(火)], 2004", headerList.get(3).toString());
    }

    public void testGetHeaderList() throws Exception {
        AccumulateOptions options;
        List<Header> resultList;

        // BaseDay=1, Unit=月
        options = new AccumulateOptions(_ArgsOfBaseday1, _PropertiesOfMonth);
        resultList = options.getHeaderList(_Semester, 2004);
        assertEquals(1, resultList.size());
        assertEquals("[2004-09-01(水) - 2004-09-28(火)], 2004", resultList.get(0).toString());

        // BaseDay=1, Unit=学期
        options = new AccumulateOptions(_ArgsOfBaseday1, _PropertiesOfSemester);
        resultList = options.getHeaderList(_Semester, 2004);
        assertEquals(1, resultList.size());
        assertEquals("[2004-09-01(水) - 2004-09-28(火)], 2004", resultList.get(0).toString());
    }
} // AccumulateOptionsTest

// eof
