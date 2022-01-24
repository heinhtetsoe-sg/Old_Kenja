// kanji=漢字
/*
 * $Id: In.java 57802 2018-01-05 10:44:05Z yamashiro $
 *
 * 作成日: 2008/04/04
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.io.IOException;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.QueryRunner;

/**
 * NBIのグループウェア関連のCSV取込み用。
 * @author takaesu
 * @version $Id: In.java 57802 2018-01-05 10:44:05Z yamashiro $
 */
public abstract class In {
    /*pkg*/static final Log log = LogFactory.getLog(In.class);

    /** DBレコードをInsertする際の識別文字列 */
    public static final String GroupWareServerString = "icg2kc0";// グループウェアサーバのホスト名

    final QueryRunner _qr = new QueryRunner();  // バグ修正版の QueryRunner。

    protected final Param _param;
    protected final DB2UDB _db;

    abstract void doIt() throws IOException, SQLException;

    public In(final DB2UDB db, final Param param, final String title) {
        _param = param;
        _db = db;

        log.info("★" + title);
    }

} // In

// eof
