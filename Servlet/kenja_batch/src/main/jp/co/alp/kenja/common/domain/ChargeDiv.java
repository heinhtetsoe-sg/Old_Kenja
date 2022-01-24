// kanji=漢字
/*
 * $Id: ChargeDiv.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/06/08 15:35:45 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.ValuedEnum;


/**
 * 講座担当職員の担任区分。
 * @author tamura
 * @version $Id: ChargeDiv.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class ChargeDiv extends ValuedEnum {
    /** 正担任 */
    public static final ChargeDiv REGULAR = new ChargeDiv("正担任", 1);

    /** 副担任 */
    public static final ChargeDiv VICE = new ChargeDiv("副担任", 0);

    private static final Class<ChargeDiv> MYCLASS = ChargeDiv.class;

    private final String _name;
    private final String _str;

    private ChargeDiv(
            final String name,
            final int code
    ) {
        super(name, code);
//        if (null == name) {
//            throw new IllegalArgumentException("引数が不正");
//        }
        _name = name;
        _str = code + ":" + _name;
    }

    /**
     * 担任区分コードを得る。
     * @return 担任区分コード
     */
    public int getCode() {
        return getValue();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _str;
    }

    /**
     * 担任区分コードから、担任区分を得る。
     * @param code 担任区分コード
     * @return 担任区分
     */
    public static ChargeDiv getInstance(final int code) {
        return (ChargeDiv) getEnum(MYCLASS, code);
    }

    /**
     * 担任区分の列挙のListを得る。
     * @return <code>List&lt;ChargeDiv&gt;</code>
     */
    public static List<ChargeDiv> getEnumList() {
        return getEnumList(MYCLASS);
    }

    /**
     * 担任区分の列挙のMapを得る。
     * @return <code>Map&lt;担任区分コード, ChargeDiv&gt;</code>
     */
    public static Map<Integer, ChargeDiv> getEnumMap() {
        return getEnumMap(MYCLASS);
    }

    /**
     * 担任区分の数を得る。
     * @return 担任区分の数
     */
    public static int size() {
        return getEnumList().size();
    }
} // ChargeDiv

// eof
