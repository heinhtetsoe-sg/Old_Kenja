/*
 * $Id: AccumulateAdjusterTest.java 74557 2020-05-27 05:13:43Z maeshiro $
 *
 * 作成日: 2010/10/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.test;

import java.util.ArrayList;
import java.util.List;

import jp.co.alp.kenja.batch.accumulate.Attendance;
import jp.co.alp.kenja.batch.accumulate.KintaiManager;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import junit.framework.TestCase;

public class AccumulateAdjusterTest extends TestCase {
    
    final MyEnum.Category _category = new MyEnum.Category(); // アプリケーションで唯一のnew MyEnum.Category()
    KintaiManager _kintaiManager;
    List<Attendance> _list;
    
    public void setUp() throws Exception {
        _kintaiManager = createKintaiManager();
        _list = new ArrayList<Attendance>();
        _list.add(new Attendance(null, null, null, _kintaiManager.seated(), AccumulateTestConstants.BLANK_REMARK));
    }
    
    private KintaiManager createKintaiManager() {
        final KintaiManager manager = null;
        return manager;
    }

}
