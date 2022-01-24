/*
 * $Id: AccumulateSemesConstants.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2011/10/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.Collection;

import jp.co.alp.kenja.common.domain.KenjaDateImpl;

/**
 * 出欠累積
 * @author maesiro
 * @version $Id: AccumulateSemesConstants.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public class AccumulateSemesConstants extends AccumulateSemes {

    /**
     * 出欠累積
     * @param lesson 授業日数
     * @param offdays 休学
     * @param abroad 留学
     * @param absent 公欠
     * @param suspend 出停
     * @param mourning 忌引
     * @param sick 病欠
     * @param notice 届有
     * @param nonotice 届無
     * @param late 遅刻
     * @param early 早退
     * @param lateNonotice 遅刻届無
     * @param earlyNonotice 早退届無
     */
    public AccumulateSemesConstants(
        final Collection<KenjaDateImpl> lesson,
        final Collection<KenjaDateImpl> offdays,
        final Collection<KenjaDateImpl> abroad,
        final Collection<KenjaDateImpl> absent,
        final Collection<KenjaDateImpl> suspend,
        final Collection<KenjaDateImpl> mourning,
        final Collection<KenjaDateImpl> sick,
        final Collection<KenjaDateImpl> notice,
        final Collection<KenjaDateImpl> nonotice,
        final Collection<KenjaDateImpl> late,
        final Collection<KenjaDateImpl> early,
        final Collection<KenjaDateImpl> lateNonotice,
        final Collection<KenjaDateImpl> earlyNonotice
    ) {
        lesson().set(lesson);

        /* 異動区分で判定する項目。 */
        offdays().set(offdays);
        abroad().set(abroad);

        /* 出欠データから算出する項目。 */
        absent().set(absent);
        suspend().set(suspend);
        mourning().set(mourning);
        sick().set(sick);
        notice().set(notice);
        nonotice().set(nonotice);
        late().set(late);
        early().set(early);
        lateNonotice().set(lateNonotice);
        earlyNonotice().set(earlyNonotice);
    }
}
