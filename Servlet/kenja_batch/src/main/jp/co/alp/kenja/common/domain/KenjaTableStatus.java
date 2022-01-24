// kanji=漢字
/*
 * $Id: KenjaTableStatus.java 74567 2020-05-27 13:21:04Z maeshiro $
 *
 * 作成日: 2015/09/08 13:38:46 - JST
 * 作成者: maesiro
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 賢者のテーブル情報。
 * @author maesiro
 * @version $Id: KenjaTableStatus.java 74567 2020-05-27 13:21:04Z maeshiro $
 */
public class KenjaTableStatus {

    /** log */
    private static final Log log = LogFactory.getLog(KenjaTableStatus.class);

    private static KenjaTableStatus INSTANCE;

    public synchronized static KenjaTableStatus getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new KenjaTableStatus();
        }
        return INSTANCE;
    }

    private boolean _isValidControlMasterAttendanceTerm;
    private boolean _isValidSchoolMasterSchoolKind;
    private boolean _isValidUsualScheduleExecuteDiv;
    private boolean _isValidUsualScheduleRemark;
    private boolean _useAttendDiCdDat;

    /**
     * コンストラクタ。
     */
    private KenjaTableStatus() {}

    /**
     * コントロール・マスタの出欠制御日付（未来）は使用可能かを設定する
     * @param isValid 使用可能ならtrue、それ以外はfalse
     */
    public void setValidControlMasterAttendanceTerm(final boolean isValid) {
        _isValidControlMasterAttendanceTerm = isValid;
    }

    /**
     * コントロール・マスタの出欠制御日付（未来）は使用可能か
     * @return 使用可能ならtrue、それ以外はfalse
     */
    public boolean isValidControlMasterAttendanceTerm() {
        return _isValidControlMasterAttendanceTerm;
    }

    /**
     * 通常時間割の実施区分は使用可能かを設定する
     * @param isValid 使用可能ならtrue、それ以外はfalse
     */
    public void setValidUsualScheduleExecuteDiv(final boolean isValid) {
        _isValidUsualScheduleExecuteDiv = isValid;
    }

    /**
     * 時間割の備考テーブルは使用可能かを設定する
     * @param isValid 使用可能ならtrue、それ以外はfalse
     */
    public void setValidUsualScheduleRemark(final boolean isValid) {
        _isValidUsualScheduleRemark = isValid;
    }

    /**
     * 時間割の備考テーブルは使用可能か
     * @return 使用可能ならtrue、それ以外はfalse
     */
    public boolean isValidUsualScheduleRemark() {
        return _isValidUsualScheduleRemark;
    }
    
    /**
     * 学校マスタの校種を使用するかを設定する
     * @param isValid 使用可能ならtrue、それ以外はfalse
     */
    public void setValidSchoolMasterSchoolKind(final boolean isValid) {
        _isValidSchoolMasterSchoolKind = isValid;
    }
    
    /**
     * 学校マスタの校種を使用するか
     * @return 使用するならtrue、それ以外はfalse
     */
    public boolean isValidSchoolMasterSchoolKind() {
        return _isValidSchoolMasterSchoolKind;
    }

    /**
     * 勤怠マスタにATTEND_DI_CD_DATを使用するかを設定する
     * @param isValid 使用可能ならtrue、それ以外はfalse
     */
    public void setUseAttendDiCdDat(final boolean useAttendDiCdDat) {
        _useAttendDiCdDat = useAttendDiCdDat;
    }

    /**
     * 勤怠マスタにATTEND_DI_CD_DATを使用するか
     * @return 使用するならtrue、それ以外はfalse
     */
    public boolean useAttendDiCdDat() {
        return _useAttendDiCdDat;
    }

} // KenjaTableStatus

// eof
