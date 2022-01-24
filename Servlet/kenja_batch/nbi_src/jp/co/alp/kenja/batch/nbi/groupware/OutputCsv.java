// kanji=����
/*
 * $Id: OutputCsv.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/04
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NBI�̃O���[�v�E�F�A�p�̕�����CSV�t�@�C���𐶐�����B
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
     * CSV�t�@�C���𐶐�����B
     */
    public void doIt() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        new MkKyousyokuin(_db, _param, "���E��");
        new MkSeito(_db, _param, "���k");
        new MkKamoku(_db, _param, "�Ȗ�");
        new MkClass(_db, _param, "�N���X");
        new MkJikanwariTouroku(_db, _param, "���Ԋ��o�^");
    }
} // OutputCsv

// eof
