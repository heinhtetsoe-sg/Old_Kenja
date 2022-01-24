// kanji=漢字
/*
 * $Id: CombinedSubClassManager.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2004/06/07 18:11:54 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.co.alp.kenja.common.domain.SubClass;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 合併科目マネージャー。
 * @author maesiro
 * @version $Id: CombinedSubClassManager.java 74552 2020-05-27 04:41:22Z maeshiro $
 */
public final class CombinedSubClassManager {

    /** log */
    private static final Log log = LogFactory.getLog(CombinedSubClassManager.class);

    private static CombinedSubClassManager manager_;

    private Map<SubClass, Collection<SubClass>> _combinedAttendsMap;

    /*
     * コンストラクタ。
     */
    private CombinedSubClassManager() {
        _combinedAttendsMap = new HashMap<SubClass, Collection<SubClass>>();
    }

    /**
     *
     * @return 合併科目マネージャーを得る
     */
    public static synchronized CombinedSubClassManager getInstance() {
        if (null == manager_) {
            manager_ = new CombinedSubClassManager();
        }
        return manager_;
    }

    /**
     * 合併先科目のコレクションを得る
     * @return 合併先科目のコレクション
     */
    public Collection<SubClass> getCombinedSubClasses() {
        return Collections.unmodifiableSet(_combinedAttendsMap.keySet());
    }

    /**
     * 合併元科目のコレクションを得る
     * @param combined 合併先科目
     * @return 合併元科目のコレクション
     */
    public Collection<SubClass> getAttendSubClasses(final SubClass combined) {
        return Collections.unmodifiableCollection(getAttendSubClasses(combined, false));
    }

    /**
     * 合併元科目のコレクションを作成する
     * @param combined 合併先科目
     * @return 合併元科目のコレクション
     */
    private Collection<SubClass> createAttendSubClasses(final SubClass combined) {
        return getAttendSubClasses(combined, true);
    }

    /**
     * 合併元科目のコレクションを得る
     * @param combined 合併先科目
     * @param isCreate 作成フラグ
     * @return 合併元科目のコレクション
     */
    private Collection<SubClass> getAttendSubClasses(final SubClass combined, final boolean isCreate) {
        if (!_combinedAttendsMap.containsKey(combined)) {
            if (isCreate) {
                _combinedAttendsMap.put(combined, new HashSet<SubClass>());
            } else {
                return Collections.emptyList();
            }
        }
        return _combinedAttendsMap.get(combined);
    }

    /**
     * 合併科目の設定を追加する
     * @param combined 合併先科目
     * @param attend 合併元科目
     */
    public void add(final SubClass combined, final SubClass attend) {
        createAttendSubClasses(combined).add(attend);
        log.debug("合併科目設定追加: 合併先科目=" + combined + ", 合併元科目=" + attend);
    }

    /**
     * 合併先科目か
     * @param subClass 科目
     * @return subClassが合併先科目ならtrue、そうでなければfalse
     */
    public boolean isCombinedSubClass(final SubClass subClass) {
        return _combinedAttendsMap.containsKey(subClass);
    }

    /**
     * 合併科目の設定があるか
     * @param subClass1 科目1
     * @param subClass2 科目2
     * @return subClass1が合併先科目でsubClass2がsubclass1の合併元科目ならtrue、そうでなければfalse
     */
    public boolean isCombinedSubClass(final SubClass subClass1, final SubClass subClass2) {
        return isCombinedSubClass(subClass1) && getAttendSubClasses(subClass1).contains(subClass2);
    }

    /**
     * 合併元科目か
     * @param subClass 科目
     * @return subClassが合併元科目ならtrue、そうでなければfalse
     */
    public boolean isAnyAttendSubClass(final SubClass subClass) {
        boolean found = false;
        for (final SubClass combined : _combinedAttendsMap.keySet()) {
            found = isAttendSubClass(combined, subClass);
            if (found) {
                break;
            }
        }
        return found;
    }

    /**
     * 指定科目を合併元科目とする合併先科目のリストを得る
     * @param attend 合併元科目
     * @return 合併先科目のリスト
     */
    public Collection<SubClass> getCombinedSubClasses(final SubClass attend) {
        if (!isAnyAttendSubClass(attend)) {
            return Collections.emptyList();
        }
        final Set<SubClass> combinedSubclasses = new HashSet<SubClass>();
        for (final SubClass combined : _combinedAttendsMap.keySet()) {
            if (isAttendSubClass(combined, attend)) {
                combinedSubclasses.add(combined);
            }
        }
        return combinedSubclasses;
    }

    /**
     * 合併科目の設定があるか
     * @param subClass1 科目1
     * @param subClass2 科目2
     * @return subClassが合併元科目ならtrue、そうでなければfalse
     */
    public boolean isAttendSubClass(final SubClass subClass1, final SubClass subClass2) {
        return isCombinedSubClass(subClass1) && getAttendSubClasses(subClass1).contains(subClass2);
    }

} // CombinedSubClass

// eof
