/*
 * $Id: ReflectHistoryQuery.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2015/06/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.reflectHistory.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import jp.co.alp.kenja.batch.reflectHistory.ReflectHistoryContext;
import jp.co.alp.kenja.common.dao.DbConnection;

public interface ReflectHistoryQuery extends Tables {
    Collection<Map<String, String>> query(final DbConnection dbcon, final ReflectHistoryContext ctx) throws SQLException;
}
