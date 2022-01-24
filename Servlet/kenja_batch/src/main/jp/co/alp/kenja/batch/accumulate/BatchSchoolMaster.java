// kanji=漢字
/*
 * $Id: BatchSchoolMaster.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2009/08/07 13:00:00 - JST
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 学校マスタ。
 * @version $Id: BatchSchoolMaster.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class BatchSchoolMaster {
    /** 授業時数フラグ：法定授業 */
    public static final int JUGYOU_JISU_FLG_HOUTEI = 1;
    /** 授業時数フラグ：実授業 */
    public static final int JUGYOU_JISU_FLG_JITU = 2;

    /*pkg*/static final Log log = LogFactory.getLog(BatchSchoolMaster.class);

    private static BatchSchoolMaster schoolMaster_;

    /** 忌引出停についての出欠席算出法 */
    private static final int SYUKESSEKI_SANSYUTUHOU_SUSPEND_MOURNING = 1;

    private final int _semOffdays;
    private final int _subOffdays;
    private final int _subAbsent;
    private final int _subMourning;
    private final int _subSuspend;
    private final int _subVirus;

    private final int _jugyouJisuFlg;
    private final int _risyuBunshi;
    private final int _risyuBunbo;
    private final int _syutokuBunshi;
    private final int _syutokuBunbo;
    private final int _risyuBunshiSpecial;
    private final int _risyuBunboSpecial;
    private final int _syutokuBunshiSpecial;
    private final int _syutokuBunboSpecial;
    private final Integer _jituJifun;
    private final int _jituJifunSpecial;
    private final int _jituSyusu;
    private final int _jougentiSansyutuHou;
    private final int _syukessekiSansyutuHou;
    private final NameMaster _nameMaster;

    // CSOFF: ParameterNumber
    /**
     * コンストラクタ。
     * @param semOffdays 休学を1日欠席として扱うか
     * @param subOffdays 休学を欠課として扱うか
     * @param subAbsent 公欠を欠課として扱うか
     * @param subMourning 忌引を欠課として扱うか
     * @param subSuspend 出停を欠課として扱うか
     * @param subVirus 出停(伝染病)を欠課として扱うか
     * @param jugyouJisuFlg 授業時数フラグ
     * @param risyuBunshi 履修欠課数上限分子
     * @param risyuBunbo 履修欠課数上限分母
     * @param syutokuBunshi 習得欠課数上限分子
     * @param syutokuBunbo 習得欠課数上限分母
     * @param risyuBunshiSpecial 特活履修欠課数上限分子
     * @param risyuBunboSpecial 特活履修欠課数上限分母
     * @param syutokuBunshiSpecial 特活習得欠課数上限分子
     * @param syutokuBunboSpecial 特活習得欠課数上限分母
     * @param jituJifun 実授業時分
     * @param jituJifunSpecial 特活実授業時分
     * @param jituSyusu 実週数
     * @param jougentiSansyutuHou 上限値算出法
     * @param syukessekiSansyutuHou 出欠席算出法
     * @param nameMasterZ 名称マスタ
     */
    public BatchSchoolMaster(
            final int semOffdays,
            final int subOffdays,
            final int subAbsent,
            final int subMourning,
            final int subSuspend,
            final int subVirus,
            final int jugyouJisuFlg,
            final int risyuBunshi,
            final int risyuBunbo,
            final int syutokuBunshi,
            final int syutokuBunbo,
            final int risyuBunshiSpecial,
            final int risyuBunboSpecial,
            final int syutokuBunshiSpecial,
            final int syutokuBunboSpecial,
            final Integer jituJifun,
            final int jituJifunSpecial,
            final int jituSyusu,
            final int jougentiSansyutuHou,
            final int syukessekiSansyutuHou,
            final NameMaster nameMasterZ
    ) {
        _semOffdays = semOffdays;
        _subOffdays = subOffdays;
        _subAbsent = subAbsent;
        _subMourning = subMourning;
        _subSuspend = subSuspend;
        _subVirus = subVirus;
        _jugyouJisuFlg = jugyouJisuFlg ;
        _risyuBunshi = risyuBunshi;
        _risyuBunbo = risyuBunbo;
        _syutokuBunshi = syutokuBunshi;
        _syutokuBunbo = syutokuBunbo;
        _risyuBunshiSpecial = risyuBunshiSpecial;
        _risyuBunboSpecial = risyuBunboSpecial;
        _syutokuBunshiSpecial = syutokuBunshiSpecial;
        _syutokuBunboSpecial = syutokuBunboSpecial;
        _jituJifun = jituJifun;
        _jituJifunSpecial = jituJifunSpecial;
        _jituSyusu = jituSyusu;
        _jougentiSansyutuHou = jougentiSansyutuHou;
        _syukessekiSansyutuHou = syukessekiSansyutuHou;
        _nameMaster = nameMasterZ;
    }

    /**
     * バッチ用学校マスタを得る
     * @return バッチ用学校マスタ
     */
    public static BatchSchoolMaster getBatchSchoolMaster() {
        return schoolMaster_;
    }

    /**
     * バッチ用学校マスタをセットする
     * @param schoolMaster バッチ用学校マスタ
     */
    public static void setBatchSchoolMaster(final BatchSchoolMaster schoolMaster) {
        schoolMaster_ = schoolMaster;
    }

    /**
     * 休学を欠席として扱うか
     * @return 休学を欠席として扱うならtrue、そうでなければfalse
     */
    public boolean offdaysIsKesseki() {
        return _semOffdays == 1;
    }
    /**
     * 休学を欠課として扱うか
     * @return 休学を欠課として扱うならtrue、そうでなければfalse
     */
    public boolean offdaysIsKekka() {
        return _subOffdays == 1;
    }
    /**
     * 公欠を欠課として扱うか
     * @return 公欠を欠課として扱うならtrue、そうでなければfalse
     */
    public boolean absentIsKekka() {
        return _subAbsent == 1;
    }
    /**
     * 忌引を欠課として扱うか
     * @return 忌引を欠課として扱うならtrue、そうでなければfalse
     */
    public boolean mourningIsKekka() {
        return _subMourning == 1;
    }
    /**
     * 忌引を欠課として扱うか
     * @return 忌引を欠課として扱うならtrue、そうでなければfalse
     */
    public boolean suspendIsKekka() {
        return _subSuspend == 1;
    }
    /**
     * 出停伝染病を欠課として扱うか
     * @return 出停伝染病を欠課として扱うならtrue、そうでなければfalse
     */
    public boolean virusIsKekka() {
        return _subVirus == 1;
    }

    /**
     * 授業時数フラグを得る
     * @return 授業時数フラグ
     */
    public int getJugyouJisuFlg() {
        if (_jugyouJisuFlg != JUGYOU_JISU_FLG_HOUTEI && _jugyouJisuFlg != JUGYOU_JISU_FLG_JITU) {
            return JUGYOU_JISU_FLG_HOUTEI;
        }
        return _jugyouJisuFlg;
    }

    /**
     * 履修欠課数上限分母を得る。
     * @return 履修欠課数上限分母
     */
    public int getRisyuBunbo() {
        return _risyuBunbo;
    }

    /**
     * 履修欠課数上限分子を得る。
     * @return 履修欠課数上限分子
     */
    public int getRisyuBunshi() {
        return _risyuBunshi;
    }

    /**
     * 習得欠課数上限分母を得る。
     * @return 習得欠課数上限分母
     */
    public int getSyutokuBunbo() {
        return _syutokuBunbo;
    }

    /**
     * 習得欠課数上限分子を得る。
     * @return 習得欠課数上限分子
     */
    public int getSyutokuBunshi() {
        return _syutokuBunshi;
    }


    /**
     * 特活履修欠課数上限分母を得る。
     * @return 特活履修欠課数上限分母
     */
    public int getRisyuBunboSpecial() {
        return _risyuBunboSpecial;
    }

    /**
     * 特活履修欠課数上限分子を得る。
     * @return 特活履修欠課数上限分子
     */
    public int getRisyuBunshiSpecial() {
        return _risyuBunshiSpecial;
    }

    /**
     * 特活習得欠課数上限分母を得る。
     * @return 特活習得欠課数上限分母
     */
    public int getSyutokuBunboSpecial() {
        return _syutokuBunboSpecial;
    }

    /**
     * 特活習得欠課数上限分子を得る。
     * @return 特活習得欠課数上限分子
     */
    public int getSyutokuBunshiSpecial() {
        return _syutokuBunshiSpecial;
    }

    /**
     * 実授業時分を得る。
     * @return 実授業時分
     */
    public Integer getJituJifun() {
        return _jituJifun;
    }

    /**
     * 特活実授業時分を得る。
     * @return 特活実授業時分
     */
    public int getJituJifunSpecial() {
        return _jituJifunSpecial;
    }

    /**
     * 実週数を得る。
     * @return 実週数
     */
    public int getJituSyusu() {
        return _jituSyusu;
    }

    /**
     * 上限値算出法が四捨五入か
     * @return 上限値算出法が四捨五入ならtrue
     */
    public boolean jougentiSansyutuhouIsSisyaGonyu() {
        return _jougentiSansyutuHou == 1;
    }

    /**
     * 上限値算出法が切り上げか
     * @return 上限値算出法が切り上げならtrue
     */
    public boolean jougentiSansyutuhouIsKiriage() {
        return _jougentiSansyutuHou == 2;
    }

    /**
     * 上限値算出法が切り捨てか
     * @return 上限値算出法が切り捨てならtrue
     */
    public boolean jougentiSansyutuhouIsKirisute() {
        return _jougentiSansyutuHou == 3;
    }

    /**
     * 上限値算出法が実数計算か
     * @return 上限値算出法が実数計算ならtrue
     */
    public boolean jougentiSansyutuhouIsJissu() {
        return _jougentiSansyutuHou == 4;
    }

    /**
     * 累積テーブルは武蔵専用を使用するか
     * @return 累積テーブルは武蔵専用を使用するか
     */
    public boolean useExtendedAccumulateTable() {
        return true;
    }

    /**
     * 忌引出停についての出欠席算出法か
     * @return 忌引出停についての出欠席算出法か
     */
    public boolean syussekiSansyutuhouIsSuspendMourning() {
        return SYUKESSEKI_SANSYUTUHOU_SUSPEND_MOURNING == _syukessekiSansyutuHou;
    }
} // BatchSchoolMaster

// eof
