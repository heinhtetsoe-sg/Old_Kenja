// kanji=����
/*
 * $Id: Kintai.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2006/12/30 11:50:02 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.otr.domain;

import jp.co.alp.kenja.batch.otr.BatchTime;

/**
 * �ΑӃR�[�h�N���X
 * @version $Id: Kintai.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public final class Kintai {

    /** �ΑӃR�[�h: �o�� */
    private static final Kintai SEATED = new Kintai(new Integer(0), "�o��");

    /** �ΑӃR�[�h: ���̌�(��) */
    private static final Kintai INPUT_NONOTICE = new Kintai(new Integer(106), "���̌�(��)");

    /** �ΑӃR�[�h: ���̌�(��) (�J�[�h���͖�) */
    private static final Kintai NONOTICE = new Kintai(new Integer(6), "���̌�(��) (�J�[�h���͖�)");

    /** �ΑӃR�[�h: �x�� */
    private static final Kintai LATE = new Kintai(new Integer(15), "�x��");

    /** �ΑӃR�[�h: ���� */
    private static final Kintai EARLY = new Kintai(new Integer(16), "����");

    private final Integer _code;
    private final String _remark;

    /**
     * �R���X�g���N�^
     * @param code �ΑӃR�[�h
     * @param remark �R�����g������
     */
    private Kintai(final Integer code, final String remark) {
        _code = code;
        _remark = remark;
    }

    /**
     * �f�t�H���g�Α�(���̌�(��))�𓾂�
     * @return �f�t�H���g�Α�
     */
    public static Kintai getDefault() {
        return NONOTICE;
    }

    /**
     * �Αӏo�Ȃ𓾂�
     * @return �Αӏo��
     */
    public static Kintai getSeated() {
        return SEATED;
    }

    /**
     * �ΑӃR�[�h�𓾂�
     * @return �ΑӃR�[�h
     */
    public Integer getCode() {
        return _code;
    }

    /**
     * DB�ɏo�͂���ΑӃR�[�h�𓾂�
     * @return DB�ɏo�͂���ΑӃR�[�h
     */
    public Integer getResultCode() {
        return (this == INPUT_NONOTICE) ? NONOTICE.getCode() : getCode();
    }

    /**
     * �o�Ȃ�
     * @return �o�ȂȂ�true�A�����łȂ����false
     */
    public boolean isSeated() {
        return this.equals(SEATED);
    }

    /**
     * ���̌�(��)��
     * @return ���̌�(��)�Ȃ�true�A�����łȂ����false
     */
    public boolean isNonotice() {
        return this.equals(NONOTICE);
    }

    /**
     * �Αӂ𔻒肷��
     * @param rec ���k�̑ō�����
     * @param begin �Z���̊J�n����
     * @param teacherRec �u���̐搶�̑ō�����
     * @return �Α�
     */
    public static Kintai getKintai(
            final BatchTime rec,
            final BatchTime begin,
            final BatchTime teacherRec) {
        if (rec == null) {
            return NONOTICE;
        }
        final BatchTime lateLine = begin.add(0, 15); // �x��/���Ȕ��莞�Ԃ͍Z���̊J�n���Ԃ���15����

        if (rec.isBefore(begin, true)) {
            // ���Ƃ̊J�n���Ԃ��O�ɑō�����
            return SEATED;
        } else if (teacherRec == null) {
            // �搶���ō����Ă��Ȃ�
            return rec.isBefore(lateLine, true) ? LATE : INPUT_NONOTICE;
        } else {
            // �搶���x��/���Ȕ��莞�Ԃ��O�ɑō����A�����k���搶�̑ō����Ԃ��O�ɑō�����
            if (teacherRec.isBefore(lateLine, true) && rec.isBefore(teacherRec, true)) {
                return SEATED;
            } else {
                // �搶���x��/���Ȏ��Ԃ��O�ɑō����A�����k���搶�̑ō����Ԃ���ɑō�����
                // �܂��͐搶���x��/���Ȏ��Ԃ���ɑō�����
                return rec.isBefore(lateLine, true) ? LATE : INPUT_NONOTICE;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        String rescode = (_code.equals(getResultCode()) ? "" : "(" + getResultCode() +  ")" );
        return "�ΑӃR�[�h=[" + _code + rescode + "] ����=[" + _remark + "]";
    }
} // KintaiManager

// eof
