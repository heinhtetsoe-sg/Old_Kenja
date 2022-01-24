// kanji=����
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/30 10:55:29 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.vqsServer;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.db.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.Curriculum;
import jp.co.alp.kenja.batch.withus.vqsServer.db.PostgreSQL;

/**
 * VQS�T�[�o�Ƃ̘A�g�B
 * @author takaesu
 * @version $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Main {
    /*pkg*/static final Log log = LogFactory.getLog(Main.class);

    public static void main(final String[] args) throws
        SQLException,
        InstantiationException,
        IllegalAccessException,
        ClassNotFoundException
    {
        try {
            final Package pkg = Main.class.getPackage();
            if (null != pkg) {
                final String implementationVersion = pkg.getImplementationVersion();    // �N���X���[�_�ɂ��Anull�̉\������
                if (null != implementationVersion) {
                    log.info("implementationVersion=" + implementationVersion);
                } else {
                    log.info("MANIFEST�t�@�C������̃o�[�W�����擾�ɂ͎��s���܂����B");
                }
            }

            final Param param = new Param(args);

            final Database knj = new DB2UDB(param._knjUrl, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            final Database vqs = new PostgreSQL(param._vqsUrl, param._vqsUser, param._vqsPass);

            try {
                knj.open();
                vqs.open();
            } catch (final InstantiationException e) {
                log.fatal("InstantiationException", e);
                throw e;
            } catch (final IllegalAccessException e) {
                log.fatal("IllegalAccessException", e);
                throw e;
            } catch (final ClassNotFoundException e) {
                log.fatal("ClassNotFoundException", e);
                throw e;
            }

            try {
                vqs.stmt = vqs.conn.createStatement();  // TAKAESU: �����Ɛ����ł���
        
                log.fatal("Start. VQS/���҂̑o���� DB�I�[�v������");
        
                param.load(knj);
                log.fatal("�p�����[�^: " + param);
        
                Curriculum.loadCurriculumMst(knj);
                log.warn("���̃}�X�^�̋���ے�����Ǎ��񂾁B");
        
                doIt(param, knj, vqs);
            } catch (final SQLException e) {
                log.fatal("���炩��SQL��O����������!", e);
                throw e;
            }

            vqs.close();
            knj.close();
            log.fatal("Done. VQS/���҂̑o���� DB�N���[�Y����");
        } catch (final Throwable e) {
            log.fatal("�d��G���[����!", e);
        }
    }

    private static void doIt(final Param param, final Database knj, final Database vqs) throws SQLException {
        try {
            // ���ҁ�VQS
            new MkSchRegistDat(param, knj, vqs, "���k���C���e�[�u��");
            new MkSchRegBaseMst(param, knj, vqs, "���k���e�[�u��");
            new MkStaffMst(param, knj, vqs, "�u�t���e�[�u��");
            new MkSchResultDat(param, knj, vqs, "���k���я��e�[�u��");

            // VQS�ˌ���
            new MkSchAttendDat(param, knj, vqs, "���k�o�ȏ��e�[�u��");
        } catch (final SQLException e) {
            log.fatal("SQLException����!");
            throw e;
        }
    }
} // Main

// eof
