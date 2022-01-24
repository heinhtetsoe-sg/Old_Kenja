// kanji=漢字
/*
 * $Id: ExamItem.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2005/05/18 22:14:43 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.enums.MyEnum;


/**
 * 考査項目。
 * 通常時間割<code>UsualSchedule</code>から参照される。
 * @author tamura
 * @version $Id: ExamItem.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class ExamItem extends MyEnum<String, ExamItem> {
    /*pkg*/static final Log log = LogFactory.getLog(ExamItem.class);
    private static final Class<ExamItem> MYCLASS = ExamItem.class;

    private static final String MOCK_TEST_KIND = "03";

    private final int _semesterCode;
    private final Kind _kind;
    private final String _code;
    private final String _scoreDiv;
    private final String _name;
    private boolean _countFlag; // T115:finalを非finalに変更

    private ExamItem(
            final Category category,
            final String key,
            final int semesterCode,
            final Kind kind,
            final String code,
            final String scoreDiv,
            final String name,
            final boolean countFlag
    ) {
        super(category, key);
        _semesterCode = semesterCode;
        _kind = kind;
        _code = code;
        _scoreDiv = scoreDiv;
        _name = name;
        _countFlag = countFlag;
    }

    /**
     * 学期コードを得る。
     * @return 学期コード
     */
    public int getSemesterCode() {
        return _semesterCode;
    }

    /**
     * 考査種別を得る。
     * @return 考査種別
     */
    public Kind getKind() {
        checkAlive();
        return _kind;
    }

    /**
     * コードを得る。
     * @return コード
     */
    public String getCode() {
        checkAlive();
        return _code;
    }

    /**
     * この考査項目の名称を得る。
     * @return この考査項目の名称
     */
    public String getName() {
        checkAlive();
        return _name;
    }

    /**
     * この考査項目の集計フラグを得る。
     * @return この考査項目の集計フラグ
     */
    public boolean getCountFlag() {
        return _countFlag;
    }

    /**
     * この考査項目の集計フラグを設定する。
     * @param countFlag 集計フラグ
     * <!-- T115:追加:setCountFlag -->
     */
    public void setCountFlag(final boolean countFlag) {
        if (_countFlag == countFlag) {
            return;
        }
        log.fatal("集計フラグ変更:" + this + "->" + countFlag);
        _countFlag = countFlag;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return _kind + "-" + _code + ":" + _name + "," + CountFlagUtils.toString(_countFlag);
    }

    public static String key(
            final int semesterCode,
            final Kind kind,
            final String code,
            final String scoreDiv
    ) {
        return semesterCode + "+" + kind.getKey().toString() + "+" + code + (StringUtils.isBlank(scoreDiv) ? "" : "+" + scoreDiv);
    }

    public static List<String> keysLike(
            final Category category,
            final int semesterCode,
            final Kind kind,
            final String code
    ) {
        final List<String> rtn = new ArrayList<String>();
        for (final String key : getEnumMap(category).keySet()) {
            final String sKey = key.toString();
            if (sKey.startsWith(key(semesterCode, kind, code, null).toString())) {
                rtn.add(key);
            }
        }
        return rtn;
    }


    public static ExamItem getExamItemWithScoreDiv(final MyEnum.Category cat,
            final ControlMaster cm,
            final String examKindCd,
            final String exaxmItemCd) {
        final int semesterCode = cm.getCurrentSemester().getCode();
        final Collection<String> examItemKeys = ExamItem.getEnumMap(cat).keySet();
        final ExamItem.Kind examKind = ExamItem.Kind.create(cat, examKindCd, "不明な考査種別");
        final String scoreDiv;
        final ExamItem examItem;
        if (examItemKeys.contains(ExamItem.key(semesterCode, examKind, exaxmItemCd, null))) {
            scoreDiv = null;
            examItem = createExamItem(
                    cm,
                    examKindCd,
                    exaxmItemCd,
                    scoreDiv
            );
        } else if (examItemKeys.contains(ExamItem.key(semesterCode, examKind, exaxmItemCd, "01"))) {
            scoreDiv = "01";
            examItem = createExamItem(
                    cm,
                    examKindCd,
                    exaxmItemCd,
                    scoreDiv
            );
        } else {
            final List<String> keyList = ExamItem.keysLike(cat, semesterCode, examKind, exaxmItemCd);
            if (keyList.isEmpty()) {
                scoreDiv = null;
                examItem = createExamItem(
                        cm,
                        examKindCd,
                        exaxmItemCd,
                        scoreDiv
                );
            } else {
                examItem = ExamItem.getInstance(cat, keyList.get(0));
            }
        }
        return examItem;
    }

    /*
     * 考査種別、考査項目を得る。
     * 該当する考査種別、考査項目がなければ「不明な考査種別」「不明な考査項目」として作成する。
     */
    private static ExamItem createExamItem(
            final ControlMaster cm,
            final String examKindCd,
            final String examItemCd,
            final String scoreDiv
    ) {
        final MyEnum.Category cat = cm.getCategory();
        final ExamItem.Kind examKind = ExamItem.Kind.create(cat, examKindCd, "不明な考査種別");
        final String examItemName;
        if (examKind.isFixed() && "01".equals(examItemCd)) {
            examItemName = "定期考査";
        } else {
            examItemName = "不明な考査項目";
        }
        return ExamItem.create(cat, cm.getCurrentSemester().getCode(), examKind, examItemCd, scoreDiv, examItemName, false);
    }

    public static ExamItem getInstance(
            final Category category,
            final String key
    ) {
        return getEnum(category, MYCLASS, key);
    }

    /**
     * インスタンスを得る。
     * @param category カテゴリ
     * @param semesterCode 学期コード
     * @param kind 考査種別
     * @param code コード
     * @param scoreDiv SCORE_DIV
     * @return インスタンス
     */
    public static ExamItem getInstance(
            final Category category,
            final int semesterCode,
            final Kind kind,
            final String code,
            final String scoreDiv
    ) {
        final String key = key(semesterCode, kind, code, scoreDiv);
        return getInstance(category, key);
    }

    /**
     * なければ、考査項目のインスタンスを作成する。
     * すでに同じコードのインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリ
     * @param semesterCode 学期コード
     * @param kind 考査種別
     * @param code コード
     * @param scoreDiv SCORE_DIV
     * @param name 名称
     * @param countFlag 集計フラグ
     * @return インスタンス
     */
    public static ExamItem create(
            final Category category,
            final int semesterCode,
            final Kind kind,
            final String code,
            final String scoreDiv,
            final String name,
            final boolean countFlag
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        if (null == kind)       { throw new IllegalArgumentException("引数が不正(kind)"); }
        if (null == code)       { throw new IllegalArgumentException("引数が不正(code)"); }

        final String key = key(semesterCode, kind, code, scoreDiv);
        final ExamItem found = getInstance(category, key);
        if (null != found) {
            return found;
        }

        if (null == name)       { throw new IllegalArgumentException("引数が不正(name)"); }

        return new ExamItem(category, key, semesterCode, kind, code, scoreDiv, name, countFlag);
    }

    /**
     * 考査項目の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;ExamItem&gt;</code>
     */
    public static List<ExamItem> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 考査項目の列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;考査項目コード, ExamItem&gt;</code>
     */
    public static Map<String, ExamItem> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * 考査項目の数を得る。
     * @param category カテゴリー
     * @return 考査項目の数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * 考査項目の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }

    /**
     * 考査項目が実力テストか。
     * @return 考査項目が実力テストならtrue、それ以外はfalse
     */
    public boolean isMockTest() {
        return MOCK_TEST_KIND.equals(getKind().getKey());
    }

    //========================================================================

    /**
     * 考査種別。
     */
    public static final class Kind extends MyEnum<String, Kind> {
        private static final Class<Kind> MYCLASS = Kind.class;

        private final String _name;

        private Kind(
                final Category category,
                final String code,
                final String name
        ) {
            super(category, code);
            _name = name;
        }

        /** {@inheritDoc} */
        public String toString() {
            return _name;
        }

        /**
         * コードを得る。
         * @return コード
         */
        public String getCode() {
            checkAlive();
            return getKey().toString();
        }

        /**
         * 名称を得る。
         * @return 名称
         */
        public String getName() {
            checkAlive();
            return _name;
        }

        /**
         * 項目が固定されているか判定する。
         * @return 固定されていれば<code>true</code>を返す
         */
        public boolean isFixed() {
            checkAlive();
            return "01".equals(getCode()) || "02".equals(getCode());
        }

        /**
         * インスタンスを生成する。
         * @param category カテゴリ
         * @param code コード
         * @param name 名称
         * @return インスタンス
         */
        public static Kind create(
                final Category category,
                final String code,
                final String name
        ) {
            if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
            if (null == code)       { throw new IllegalArgumentException("引数が不正(code)"); }

            final Kind found = getInstance(category, code);
            if (null != found) {
                return found;
            }

            if (null == name)       { throw new IllegalArgumentException("引数が不正(name)"); }

            return new Kind(category, code, name);
        }

        /**
         * インスタンスを得る。
         * @param category カテゴリ
         * @param code コード
         * @return インスタンス
         */
        public static Kind getInstance(
                final Category category,
                final String code
        ) {
            return getEnum(category, MYCLASS, code);
        }

        /**
         * 考査種別の列挙のListを得る。
         * @param category カテゴリー
         * @return <code>List&lt;Kind&gt;</code>
         */
        public static List<Kind> getEnumList(final Category category) {
            return getEnumList(category, MYCLASS);
        }

        /**
         * 考査種別の列挙のMapを得る。
         * @param category カテゴリー
         * @return <code>Map&lt;考査種別コード, Kind&gt;</code>
         */
        public static Map<String, Kind> getEnumMap(final Category category) {
            return getEnumMap(category, MYCLASS);
        }

        /**
         * 考査種別の数を得る。
         * @param category カテゴリー
         * @return 考査種別の数
         */
        public static int size(final Category category) {
            return size(category, MYCLASS);
        }

        /**
         * 考査種別の列挙をクリアする。
         * @param category カテゴリー
         */
        public static void clearAll(final Category category) {
            clear(category, MYCLASS);
        }
    } // Kind

} // ExamItem

// eof
