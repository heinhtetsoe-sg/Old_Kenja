// kanji=����
/*
 * $Id: InSyuketuTodoke.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/06/27 14:54:42 - JST
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.opencsv.CSVReader;

/*
 * �\�[�X�̍Ō�Ɍ��ґ�DB�̃e�[�u����`�R�����g����B
 */

/**
 * �o���͂�CSV�B
 * @author takaesu
 * @version $Id: InSyuketuTodoke.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class InSyuketuTodoke extends In {
    /*pkg*/static final Log log = LogFactory.getLog(InSyuketuTodoke.class);

    private String _file;

    public InSyuketuTodoke(final DB2UDB db, final Param param, final String title) {
        super(db, param, title);

        _file = getFileName();
    }

    public void doIt() throws IOException, SQLException {
        final Reader inputStreamReader = new InputStreamReader(new FileInputStream(_file), Param.encode);
        final CSVReader reader = new CSVReader(new BufferedReader(inputStreamReader));
        try {
            final List list = reader.readAll();
            log.info("�ǂݍ��݌���=" + list.size());

            // DB�ɓ����
            toDb(list);
        } catch (final IOException e) {
            log.fatal("CSV���Ǎ��߂Ȃ�:" + _file, e);
            throw e;
        }
    }

    private void toDb(final List list) throws SQLException {
        final ScalarHandler scalarHandler = new ScalarHandler();

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String[] line = (String[]) it.next();
            if (invalid(line)) {
                continue;
            }
            final Petition petition = createPetition(line);

            int seq = 1;
            try {
                final String seqSql = getSeqSql();
                final Integer maxSeq = (Integer) _qr.query(_db.conn, seqSql, scalarHandler);
                if (null != maxSeq) {
                    seq = maxSeq.intValue() + 1;
                }
            } catch (final SQLException e) {
                log.error("��t�ԍ��̍ő�l�擾�ŃG���[:" + petition, e);
                throw e;
            }

            insertTables(petition, seq);
            log.debug("��t�ԍ�(SeqNo)=" + seq + " ��2�̃e�[�u��(attend_petition_hdat, attend_petition_dat)��INSERT�����B" + petition);
        }
        _db.commit();
    }

    private void insertTables(final Petition petition, int seq) throws SQLException {
        final String sql0 = "INSERT INTO attend_petition_hdat VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";
        final Object[] params0 = petition.toArray0(seq);
        try {
            _qr.update(_db.conn, sql0, params0);
        } catch (final SQLException e) {
            log.fatal("�o���͂��w�b�_�[�f�[�^��INSERT�ŃG���[!:" + sql0, e);
            throw e;
        }

        final String sql1 = "INSERT INTO attend_petition_dat VALUES (?,?,?,?,?,?,?,?,?,current timestamp)";
        final Object[] params1 = petition.toArray1(seq);
        try {
            _qr.update(_db.conn, sql1, params1);
        } catch (final SQLException e) {
            log.fatal("�o���͂��f�[�^��INSERT�ŃG���[!:" + sql1, e);
            throw e;
        }
    }

    private String getSeqSql() {
        final String sql;
        sql = "SELECT"
            + "  max(seqno) as maxseq"
            + " FROM"
            + "  attend_petition_hdat"
            + " WHERE"
            + "  year='" + _param.getYear() + "'"
            ;
        return sql;
    }

    private boolean invalid(final String[] line) {
        if (StringUtils.isEmpty(line[7])) {//TODO: �o���ȃR�[�h���u1:���ȁv�̎���1���S�̂����ȂȂ̂ŁA�Z���͓���Ă��Ȃ��Ƃ̎��B'08.7.10�ēc���̃��[�����
            return true;
        }
        return false;
    }

    private Petition createPetition(final String[] line) {
        /*
         * 0: �w�Z���ʃR�[�h
         * 1: �o���ȔN����
         * 2: �w�Дԍ�
         * 3: �o�Ȕԍ�
         * 4: �g����
         * 5: �o���ȃR�[�h
         * 6: ���R�R�[�h
         * 7: �Z��
         */
        final String date = line[1];
        final String schregno = line[2];
        final String attendCd = line[5];
        final String reasonCd = line[6];
        final String periodCd = line[7];

        final Petition petition = new Petition(date, periodCd, schregno, attendCd, reasonCd);
        return petition;
    }

    private String getFileName() {
        final String fileName;
        if (null == _param._debugFileName) {
            final String ymd = new SimpleDateFormat("yyyyMMdd").format(new Date());
            fileName = "group" + ymd + ".csv";
        } else {
            fileName = _param._debugFileName;
        }
        return _param.getFullPath(fileName);
    }

    private class Petition {
        private final String _date;
        private final String _periodCd;
        private final String _schregno;
        /** �o���ȃR�[�h. 1=����, 2=�x��, 3=���� */
        private final String _attendCd;
        /** ���R�R�[�h. 1=����, 2=����, 3=�a�� */
        private final String _reasonCd;

        public Petition(
                final String date,
                final String periodCd,
                final String schregno,
                final String attendCd,
                final String reasonCd
        ) {
            final String yyyy = date.substring(0, 4);
            final String mm = date.substring(4, 6);
            final String dd = date.substring(6);
            _date = yyyy + "-" + mm + "-" + dd;
            _periodCd = periodCd;
            _schregno = schregno;
            _attendCd = attendCd;
            _reasonCd = reasonCd;
        }

        private Object[] toArray0(final int seq) {
            final Integer seqNo = new Integer(seq);
            final Object[] rtn = {
                    _param._year,
                    seqNo,
                    _schregno,
                    "2",    // �A�����敪�B0=�ی�ҁA1=���k�A2=���̑�
                    null,   // �A����
                    "0",    // �ԓd�̗v�E�s�v�B0=�A���s�v�A1=�A���K�v
                    _param.getDate() + " 00:00:00",   // ��t����
                    GroupWareServerString,
                    _date,
                    _periodCd,
                    _date,
                    _periodCd,
                    getDiCd(),
                    null,
                    GroupWareServerString,
            };
            return rtn;
        }

        private Object[] toArray1(final int seq) {
            final Integer seqNo = new Integer(seq);
            final Object[] rtn = {
                    _param._year,
                    seqNo,
                    _schregno,
                    _date,
                    _periodCd,
                    getDiCd(),
                    null,
                    "0",    // ���{�敪�B0=�o�����A1=�o���ς�
                    GroupWareServerString,
            };
            return rtn;
        }

        /**
         * ���҂̋ΑӃR�[�h�𓾂�B
         * @return ���҂̋ΑӃR�[�h
         */
        private String getDiCd() {
            // TODO: �K�؂ɕϊ�����
            return _reasonCd;
        }

        public String toString() {
            return _date + " " + _periodCd + "/" + _schregno;
        }
    }
} // InSyuketuTodoke

