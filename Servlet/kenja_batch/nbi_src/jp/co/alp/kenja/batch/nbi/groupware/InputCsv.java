// kanji=漢字
/*
 * $Id: InputCsv.java 56574 2017-10-22 11:21:06Z maeshiro $
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

/**
 * NBIのグループウェアが生成したCSVを取込んで、DBに格納。
 * @author takaesu
 * @version $Id: InputCsv.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class InputCsv {
    /*pkg*/static final Log log = LogFactory.getLog(InputCsv.class);

    private final Param _param;
    private final DB2UDB _db;

    public InputCsv(final DB2UDB db, final Param param) {
        _db = db;
        _param = param;
    }

    /**
     * CSVファイルを読込み、DBに格納する。
     */
    public void doIt() throws IOException, SQLException {
        new InJikanwariHenkou(_db, _param, "時間割変更");

        final InSyuketuTodoke syuketuTodoke = new InSyuketuTodoke(_db, _param, "出欠届け");
        syuketuTodoke.doIt();
    }
} // InputCsv

// eof
