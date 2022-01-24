// kanji=漢字
/*
 * $Id: Gender.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2006/02/02 17:03:09 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.awt.Image;

import org.apache.commons.lang.enums.Enum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 性別。
 * @author tamura
 * @version $Id: Gender.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class Gender extends Enum {
    /** 1:男性 */
    public static final Gender MALE = new Gender("1", "男性", "gender.male.image");
    /** 2:女性 */
    public static final Gender FEMALE = new Gender("2", "女性", "gender.female.image");
    /** -:性別不詳 */
    public static final Gender UNKNOWN = new Gender("-", "性別不詳", "gender.unknown.image");

    /*pkg*/static final Class<Gender> MYCLASS = Gender.class;
    /*pkg*/static final Log log = LogFactory.getLog(Gender.class);

    private final String _desc;
    private final String _imageKey;
    private Image _image;

    /*
     * コンストラクタ。
     * @param code 性別コード
     * @param desc 文字列
     */
    private Gender(
            final String code,
            final String desc,
            final String imageKey
    ) {
        super(code);
        _desc = desc;
        _imageKey = imageKey;
    }

    /**
     * 性別コードを得る。
     * @return 性別コード
     */
    public String getCode() {
        return getName();
    }

    /** {@inheritDoc} */
    public String toString() {
        return _desc;
    }

    /**
     * 性別のインスタンスを得る。
     * @param code 性別コード
     * @return 性別のインスタンス
     */
    public static Gender getInstance(final String code) {
        final Enum rtn = getEnum(MYCLASS, code);
        if (null != rtn) {
            return (Gender) rtn;
        }
        return UNKNOWN;
    }
} // Gender

// eof
