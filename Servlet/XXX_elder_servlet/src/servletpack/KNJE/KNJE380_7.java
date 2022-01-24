// kanji=漢字
/*
 * $Id: 0999e9f24e029f8fd5458e287d4cd2d1b5abc050 $
 *
 * 作成日: 2009/10/19 18:14:45 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.util.List;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.KNJE380.Param;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 0999e9f24e029f8fd5458e287d4cd2d1b5abc050 $
 */
public class KNJE380_7 extends KNJE380Abstract {

    /**
     * コンストラクタ。
     * @param param
     * @param db2
     * @param svf
     */
    public KNJE380_7(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJE380_7.class");

    protected boolean printMain(final List printStudents, final String majorCd) throws SQLException {
        return false;
    }
}

// eof
