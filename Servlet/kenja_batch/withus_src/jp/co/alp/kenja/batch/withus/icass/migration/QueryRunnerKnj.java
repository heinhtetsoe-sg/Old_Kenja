// kanji=����
/*
 * $Id: QueryRunnerKnj.java 57802 2018-01-05 10:44:05Z yamashiro $
 *
 * �쐬��: 2008/09/08 14:16:21 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.handlers.MapListHandler;

import jp.co.alp.kenja.batch.accumulate.QueryRunner;// �o�O�C���ł� QueryRunner

/**
 * ICASS�ڍs�̐�p QueryRunner�B
 * @author takaesu
 * @see jp.co.alp.kenja.batch.QueryRunner
 * @version $Id: QueryRunnerKnj.java 57802 2018-01-05 10:44:05Z yamashiro $
 */
public class QueryRunnerKnj extends QueryRunner {
    private DB2UDB _db2;
    final MapListHandler _handler = new MapListHandler();

    public QueryRunnerKnj() {
        super();
    }

    public void init(final DB2UDB db2) {
        _db2 = db2;
    }

    public List mapListQuery(final String sql) throws SQLException {
        final List result = (List) query(_db2.conn, sql, _handler);
        return ((null != result) ? result : Collections.EMPTY_LIST);
    }

    public void listToKnj(final List list, final String tableName, final IKnj hoge) throws SQLException {
        int totalCount = 0;

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            final Object[] array = hoge.mapToArray(map);
            if (null == array) {
                continue;
            }
            try {
                final int insertCount = insert(tableName, array);
                if (1 != insertCount) {
                    throw new IllegalStateException("INSERT������1���ȊO!:" + insertCount);
                }
                totalCount += insertCount;
            } catch (final SQLException e) {
                throw e;
            }
        }
        _db2.commit();
    }

    public int insert(final String tableName, final Object[] array) throws SQLException {
        final String holders = getHolders(array.length);
        final String sql = "INSERT INTO " + tableName + " VALUES(" + holders + "current timestamp)";//TODO: SQL���͏����1�x��������Ă��ǂ�
        return update(_db2.conn, sql, array);
    }

    /**
     * count �̐����� "?,"��A�����ĕԂ��B
     * @param count ��
     * @return ��) count=3 �Ȃ�A"?,?,?,"
     */
    private String getHolders(final int count) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < count; i++) {
            sb.append("?,");
        }
        return sb.toString();
    }
} // QueryRunnerKnj

// eof
