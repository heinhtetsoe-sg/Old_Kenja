// kanji=����
/*
 * $Id: InputCsv.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/04
 * �쐬��: takaesu
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
 * NBI�̃O���[�v�E�F�A����������CSV���捞��ŁADB�Ɋi�[�B
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
     * CSV�t�@�C����Ǎ��݁ADB�Ɋi�[����B
     */
    public void doIt() throws IOException, SQLException {
        new InJikanwariHenkou(_db, _param, "���Ԋ��ύX");

        final InSyuketuTodoke syuketuTodoke = new InSyuketuTodoke(_db, _param, "�o���͂�");
        syuketuTodoke.doIt();
    }
} // InputCsv

// eof
