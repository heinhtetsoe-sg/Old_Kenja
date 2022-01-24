// kanji=����
/*
 * $Id: InJikanwariHenkou.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/06/27 14:53:13 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import java.sql.Date;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.nbi.groupware.domain.Schedule;
import jp.co.alp.kenja.batch.opencsv.CSVReader;

/**
 * ���Ԋ��ύXCSV�B
 * @author takaesu
 * @version $Id: InJikanwariHenkou.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class InJikanwariHenkou extends In {
    /*pkg*/static final Log log = LogFactory.getLog(InJikanwariHenkou.class);

    private final static String _FILE = "group0601.csv";

    /* CSV�t�H�[�}�b�g
     *  1: �w�Z���ʃR�[�h
     *  2: ���Ԋ��ύX���(1=���ƕύX, 2=�j���̌���, 3=�j���̕ύX, 4=�Z���̈ꊇ����, 5=�Z���̈ꊇ�ύX, 6=�Z���̈ꊇ�폜, 7=���Ƃ̌���)
     *  3: �J��グ�J�艺���R�[�h(���Ԋ��ύX���=�Z���̈ꊇ�폜(6)�̏ꍇ�L���B0=�����Ȃ�(�폜�̂�), 1=�J��グ, 2=�J�艺��)
     *  4: �w�Z�R�[�h
     *  --- �ύX�O
     *  5: ���t//TAKAESU:YYYYMMDD
     *  6: �j��
     *  7: �Z��
     *  8: �g����
     *  9: �Q�R�[�h
     * 10: �ȖڃR�[�h
     * 11: �E���R�[�h
     *  --- �ύX��
     * 12: ���t
     * 13: �j��
     * 14: �Z��
     * 15: �g����
     * 16: �Q�R�[�h
     * 17: �ȖڃR�[�h
     * 18: �E���R�[�h
     */
    //TAKAESU: �w���Ƃ̕ύX�����R�[�h(0=�����Ȃ�, 1=��������, 2=��������)�x���ĉ�?FAX�ł�����������ɏ����Ă���

    public InJikanwariHenkou(DB2UDB db, Param param, String title) {
        super(db, param, title);
    }

    void doIt() throws IOException, SQLException {
        final String fullPathFile = _param.getFullPath(_FILE);
        final Reader inputStreamReader = new InputStreamReader(new FileInputStream(fullPathFile), Param.encode);
        final CSVReader reader = new CSVReader(new BufferedReader(inputStreamReader));
        try {
            final List list = reader.readAll();
            log.info("�ǂݍ��݌���=" + list.size());

            // DB�ɓ����
            toDb(list);
        } catch (final IOException e) {
            log.fatal("CSV���Ǎ��߂Ȃ�:" + _FILE, e);
            throw e;
        }
    }

    private void toDb(final List list) throws SQLException {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String[] line = (String[]) it.next();
            if (invalid(line)) {
                continue;
            }
            log.debug(line);
            
        }
    }

    private boolean invalid(final String[] line) {
        if ("6".equals(line[1])) {
            if (line[7].length() != 1) {
                return true;
            }
            if ("012".indexOf(line[7]) == -1) {
                return true;
            }
        }
        return false;
    }

    private void deleteTables() throws SQLException {
        final String sql = "DELETE FROM sch_chr_dat WHERE executedate=? AND periodcd=? AND chaircd=?";
        try {
            _qr.update(_db.conn, sql);
        } catch (final SQLException e) {
            log.fatal("���Ԋ��폜�ŃG���[");
            throw e;
        }
    }

    private void insertTables() throws SQLException {
        final String sql = "INSERT INTO sch_chr_dat VALUES (?,?,?,?,?,?,?,?,?,current timestamp)";
    }

    private class MySchedule extends Schedule {
        /** �f�[�^�敪. 0=��{���Ԋ�����Z�b�g, 1=�ʏ펞�Ԋ��ŃZ�b�g, 2=�e�X�g���Ԋ��ŃZ�b�g */
        private String _dataDiv;

        /** �o���m�F�҃R�[�h. */
        private final String _attestor;

        public MySchedule(final Date date, final String periodCd, final String chairCd, final boolean executed, String attestor) {
            super(date, periodCd, chairCd, executed);
            _attestor = attestor;
        }
    }
} // InJikanwariHenkou

// eof
