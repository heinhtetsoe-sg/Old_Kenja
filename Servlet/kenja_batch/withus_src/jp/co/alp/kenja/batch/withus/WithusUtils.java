// kanji=����
/*
 * $Id: WithusUtils.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/05/19 10:32:35 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus;

import java.util.Arrays;
import java.util.List;

/**
 * �E�B�U�X��p���[�e�B���e�B�N���X�B
 * @author takaesu
 * @version $Id: WithusUtils.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class WithusUtils {
    /** 1���Ƃ̕��B(1���Ԃŉ������Ƃ�?) */
    public static final int PERIOD_MINUTE = 50;

    /** �̈�̋��ȃR�[�h. */
    public static final String PHYSICAL_EDUCATION_CLASS_CD = "06";

    /**
     * �̈�̉ߒ��R�[�h.
     * 2003�ߒ��̂�
     */
    public static final String PHYSICAL_EDUCATION_CURRICULUM = "2";

    /** �̈�̉ȖڃR�[�h�B */
    public static final String PHYSICAL_EDUCATION_SUBCLASS_CD = "060100";

    /** �ȖڃR�[�h�B�̈�1�`�̈�7 */
    public static final List PHYSICAL_EDUCATIONS_LIST;

    /** �ȖڃR�[�h�B�̈�1�`�̈�7 */
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
     * �̈�1�`�̈�7�̉ȖڃR�[�h��?<br>
     * ���ȃR�[�h�A�ے��R�[�h�͎��ȐӔC.
     * @param subclassCd �ȖڃR�[�h
     * @return �̈�1�`�̈�7�̉ȖڃR�[�h�Ȃ� true
     */
    public static boolean isPhysicalEdu(final String subclassCd) {
        return PHYSICAL_EDUCATIONS_LIST.contains(subclassCd);
    }

    /**
     * �̈�1�`�̈�7��?
     * @param classCd ���ȃR�[�h
     * @param curriculumCd �ߒ��R�[�h
     * @param subclassCd �ȖڃR�[�h
     * @return �̈�1�`�̈�7�Ȃ� true
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
