// kanji=����
/*
 * $Id: InputCsv.java 57802 2018-01-05 10:44:05Z yamashiro $
 *
 * �쐬��: 2008/03/21 10:46:12 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.markServer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.accumulate.QueryRunner;
import jp.co.alp.kenja.batch.opencsv.CSVReader;
import jp.co.alp.kenja.batch.withus.Curriculum;

/**
 * �̓_���ʑ��M�f�[�^CSV���捞��ŁADB�Ɋi�[�B
 * @author takaesu
 * @version $Id: InputCsv.java 57802 2018-01-05 10:44:05Z yamashiro $
 */
public class InputCsv {
    /*pkg*/static final Log log = LogFactory.getLog(InputCsv.class);

    /** DB���R�[�h��Insert����ۂ̎��ʕ����� */
    public static final String MarkserverString = "MarkSvr";

    /** DB�e�[�u���� REGISTERCD */
    public static final String StaffCd = "99999999";

    private final Param _param;
    private final DB2UDB _db;

    final QueryRunner _qr = new QueryRunner();  // �o�O�C���ł� QueryRunner�B

    public InputCsv(final DB2UDB db, final Param param) {
        _db = db;
        _param = param;
    }

    /**
     * CSV�t�@�C����Ǎ��݁ADB�Ɋi�[����B
     */
    public void doIt() throws IOException {
        // CSV����荞��
        final String file = _param.getFile();
        final Reader inputStreamReader = new InputStreamReader(new FileInputStream(file), Param.encode);
        final CSVReader reader = new CSVReader(new BufferedReader(inputStreamReader));
        try {
            final List list = reader.readAll();
            log.info("�ǂݍ��݌���=" + list.size());
            log.info("�擪�s�̓X�L�b�v���܂��B");

            // DB�ɓ����
            toDb(list);
        } catch (final IOException e) {
            log.fatal("CSV���Ǎ��߂Ȃ�:" + file, e);
            throw e;
        }
    }

