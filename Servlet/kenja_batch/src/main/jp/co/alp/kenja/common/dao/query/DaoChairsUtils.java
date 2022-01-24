// kanji=漢字
/*
 * $Id: DaoChairsUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2006/05/08 16:56:21 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.Chair;

/**
 * 講座コードのSQL(query)に関するユーティリティクラス。
 * @author tamura
 * @version $Id: DaoChairsUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoChairsUtils {
    /*pkg*/static final Log log = LogFactory.getLog(DaoChairsUtils.class);

    private DaoChairsUtils() {
        super();
    }

    /**
     * 講座コードのSQL条件を組み立てる。
     * @param join 接続文字列(" and "など)
     * @param columnName カラム名
     * @param sql 文字列バッファ
     * @param chairs 講座のコレクション。Collection&lt;講座&gt;
     */
    public static void sqlChairCondition(
            final String join,
            final String columnName,
            final StringBuffer sql,
            final Collection<Chair> chairs
    ) {
        if (null == chairs || chairs.isEmpty()) {
            log.error("引数 chairs がnullまたは空");
            return;
        }

        DaoDomainItemsUtils.sqlCondition(join, columnName, sql, chairs);
    }

} // DaoChairsUtils

// eof
