// kanji=漢字
/*
 * $Id: 7e0a63e5d808eeed257cdb2c474059122e46900f $
 *
 * 作成日: 2006/12/18
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  学校教育システム 賢者 [成績管理] 成績個人票
 */

public class KNJD152T extends KNJD151 {

    private static final Log log = LogFactory.getLog(KNJD152T.class);

    private static final String FORM_FILE = "KNJD152.frm";
    private static final String FORM_FILE2 = "KNJD152_2.frm";

    protected String setSvfForm(final Map paramap) { return ((String)paramap.get("DEVIATION") != null) ? FORM_FILE2 : FORM_FILE; }

    protected List setTestKindList(final Map paramap) {
        final List list = new ArrayList();
        if ((String) paramap.get("TESTKINDCD") == null) { return list; }

        list.add (setTestKindListDetail (paramap));
        return list;
    }

    protected List setRecordList(final Map paramap) {

        final List list = new ArrayList();
        list.add(setRecordFieldListDetail("SEM1_INTR_CHAIRCD","SEM1_INTR_SCORE","SEM1_INTR_VALUE","SEM1_INTR_VALUE_DI","1-0101"));  // １学期中間テスト
        list.add(setRecordFieldListDetail("SEM1_TERM_CHAIRCD","SEM1_TERM_SCORE","SEM1_TERM_VALUE","SEM1_TERM_VALUE_DI","1-0201"));  // １学期期末テスト
        list.add(setRecordFieldListDetail("SEM1_TERM2_CHAIRCD","SEM1_TERM2_SCORE","SEM1_TERM2_VALUE","SEM1_TERM2_VALUE_DI","1-0202"));  // １学期期末テスト２
        list.add(setRecordFieldListDetail(null,null,"SEM1_VALUE","SEM1_VALUE_DI","1-0000"));  // １学期評価

        final int s = Integer.parseInt((String)paramap.get("SEMESTER"));
        if (1 < s) {
            list.add(setRecordFieldListDetail("SEM2_INTR_CHAIRCD","SEM2_INTR_SCORE","SEM2_INTR_VALUE","SEM2_INTR_VALUE_DI","2-0101"));  // ２学期中間テスト
            list.add(setRecordFieldListDetail("SEM2_TERM_CHAIRCD","SEM2_TERM_SCORE","SEM2_TERM_VALUE","SEM2_TERM_VALUE_DI","2-0201"));  // ２学期期末テスト
            list.add(setRecordFieldListDetail(null,null,"SEM2_VALUE","SEM2_VALUE_DI","2-0000"));  // ２学期評価
        }
        if (2 < s && 2 < definecode.semesdiv) {
            list.add(setRecordFieldListDetail("SEM3_INTR_CHAIRCD","SEM3_INTR_SCORE","SEM3_INTR_VALUE","SEM3_INTR_VALUE_DI","3-0101"));  // ３学期中間テスト
            list.add(setRecordFieldListDetail("SEM3_TERM_CHAIRCD","SEM3_TERM_SCORE","SEM3_TERM_VALUE","SEM3_TERM_VALUE_DI","3-0201"));  // ３学期期末テスト
            list.add(setRecordFieldListDetail(null,null,"SEM3_VALUE","SEM3_VALUE_DI","3-0000"));  // ３学期評価
        }
        list.add(setRecordFieldListDetail(null,null,"GRAD_VALUE","GRAD_VALUE_DI","9-0000"));  // 学年評定

        return list;
    }
}
