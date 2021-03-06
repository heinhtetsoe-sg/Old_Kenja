// kanji=漢字
/*
 * $Id: PostgreSQL.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/30 10:48:13 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.miyagi.db;

import nao_package.db.Database;

/**
 * PostgreSQL。
 * @author takaesu
 * @version $Id: PostgreSQL.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class PostgreSQL extends Database {
    public PostgreSQL(final String url, final String user, final String password) {
        driver = "org.postgresql.Driver";

        super.url = "jdbc:postgresql:" + url;
        super.user = user;
        super.pass = password;
    }
} // PostgreSQL

// eof
