// kanji=漢字
/*
 * $Id: WithusUtils.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/05/19 10:32:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus;

import java.util.Arrays;
import java.util.List;

/**
 * ウィザス専用ユーティリティクラス。
 * @author takaesu
 * @version $Id: WithusUtils.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class WithusUtils {
    /** 1授業の分。(1時間で何分授業か?) */
    public static final int PERIOD_MINUTE = 50;

    /** 体育の教科コード. */
    public static final String PHYSICAL_EDUCATION_CLASS_CD = "06";

    /**
     * 体育の過程コード.
     * 2003過程のみ
     */
    public static final String PHYSICAL_EDUCATION_CURRICULUM = "2";

    /** 体育の科目コード。 */
    public static final String PHYSICAL_EDUCATION_SUBCLASS_CD = "060100";

    /** 科目コード。体育1～体育7 */
    public static final List PHYSICAL_EDUCATIONS_LIST;

    /** 科目コード。体育1～体育7 */
    public static final String[] PHYSICAL_EDUCATIONS = {
            "060204",
            "060205",
            "060206",
            "060207",
            "060208",
            "060209",
            "060210",
    };
    static {
        PHYSICAL_EDUCATIONS_LIST = Arrays.asList(PHYSICAL_EDUCATIONS);
    }

    /**
     * 体育1～体育7の科目コードか?<br>
     * 教科コード、課程コードは自己責任.
     * @param subclassCd 科目コード
     * @return 体育1～体育7の科目コードなら true
     */
    public static boolean isPhysicalEdu(final String subclassCd) {
        return PHYSICAL_EDUCATIONS_LIST.contains(subclassCd);
    }

    /**
     * 体育1～体育7か?
     * @param classCd 教科コード
     * @param curriculumCd 過程コード
     * @param subclassCd 科目コード
     * @return 体育1～体育7なら true
     */
    public static boolean isPhysicalEdu(final String classCd, final String curriculumCd, final String subclassCd) {
        if (!PHYSICAL_EDUCATION_CLASS_CD.equals(classCd)) {
            return false;
        }
        if (!PHYSICAL_EDUCATION_CURRICULUM.equals(curriculumCd)) {
            return false;
        }
        return PHYSICAL_EDUCATIONS_LIST.contains(subclassCd);
    }

} // WithusUtils

// eof
