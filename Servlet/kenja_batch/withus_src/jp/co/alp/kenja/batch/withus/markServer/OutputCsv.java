// kanji=¿
/*
 * $Id: OutputCsv.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * ì¬ú: 2008/03/21 10:44:34 - JST
 * ì¬Ò: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * }[NT[opÌ¡ÌCSVt@Cð¶¬·éB
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
     * CSVt@Cð¶¬·éB
     */
    public void doIt() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        new MkSeito(_db, _param, "¶kîñ");
        new MkGakkoKankeisha(_db, _param, "wZÖWÒ");
        new MkKinmusakiGakushuKyoten(_db, _param, "Î±æwK_");
        new MkGakushuKyoten(_db, _param, "wK_");
        new MkKyoka(_db, _param, "³È");
        new MkKamoku(_db, _param, "ÈÚ");
        new MkCourse(_db, _param, "R[X");
        new MkClass(_db, _param, "NX");
        new MkRishuKadai(_db, _param, "CÛè");
        new MkSeitoRishuKamoku(_db, _param, "¶kCÈÚ");
        new MkSeitoRishuKadaiJisseki(_db, _param, "¶kCÛèÀÑ");
    }
} // OutputCsv

// eof
