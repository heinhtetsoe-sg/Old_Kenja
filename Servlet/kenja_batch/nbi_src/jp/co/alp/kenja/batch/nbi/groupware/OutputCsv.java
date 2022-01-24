// kanji=漢字
/*
 * $Id: OutputCsv.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/04/04
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NBIのグループウェア用の複数のCSVファイルを生成する。
 * @author takaesu
 * @version $Id: OutputCsv.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class OutputCsv {
    /*pkg*/static final Log log = LogFactory.getLog(OutputCsv.class);

    private final Param _param;
    private final DB2UDB _db;

    public OutputCsv(final DB2UDB db, final Param param) {
        _db = db;
        _param = param;
    }

    /**
     * CSVファイルを生成する。
     */
    public void doIt() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        new MkKyousyokuin(_db, _param, "教職員");
        new MkSeito(_db, _param, "生徒");
        new MkKamoku(_db, _param, "科目");
        new MkClass(_db, _param, "クラス");
        new MkJikanwariTouroku(_db, _param, "時間割登録");
    }
} // OutputCsv

// eof
