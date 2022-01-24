// kanji=漢字
/*
 * $Id: IKnj.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/09/05 18:23:15 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.Map;

/**
 * <<クラスの説明>>。
 * @author takaesu
 * @version $Id: IKnj.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public interface IKnj {

    /**
     * Mapのデータを配列に変換する。
     * @param map org.apache.commons.dbutils.handlers.MapListHandler の Map
     * @return 変換されたデータ
     */
    Object[] mapToArray(Map map);

}
// eof
