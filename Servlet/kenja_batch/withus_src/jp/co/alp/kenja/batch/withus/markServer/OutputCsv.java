// kanji=漢字
/*
 * $Id: OutputCsv.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/03/21 10:44:34 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * マークサーバ用の複数のCSVファイルを生成する。
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
        new MkSeito(_db, _param, "生徒情報");
        new MkGakkoKankeisha(_db, _param, "学校関係者");
        new MkKinmusakiGakushuKyoten(_db, _param, "勤務先学習拠点");
        new MkGakushuKyoten(_db, _param, "学習拠点");
        new MkKyoka(_db, _param, "教科");
        new MkKamoku(_db, _param, "科目");
        new MkCourse(_db, _param, "コース");
        new MkClass(_db, _param, "クラス");
        new MkRishuKadai(_db, _param, "履修課題");
        new MkSeitoRishuKamoku(_db, _param, "生徒履修科目");
        new MkSeitoRishuKadaiJisseki(_db, _param, "生徒履修課題実績");
    }
} // OutputCsv

// eof
