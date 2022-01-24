// kanji=漢字
/*
 * $Id: 5b92f67644f5575365036370f39947c3946ede9d $
 *
 * 作成日: 2008/05/19 10:32:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;

import java.util.Arrays;
import java.util.List;

/**
 * ウィザス専用ユーティリティクラス。
 * @author takaesu
 * @version $Id: 5b92f67644f5575365036370f39947c3946ede9d $
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
    public static final String PHYSICAL_EDUCATION_NEW_CURRICULUM = "3";
    public static final String PHYSICAL_EDUCATION_OLD_CURRICULUM = "1";

    /** 体育の科目コード。 */
    public static final String PHYSICAL_EDUCATION_SUBCLASS_CD = "060100";
    public static final String PHYSICAL_EDUCATION_SUBCLASS_CD_NEW = "060500";

    /** 科目コード。体育1〜体育7 */
    public static final List PHYSICAL_EDUCATIONS_NEW_LIST;
    public static final List PHYSICAL_EDUCATIONS_LIST;
    public static final List PHYSICAL_EDUCATIONS_OLD_LIST;

    /** 科目コード。体育1〜体育3 */
    public static final String[] PHYSICAL_EDUCATIONS_NEW = {
            "060100",
            "060200",
            "060300",
    };
    static {
        PHYSICAL_EDUCATIONS_NEW_LIST = Arrays.asList(PHYSICAL_EDUCATIONS_NEW);
    }

    /** 科目コード。体育1〜体育7 */
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

    /** 科目コード。体育1〜体育7 */
    public static final String[] PHYSICAL_EDUCATIONS_OLD = {
            "060201",
            "060202",
            "060203",
            "060204",
            "060205",
            "060206",
            "060207",
    };
    static {
        PHYSICAL_EDUCATIONS_OLD_LIST = Arrays.asList(PHYSICAL_EDUCATIONS_OLD);
    }

    /**
     * 体育1〜体育7の科目コードか?<br>
     * 教科コード、課程コードは自己責任.
     * @param subclassCd 科目コード
     * @return 体育1〜体育7の科目コードなら true
     */
    public static boolean isPhysicalEduNew(final String subclassCd) {
        return PHYSICAL_EDUCATIONS_NEW_LIST.contains(subclassCd);
    }

    /**
     * 体育1〜体育7の科目コードか?<br>
     * 教科コード、課程コードは自己責任.
     * @param subclassCd 科目コード
     * @return 体育1〜体育7の科目コードなら true
     */
    public static boolean isPhysicalEdu(final String subclassCd) {
        return PHYSICAL_EDUCATIONS_LIST.contains(subclassCd);
    }

    /**
     * 体育1〜体育3か?
     * @param classCd 教科コード
     * @param curriculumCd 過程コード
     * @param subclassCd 科目コード
     * @return 体育1〜体育3なら true
     */
    public static boolean isPhysicalEduNew(final String classCd, final String curriculumCd, final String subclassCd) {
        if (!PHYSICAL_EDUCATION_CLASS_CD.equals(classCd)) {
            return false;
        }
        if (!PHYSICAL_EDUCATION_NEW_CURRICULUM.equals(curriculumCd)) {
            return false;
        }
        return PHYSICAL_EDUCATIONS_NEW_LIST.contains(subclassCd);
    }

    /**
     * 体育1〜体育7か?
     * @param classCd 教科コード
     * @param curriculumCd 過程コード
     * @param subclassCd 科目コード
     * @return 体育1〜体育7なら true
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
