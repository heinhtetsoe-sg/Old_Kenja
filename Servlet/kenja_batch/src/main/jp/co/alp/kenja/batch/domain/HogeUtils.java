// kanji=漢字
/*
 * $Id: HogeUtils.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2006/12/25 17:47:47 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.domain;

import java.util.List;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.domain.Student;
import jp.co.alp.kenja.common.domain.Student.Transfer;
import jp.co.alp.kenja.common.domain.Student.TransferCd;
import jp.co.alp.kenja.common.domain.SubClass;

/**
 * jp.co.alp.kenja.common.domain にまとめられそうなロジック。
 * @author takaesu
 * @version $Id: HogeUtils.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class HogeUtils {

    private HogeUtils() {
    }

    /**
     * 指定日の異動区分を得る。
     * @param student 生徒
     * @param date 日付
     * @return 異動区分
     */
    public static TransferCd getTransferCd(final Student student, final KenjaDateImpl date) {
        final List<Transfer> list = student.getTransfers();
        for (final Transfer transfer : list) {

            if (transfer.getSdate().compareTo(date) > 0) {
                continue;
            }
            if (null != transfer.getEdate() && transfer.getEdate().compareTo(date) < 0) {
                continue;
            }
            return transfer.getTransferCd();
        }
        return null;
    }

    /**
     * 教科コードを得る。
     * @param subClass 科目
     * @return 教科コード
     */
    public static String getClazzCode(final SubClass subClass) {
        if (null == subClass || subClass.getCode().length() < 2) {
            return null;
        }
        return subClass.getCode().substring(0, 2);
    }

    /**
     * 小さい(過去)方の日付を返す。
     * @param date1 日付
     * @param date2 日付
     * @return 小さい(過去)方の日付
     */
    public static KenjaDateImpl min(final KenjaDateImpl date1, final KenjaDateImpl date2) {
        if (null == date1 && null == date2) {
            return null;
        }
        if (null == date1) { return date2; }
        if (null == date2) { return date1; }

        if (date1.compareTo(date2) < 0) {
            return date1;
        }
        return date2;
    }
} // HogeUtils

// eof
