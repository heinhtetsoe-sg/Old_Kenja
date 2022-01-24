package jp.co.alp.kenja.common.domain.test;

import java.util.Arrays;
import java.util.List;

import jp.co.alp.kenja.common.domain.CountFlagUtils;
import jp.co.alp.kenja.common.lang.enums.MyEnum;
import junit.framework.TestCase;

public class CountFlagUtilsTest extends TestCase {

    private static final MyEnum.Category C = new MyEnum.Category();

    private static final String まる = "○";
    private static final String 空白 = "";
    private static final String ばつ = "×";
    private static final String 三角 = "△";

    private static final String ON_ = "集計する";
    private static final String OFF = "集計しない";
    private static final String MIX = "一部集計する";

    private static final List<Boolean> BOOL_1_T_ = Arrays.asList(Boolean.TRUE);
    private static final List<Boolean> BOOL_1_F_ = Arrays.asList(Boolean.FALSE);
    
    private static final List<Boolean> BOOL_2_TT = Arrays.asList(Boolean.TRUE, Boolean.TRUE);
    private static final List<Boolean> BOOL_2_FF = Arrays.asList(Boolean.FALSE, Boolean.FALSE);
    private static final List<Boolean> BOOL_2_TF = Arrays.asList(Boolean.TRUE, Boolean.FALSE);

    public void test_booleanValue() throws Exception {
        // "0"の場合だけ、false
        assertEquals(false, CountFlagUtils.booleanValue("0"));

        // "0"以外なら true
        assertEquals(true, CountFlagUtils.booleanValue("1"));
        assertEquals(true, CountFlagUtils.booleanValue("2"));
        assertEquals(true, CountFlagUtils.booleanValue("11"));
        assertEquals(true, CountFlagUtils.booleanValue("xxx"));
        assertEquals(true, CountFlagUtils.booleanValue(null));
    }

}
