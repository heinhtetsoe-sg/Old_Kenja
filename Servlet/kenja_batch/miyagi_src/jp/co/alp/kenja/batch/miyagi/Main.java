// kanji=����
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/30 10:55:29 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.miyagi;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.db.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.miyagi.db.PostgreSQL;

/**
 * POSTGRE�T�[�o�Ƃ̘A�g�B
 * 
 * @author takaesu
 * @version $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Main {
    /* pkg */static final Log log = LogFactory.getLog(Main.class);

    public static void main(final String[] args) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            final Package pkg = Main.class.getPackage();
            if (null != pkg) {
                final String implementationVersion = pkg.getImplementationVersion(); // �N���X���[�_�ɂ��Anull�̉\������
                if (null != implementationVersion) {
                    log.info("implementationVersion=" + implementationVersion);
                } else {
                    log.info("MANIFEST�t�@�C������̃o�[�W�����擾�ɂ͎��s���܂����B");
                }
            }

            final Param param = new Param(args);

            final Database knj = new DB2UDB(param._knjUrl, param._knjUser, param._knjPass, DB2UDB.TYPE2);
            final Database vqs = new PostgreSQL(param._vqsUrl, param._vqsUser, param._vqsPass);
            final Database iinkai = new DB2UDB(param._iinkaiUrl, param._iinkaiUser, param._iinkaiPass, DB2UDB.TYPE2);

            try {
                knj.open();
                vqs.open(param._vqsUser, param._vqsPass);
                iinkai.open();
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
                log.fatal("Start. �A�g/���҂̑o���� DB�I�[�v������");

                param.load(knj);
                log.fatal("�p�����[�^: " + param);

                // �A�g�e�[�u�����ʃt�B�[���h�i�w�Z�R�[�h�A�ے��R�[�h�j�����邩
                if (param.isNotNullCommonField()) {
                    doIt(param, knj, vqs, iinkai);
                }
            } catch (final SQLException e) {
                log.fatal("���炩��SQL��O����������!", e);
                throw e;
            }

            iinkai.close();
            vqs.close();
            knj.close();
            log.fatal("Done. �A�g/���҂̑o���� DB�N���[�Y����");
        } catch (final Throwable e) {
            log.fatal("�d��G���[����!", e);
        }
    }

    private static void doIt(final Param param, final Database knj, final Database vqs, final Database iinkai) throws SQLException {
        try {
            // ���ҁ˘A�g
            new StaffInfo(param, knj, vqs, iinkai, "�E�����e�[�u��");
            new GradeInfo(param, knj, vqs, "�w�N���e�[�u��");
            new ClassInfo(param, knj, vqs, "�N���X���e�[�u��");
            new StudentInfo(param, knj, vqs, "���k���e�[�u��");
        } catch (final SQLException e) {
            log.fatal("SQLException����!");
            throw e;
        }
    }
} // Main

// eof
