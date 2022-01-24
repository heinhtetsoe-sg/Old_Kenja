// kanji=漢字
/*
 * $Id: DaoDomainItemsUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2006/05/12 16:20:10 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.domain.DomainItem;

/**
 * コードのSQL(query)に関するユーティリティクラス。
 * @author tamura
 * @version $Id: DaoDomainItemsUtils.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class DaoDomainItemsUtils {
    /*pkg*/static final Log log = LogFactory.getLog(DaoDomainItemsUtils.class);

    private DaoDomainItemsUtils() {
        super();
    }

    private static void sqlCode(final StringBuffer sql, final DomainItem item) {
        sql.append("'").append(StringEscapeUtils.escapeSql(item.getCode())).append("'");
    }

    /**
     * コードのSQL条件を組み立てる。
     * @param join 接続文字列(" and "など)
     * @param columnName カラム名
     * @param sql 文字列バッファ
     * @param items 講座のコレクション。Collection&lt;講座&gt;
     */
    public static void sqlCondition(
            final String join,
            final String columnName,
            final StringBuffer sql,
            final Collection<? extends DomainItem> items
    ) {
        if (null == items || items.isEmpty()) {
            log.error("引数 items がnullまたは空");
            return;
        }

        if (1 == items.size()) {
            sql.append(join).append(" ").append(columnName).append(" = ");
            final Iterator<? extends DomainItem> it = items.iterator();
            final DomainItem item = it.next();
            sqlCode(sql, item);
        } else {
            sql.append(join).append(" ").append(columnName).append(" in (");

            String comma = "";
            for (final DomainItem item : items) {
                sql.append(comma);
                sqlCode(sql, item);
                // --
                comma = ",";
            }

            sql.append(")");
        }
    }
} // DaoDomainItemsUtils

// eof