    private void toDb(final List list) {
        boolean isFirstLine = true;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String[] line = (String[]) it.next();
            if (isFirstLine) {
                isFirstLine = false;
                continue;   // �擪�s�̓w�b�_�[�Ȃ̂ŃX�L�b�v
            }

            final SaitenKekka saitenKekka = createSaitenKekka(line);
            if (null == saitenKekka) {
                continue;
            }
            if (saitenKekka.isReport() && !saitenKekka.validScore()) {
                log.warn("���񓾓_�ƒǎ����_����������� or ���������Ă���B�������܂��B:" + saitenKekka);
                continue;
            }

            final String sql;
            final Object[] params;
            if (saitenKekka.isReport()) {
                if (!saitenKekka.isSecond()) {
                    sql = "INSERT INTO rec_report_dat VALUES (?,?,?,?,?,?,?,?,?,?,?,?,current timestamp)";
                    params = saitenKekka.toArrayOfInsert();
                } else {
                    sql = updateReportSql(saitenKekka);
                    params = saitenKekka.toArrayOfUpdate();
                }
            } else {
                // �e�X�g�̏ꍇ
                sql = "INSERT INTO rec_test_dat VALUES (?,?,?,?,?,?,?,?,?,current timestamp)";
                params = saitenKekka.toArrayOfInsertOfTest();
            }

            try {
                final int cnt = _qr.update(_db.conn, sql, params);
                if (cnt != 1) {
                    log.warn("�X�V������1���̃n�Y�����Ⴄ:" + cnt + ", sql=" + sql);
                }
            } catch (final SQLException e) {
                /* SQL0803N
                 *   INSERT �X�e�[�g�����g�AUPDATE �X�e�[�g�����g�� 1 �ȏ�̒l�A
                 *   �܂��� DELETE �X�e�[�g�����g�ɂ���čs��ꂽ�O���L�[�̍X�V��
                 *   1 ���L�[�A�ŗL����܂��͌ŗL�����𔺂��\�̂ŏd���s���쐬���邽�߁A
                 *   �L���ł͂���܂���B
                 * sqlcode:  -803(�x���_�[�ŗL�̗�O�R�[�h)
                 * sqlstate:  '23505'
                 */
                if (-803 == e.getErrorCode() && e.getSQLState().equals("23505")) {
                    log.warn("�d���G���[�B���Ƀ��R�[�h������B" + sql);
                } else {
                    log.fatal("SQL�R�}���h���s�ŃG���[!", e);
                }
            }
        }
        _db.commit();
    }

    private SaitenKekka createSaitenKekka(final String[] line) {
        /*
         * �w�Z�敪,���k�ԍ�,�w�K���_,�N���X,���w��,
         * ����,�w�Џ��,�R�[�X,�ے��R�[�h,�ے�,
         * ����ے��K�p�N�x�R�[�h,����ے�,�N�x,���ȃR�[�h,����,
         * �ȖڃR�[�h,�Ȗ�,���C���ԃR�[�h,���C����,���C�ۑ��ʃR�[�h,
         * ���C�ۑ���,���{�ԍ�,�ۑ背�x��,��o�N����,���񓾓_,
         * �ǎ����_,��o��
         */
        final String schoolDiv = line[0];
        final String schregno = line[1];
        final String curriculumCd = Curriculum.getCurriculumCd(line[10]);
        final String year = line[12];
        final String classCd = line[13];
        final String subclassCd = line[15];
        final String risyu�ۑ���cd = line[19];
        final String jissiBangou = line[21];
        final String teisyutuYMD = line[23];
        final String firstScore = line[24];
        final String secondScore = line[25];
        final String staffCd = StaffCd;

        SaitenKekka saitenKekka = null;
        try {
            saitenKekka = new SaitenKekka(
                    schoolDiv,
                    schregno,
                    year,
                    curriculumCd,
                    classCd,
                    subclassCd,
                    risyu�ۑ���cd,
                    jissiBangou,
                    teisyutuYMD,
                    firstScore,
                    secondScore,
                    staffCd
            );
        } catch (final IllegalArgumentException e) {
            log.fatal("�s���ȃf�[�^�B", e);
        }
        return saitenKekka;
    }

    private String updateReportSql(final SaitenKekka kekka) {
        final String sql;
        sql = "UPDATE rec_report_dat SET"
            + " commited_date2=?,"
            + " commited_score2=?"
            + " WHERE year=?"
            + " AND classcd=?"
            + " AND curriculum_cd=?"
            + " AND subclasscd=?"
            + " AND schregno=?"
            + " AND report_seq=?";
        return sql;
    }

    private class SaitenKekka {
        private final String _schoolDiv;
        private final String _schregno;
        private final String _year;
        private final String _curriculumCd;
        private final String _classCd;
        private final String _subclassCd;
        private final String _risyu�ۑ���cd;

        /** ���{�ԍ��B �e�X�g�̎��� 90=9��, 91=3�� */
        private final String _jissiBangou;
        private final String _teisyutuYMD;
        private final String _firstScore;
        private final String _secondScore;
        private final String _staffCd;

        private final boolean _isReport;

        public SaitenKekka(
                final String schoolDiv,
                final String schregno,
                final String year,
                final String curriculumCd,
                final String classCd,
                final String subclassCd,
                final String risyu�ۑ���cd,
                final String jissiBangou,
                final String teisyutuYMD,
                final String firstScore,
                final String secondScore,
                final String staffCd
        ) {
            if (classCd.length() != 2) {
                throw new IllegalArgumentException("���ȃR�[�h���s��:" + classCd);
            }
            if (subclassCd.length() != 4) {
                throw new IllegalArgumentException("�ȖڃR�[�h���s��:" + subclassCd);
            }
            if (teisyutuYMD.length() != 10) {
                throw new IllegalArgumentException("��o�N�������s��:" + teisyutuYMD);
            }

            _schoolDiv = schoolDiv;
            _schregno = schregno;
            _year = year;
            _curriculumCd = curriculumCd;
            _classCd = classCd;
            _subclassCd = classCd + subclassCd; // ���ȃR�[�h+�ȖڃR�[�h
            _risyu�ۑ���cd = risyu�ۑ���cd;
            _jissiBangou = jissiBangou;
            _teisyutuYMD = teisyutuYMD.replace('/', '-');
            _firstScore = firstScore;
            _secondScore = secondScore;
            _staffCd = staffCd;

            // validate
            if (classCd.length() != 2) {
                throw new IllegalArgumentException("���ȃR�[�h���s��:" + classCd);
            }
            if (subclassCd.length() != 4) {
                throw new IllegalArgumentException("�ȖڃR�[�h���s��:" + subclassCd);
            }

            if (!"0".equals(_schoolDiv) && !"1".equals(_schoolDiv)) {
                throw new IllegalArgumentException("�w�Z�敪���z��O:" + _schoolDiv);
            }
            if (!_param.getYear().equals(_year)) {
                throw new IllegalArgumentException("���N�x�ȊO:" + _year);
            }
            if (!"1".equals(_risyu�ۑ���cd) && !"2".equals(_risyu�ۑ���cd)) {
                throw new IllegalArgumentException("���C�ۑ莯��CD�� 1 or 2 �ł͂Ȃ�:" + _risyu�ۑ���cd);
            }
            // TAKAESU: �Ȗڃ}�X�^�ɂ��邩�`�F�b�N(����+�ے�+�Ȗ�)
            // TAKAESU: ���k�͑��݂��邩�`�F�b�N
            // TAKAESU: �e�X�g�̎��́A�ǎ����_������Ζ���
            // TAKAESU: ���񓾓_�A�ǎ����_�͂��Âꂩ�ɂ̂ݒl������͂�
            //
            _isReport = ("1".equals(_risyu�ۑ���cd)) ? true : false;
            if (!_isReport) {
                if (!"90".equals(jissiBangou) && !"91".equals(jissiBangou)) {
                    // 90=9��, 91=3��
                    throw new IllegalArgumentException("�e�X�g�̎��͎��{�ԍ��� 90 or 91 �̂͂��Ȃ̂ɈႤ:" + _risyu�ۑ���cd);
                }
            }
        }

        public boolean validScore() {
            if (StringUtils.isEmpty(_firstScore) && StringUtils.isEmpty(_secondScore)) {
                return false;
            }
            if (!StringUtils.isEmpty(_firstScore) && !StringUtils.isEmpty(_secondScore)) {
                return false;
            }
            return true;
        }

        /**
         * ���񓾓_�������True�B
         * @deprecated
         * @return �ǎ��̃f�[�^�Ȃ�False
         */
        public boolean isFirst() {
            return !StringUtils.isEmpty(_firstScore);
        }

        /**
         * �Ē�o(2���)���_������� true�B
         * @return �Ē�o(2���)���_
         */
        public boolean isSecond() {
            return !StringUtils.isEmpty(_secondScore);
        }

        public String getSecondScoreValue() {
            return getDbValue(_secondScore);
        }

        public String getFirstScoreValue() {
            return getDbValue(_firstScore);
        }

        private String getDbValue(String score) {
            if (StringUtils.isEmpty(score)) {
                return null;
            }
            return score;
        }

        /**
         * �J�É�(REC_REPORT_DAT.REPORT_SEQ)�𓾂�B
         * @return �J�É�
         */
        public String getReportSeq() {
            return _jissiBangou;
        }

        /**
         * ���̃R�[�h�𓾂�B
         * @return ���̃R�[�h
         */
        public String getMonth() {
            return "90".equals(_jissiBangou) ? "09" : "03";
        }

        /**
         * ���|�[�g�̃f�[�^��?
         * @return ���|�[�g�̃f�[�^�Ȃ�true
         */
        public boolean isReport() {
            return _isReport;
        }

        public String getType() {
            return isReport() ? "���|�[�g" : "�e�X�g";
        }

        public Object[] toArrayOfInsert() {
            final Object[] rtn = {
                    _year,
                    _classCd,
                    _curriculumCd,
                    _subclassCd,
                    _schregno,
                    getReportSeq(),
                    _teisyutuYMD,
                    null,
                    getFirstScoreValue(),
                    getSecondScoreValue(),
                    MarkserverString,
                    _staffCd,
            };
            return rtn;
        }

        public Object[] toArrayOfUpdate() {
            final Object[] rtn = {
                    _teisyutuYMD,
                    _secondScore,
                    _year,
                    _classCd,
                    _curriculumCd,
                    _subclassCd,
                    _schregno,
                    _jissiBangou,
            };
            return rtn;
        }

        public Object[] toArrayOfInsertOfTest() {
            final Object[] rtn = {
                    _year,
                    _classCd,
                    _curriculumCd,
                    _subclassCd,
                    _schregno,
                    getMonth(),
                    _firstScore,    // TODO: �e�X�g�͏�ɏ��񓾓_��?
                    MarkserverString,
                    _staffCd,
            };
            return rtn;
        }

        public String toString() {
            return "schregno=" + _schregno + ", �Ȗ�CD=" + _subclassCd + ", ���=" + getType();
        }
    }
} // InputCsv

// eof
