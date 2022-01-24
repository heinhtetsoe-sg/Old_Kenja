// kanji=����
/*
 * $Id: Mk.java 57802 2018-01-05 10:44:05Z yamashiro $
 *
 * �쐬��: 2008/04/30 16:42:46 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer;

import nao_package.db.Database;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.QueryRunner;

/**
 * VQS�T�[�o�A�g�p�B
 * @author takaesu
 * @version $Id: Mk.java 57802 2018-01-05 10:44:05Z yamashiro $
 */
public abstract class Mk {
    /*pkg*/static final Log log = LogFactory.getLog(Mk.class);

    public static final String EMPTY = "";
    /** PostgreSQL�G���[�R�[�h�B����������ᔽ�B��Ӑ��ᔽ�B */
    public static final String UNIQUE_VIOLATION = "23505";

    protected final Param _param;
    protected final Database _knj;
    protected final Database _vqs;

    final MapListHandler _handler = new MapListHandler();
    final QueryRunner _runner = new QueryRunner();  // �o�O�C���ł� QueryRunner�B

    public Mk(final Param param, final Database knj, final Database vqs, final String title) {
        _param = param;
        _knj = knj;
        _vqs = vqs;

        log.info("��" + title);
    }

    /**
     * null �� �󕶎���ɒu��������B
     * @param data ������̔z��
     * @return �󕶎���ɒu������������
     */
    public static String[] nullToEmpty(final String[] data) {
        final String[] rtn = new String[data.length];

        for (int i = 0; i < data.length; i++) {
            rtn[i] = (data[i] == null) ? EMPTY : data[i];
        }
        return rtn;
    }
} // Mk

// eof
