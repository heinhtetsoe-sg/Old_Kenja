// kanji=����
/*
 * $Id: Main.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2017/05/30 14:23:11 - JST
 * �쐬��: m-yamashiro
 *
 * Copyright(C) 2017-2021 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.knjxTool;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.db.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * POSTGRE�T�[�o�Ƃ̘A�g�B
 *
 * @author m-yamashiro
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

            try {
                knj.open();
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
                param.load(knj);
                log.fatal("�p�����[�^: " + param);

                if (param.existA023()) {
                    doIt(param, knj);
                }
            } catch (final SQLException e) {
                log.fatal("���炩��SQL��O����������!", e);
                throw e;
            }

            knj.close();
            log.fatal("Done. �A�g/���҂̑o���� DB�N���[�Y����");
        } catch (final Throwable e) {
            log.fatal("�d��G���[����!", e);
        }
    }

    private static void doIt(final Param param, final Database knj) throws SQLException {
        try {
            new RegdUpdate(param, knj, "�w�Ё@TOOL_SCHREGD_DAT");
            new Score1update(param, knj, "�����i����l���jTOOL_SCORE_DAT");
            new Score2update(param, knj, "�����i���͎����jTOOL_SCORE_DAT");
            new Score4update(param, knj, "�����i�O���͎��jTOOL_SCORE_DAT");
            new Score5update(param, knj, "�����i�Z���^�[�jTOOL_SCORE_DAT");
            new AttendUpdate(param, knj, "�o���@TOOL_ATTEND_DAT");
            new RecordUpdate(param, knj, "�]��@TOOL_RECORD_DAT");
            new GradUpdate(param, knj, "��w���i�@TOOL_AFT_GRAD_DAT");
            new UserUpdate(param, knj, "���[�U�[�@TOOL_USER_MST");
            new UpdateUpdate(param, knj, "TOOL_UPDATE_DAT");
        } catch (final SQLException e) {
            log.fatal("SQLException����!");
            throw e;
        }
    }
} // Main

// eof