/*
    puma /tmp% db2 describe table attend_petition_hdat
                                   �^�C�v�E
    ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
    ------------------------------ --------- ------------------ -------- ----- ------
    YEAR                           SYSIBM    VARCHAR                   4     0 ������
    SEQNO                          SYSIBM    INTEGER                   4     0 ������
    SCHREGNO                       SYSIBM    VARCHAR                   8     0 �͂�
    CONTACTERDIV                   SYSIBM    VARCHAR                   1     0 �͂�
    CONTACTER                      SYSIBM    VARCHAR                  90     0 �͂�
    CALLBACK                       SYSIBM    VARCHAR                   1     0 �͂�
    FIRSTDATE                      SYSIBM    TIMESTAMP                10     0 �͂�
    FIRSTREGISTER                  SYSIBM    VARCHAR                   8     0 �͂�
    FROMDATE                       SYSIBM    DATE                      4     0 �͂�
    FROMPERIOD                     SYSIBM    VARCHAR                   1     0 �͂�
    TODATE                         SYSIBM    DATE                      4     0 �͂�
    TOPERIOD                       SYSIBM    VARCHAR                   1     0 �͂�
    DI_CD                          SYSIBM    VARCHAR                   2     0 �͂�
    DI_REMARK                      SYSIBM    VARCHAR                  30     0 �͂�
    REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
    UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
    
      16 ���R�[�h���I������܂����B
    
    puma /tmp% db2 describe table attend_petition_dat
    
                                   �^�C�v�E
    ��                           �X�L�[�}  �^�C�v��           ����    �ʎ�� NULL
    ------------------------------ --------- ------------------ -------- ----- ------
    YEAR                           SYSIBM    VARCHAR                   4     0 ������
    SEQNO                          SYSIBM    INTEGER                   4     0 ������
    SCHREGNO                       SYSIBM    VARCHAR                   8     0 ������
    ATTENDDATE                     SYSIBM    DATE                      4     0 ������
    PERIODCD                       SYSIBM    VARCHAR                   1     0 ������
    DI_CD                          SYSIBM    VARCHAR                   2     0 �͂�
    DI_REMARK                      SYSIBM    VARCHAR                  30     0 �͂�
    EXECUTED                       SYSIBM    VARCHAR                   1     0 �͂�
    REGISTERCD                     SYSIBM    VARCHAR                   8     0 �͂�
    UPDATED                        SYSIBM    TIMESTAMP                10     0 �͂�
    
      10 ���R�[�h���I������܂����B
    
    puma /tmp%
 */
// eof
