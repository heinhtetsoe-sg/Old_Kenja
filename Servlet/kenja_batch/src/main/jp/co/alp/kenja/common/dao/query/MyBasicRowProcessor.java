// kanji=漢字
/*
 * $Id: MyBasicRowProcessor.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2012/07/06 21:35:52 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004-2005,2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.dao.query;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import jp.co.alp.kenja.common.util.MyCaseInsensitiveHashMap;

import org.apache.commons.dbutils.BasicRowProcessor;

/**
 * org.apache.commons.dbutils.BasicRowProcessor
 */
public class MyBasicRowProcessor extends BasicRowProcessor {
    public MyBasicRowProcessor() {
        super();
    }

    public Map<String, Object> toMap(ResultSet rs) throws SQLException {
        Map<String, Object> result = new MyCaseInsensitiveHashMap();
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();

        for (int i = 1; i <= cols; i++) {
            // result.put(rsmd.getColumnName(i), rs.getObject(i));
            result.put(rsmd.getColumnLabel(i), rs.getObject(i));
        }

        return result;
    }
} // MyBasicRowProcessor

