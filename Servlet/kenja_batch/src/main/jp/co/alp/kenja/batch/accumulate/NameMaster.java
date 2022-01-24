// kanji=漢字
/*
 * $Id: NameMaster.java 76357 2020-09-02 06:37:30Z maeshiro $
 *
 * 作成日: 2010/10/28 16:17:03 - JST
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.accumulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 名称マスタ。
 * @author maesiro
 * @version $Id: NameMaster.java 76357 2020-09-02 06:37:30Z maeshiro $
 */
public final class NameMaster {

    private static Log log = LogFactory.getLog(NameMaster.class);

    private static NameMaster instance_ = new NameMaster();

    private final Map<String, Map<String, Map<String, String>>> _dataTree;

    private NameMaster() {
        _dataTree = new HashMap<String, Map<String, Map<String, String>>>();
    }

    /**
     * インスタンスを得る
     * @return インスタンス
     */
    public static synchronized NameMaster getInstance() {
        return instance_;
    }

    private static <T> Map<String, T> createMap(final Map<String, Map<String, T>> map, final String cd) {
        if (!map.containsKey(cd)) {
            map.put(cd, new HashMap<String, T>());
        }
        return map.get(cd);
    }

    /**
     * データのマップを追加する
     * @param namecd1 NAME_MST.NAMECD1
     * @param namecd2 NAME_MST.NAMECD2
     * @param dataMap データのマップ
     */
    public void add(final String namecd1, final String namecd2, final Map<String, String> dataMap) {
        createMap(_dataTree, namecd1).put(namecd2, new HashMap<String, String>(dataMap));
    }

   /**
     * 指定フィールドの値を得る
     * @param namecd1 NAME_MST.NAMECD1
     * @param namecd2 NAME_MST.NAMECD2
     * @param fieldName NAME_MSTのフィールド名
     * @return 指定フィールドの値
     */
    public String get(final String namecd1, final String namecd2, final String fieldName) {
        return createMap(createMap(_dataTree, namecd1), namecd2).get(fieldName.toLowerCase());
    }

    /**
      * 指定namecd1のMapのコレクションを得る
      * @param namecd1 NAME_MST.NAMECD1
      * @return 指定namecd1のMapのコレクション
      */
     public Collection<Map<String, String>> get(final String namecd1) {
         final List<Map<String, String>> col = new ArrayList<Map<String, String>>();
         if (_dataTree.containsKey(namecd1)) {
             for (final Map<String, String> row : _dataTree.get(namecd1).values()) {
                 col.add(Collections.unmodifiableMap(row));
             }
         }
         return col;
     }
     
     public static String getValue(final Map<String, String> row, final String fieldName) {
         return null == row ? null : row.get(fieldName.toLowerCase());
     }

    /**
     * デバッグ用ログ出力
     */
    public void logDebug() {
        for (final String namecd1 : _dataTree.keySet()) {
            final Map<String, Map<String, String>> namecd2Map = createMap(_dataTree, namecd1);
            for (final String namecd2 : namecd2Map.keySet()) {
                final Map<String, String> data = createMap(namecd2Map, namecd2);
                log.debug(" namecd1 = " + namecd1 + ", namecd2 = " + namecd2 + ", data = " + data);
            }
        }
    }

} // NameMasterZ

// eof
