// kanji=����
/*
 * $Id: In.java 57802 2018-01-05 10:44:05Z yamashiro $
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

import jp.co.alp.kenja.batch.accumulate.QueryRunner;

/**
 * NBI�̃O���[�v�E�F�A�֘A��CSV�捞�ݗp�B
 * @author takaesu
 * @version $Id: In.java 57802 2018-01-05 10:44:05Z yamashiro $
 */
public abstract class In {
    /*pkg*/static final Log log = LogFactory.getLog(In.class);

    /** DB���R�[�h��Insert����ۂ̎��ʕ����� */
    public static final String GroupWareServerString = "icg2kc0";// �O���[�v�E�F�A�T�[�o�̃z�X�g��

    final QueryRunner _qr = new QueryRunner();  // �o�O�C���ł� QueryRunner�B

    protected final Param _param;
    protected final DB2UDB _db;

    abstract void doIt() throws IOException, SQLException;

    public In(final DB2UDB db, final Param param, final String title) {
        _param = param;
        _db = db;

        log.info("��" + title);
    }

} // In

// eof
