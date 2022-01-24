// kanji=����
/*
 * $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/07/15 14:20:49 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.withus.Curriculum;

/**
 * �p�����[�^�B
 * @author takaesu
 * @version $Id: Param.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Param {

    /*pkg*/static final Log log = LogFactory.getLog(Param.class);

    /** �o�^�҃R�[�h. */
    public static final String REGISTERCD = "00999990";

    private final String _dbUrl;
    public final List _classes = new ArrayList();

    /** [�u��ҘA�ԁ����w�Дԍ�]�̑Ή�. Key�͑O�� */
    private final Map _seito = new HashMap();

    /** �����̑Ή� */
    private final Map _zokugara = new HashMap();

    /** [����ے��N�x��������ے��R�[�h]�̑Ή�. Key�͑O�� */
    private final Map _curriculumTable = new TreeMap();

    /** �w���敪�̑Ή� */
    private final Map _studentDiv = new HashMap();

    /** �R�[�X�R�[�h�̑Ή�*/
    private final Map _courseCode = new HashMap();
    private static final String _BELONG01 = "001";
    private static final String _COURSE0001 = "0001";
    private static final String _COURSE0002 = "0002";

    public Param(final String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Main <//localhost:50000/witestdb> <�ڍs�v���O�����N���X�t�@�C��...>");
            throw new IllegalArgumentException("�����̐����Ⴄ");
        }

        _dbUrl = args[0];

        for (int i = 1; i < args.length; i++) {
            final char moji = args[i].charAt(0);
            if (!isAlphabet(moji)) {
                continue;
            }
            _classes.add(args[i]);
        }

        /** �����Z�b�g */
        _zokugara.put("101", "01");
        _zokugara.put("102", "02");
        _zokugara.put("401", "03");
        _zokugara.put("402", "04");
        _zokugara.put("451", "05");
        _zokugara.put("452", "06");
        _zokugara.put("011", "07");
        _zokugara.put("012", "08");
        _zokugara.put("200", "09");
        _zokugara.put("501", "11");
        _zokugara.put("502", "12");
        _zokugara.put("701", "13");
        _zokugara.put("702", "14");
        _zokugara.put("600", "90");
        _zokugara.put("801", "90");
        _zokugara.put("802", "90");
        _zokugara.put("900", "90");

        /** �w���敪�Z�b�g */
        _studentDiv.put("1010101", "01");
        _studentDiv.put("1010103", "01");
        _studentDiv.put("1020101", "01");
        _studentDiv.put("1020103", "01");
        _studentDiv.put("1010102", "02");
        _studentDiv.put("1010104", "02");
        _studentDiv.put("1020102", "02");
        _studentDiv.put("1020104", "02");
        _studentDiv.put("1010105", "05");
        _studentDiv.put("1020105", "05");
        _studentDiv.put("1010106", "06");
        _studentDiv.put("1020106", "06");
        _studentDiv.put("1020121", "07");
        _studentDiv.put("1020123", "07");
        _studentDiv.put("1010107", "08");
        _studentDiv.put("1020107", "08");
        _studentDiv.put("1020108", "08");
        _studentDiv.put("9999999", "99");

        /** �R�[�X�R�[�h�Z�b�g*/
        _courseCode.put("1020121", _COURSE0002);
        _courseCode.put("1020123", _COURSE0002);
    }

    public static boolean isAlphabet(final char moji) {
        return (moji >= 'a' && moji <= 'z') || (moji >= 'A' && moji <= 'Z');
    }

    public AbstractKnj createKnj(final String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final Class hoge;
        try {
            hoge = Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw e;
        }

        final Object newInstance;
        try {
            newInstance = hoge.newInstance();
        } catch (final InstantiationException e) {
            throw e;
        } catch (final IllegalAccessException e) {
            throw e;
        }
        return (AbstractKnj) newInstance;
    }

    public String getDbUrl() {
        return _dbUrl;
    }

    public void load(DB2UDB db) throws SQLException {
        Curriculum.loadCurriculumMst(db);
        log.warn("���̃}�X�^�̋���ے�����Ǎ��񂾁B");

        loadIcassSeito(db);
    }

    private void loadIcassSeito(DB2UDB db) throws SQLException {
        try {
            final String sql = "SELECT "
                             + "    shigansha_renban, "
                             + "    shigansha_no, "
                             + "    seito_no, "
                             + "    CASE WHEN VALUE(shigansha_no, '') = '' "
                             + "         THEN '' "
                             + "         ELSE RIGHT(RTRIM('0000000' || shigansha_no), 7) "
                             + "    END AS applicantno "
                             + " FROM "
                             + "    seito";
            db.query(sql);
            ResultSet rs = db.getResultSet();
            while (rs.next()) {
                final String shigansyaRenban = rs.getString("shigansha_renban");
                final String shiganshaNo = rs.getString("shigansha_no");
                final String seitoNo = rs.getString("seito_no");
                final String applicantNo = rs.getString("applicantno");
                _seito.put(shigansyaRenban, new Seito(shiganshaNo, seitoNo, applicantNo));
            }
            db.commit();
            rs.close();
        } catch (final SQLException e) {
            log.fatal("ICASS���k�̓Ǎ��݂ŃG���[");
            throw e;
        }
        log.debug("ICASS:���k��=" + _seito.keySet().size());
    }

    private class Seito {
        final String _shiganshano;
        final String _seitoNo;
        final String _applicantNo;
        /**
         * �R���X�g���N�^�B
         */
        public Seito(final String shiganshaNo, final String seitoNo, final String applicantNo) {
            _shiganshano = shiganshaNo;
            _seitoNo = seitoNo;
            _applicantNo = applicantNo;
        }
    }

    /**
     * �w�Дԍ��𓾂�B
     * @param shiganshaRenban �u��ҘA��
     * @return �w�Дԍ�
     */
    public String getSchregno(final String shiganshaRenban) {
        final Seito seito = (Seito) _seito.get(shiganshaRenban);
        if (null == seito || "".equals(seito._seitoNo)) {
            return null;
        }
        return (String) seito._seitoNo;
    }

    /**
     * �u��Ҕԍ��𓾂�B
     * @param shiganshaRenban �u��ҘA��
     * @return �w�Дԍ�
     */
    public String getApplicantNo(final String shiganshaRenban) {
        final Seito seito = (Seito) _seito.get(shiganshaRenban);
        return (String) seito._applicantNo;
    }

    /**
     * �����𓾂�B
     * @param zokugara ICASS����
     * @return ���ґ���
     */
    public String getZokugara(final String zokugara) {
        return _zokugara.containsKey(zokugara) ? (String) _zokugara.get(zokugara) : "11";
    }

    /**
     * �w���敪�𓾂�B
     * @param GAKUSHU_KYOTEN_CODE ICASS�w�K���_�R�[�h
     * @param course_mst ICASS�R�[�X�}�X�^
     * @return ���Ҋw���敪
     */
    public String getStudentDiv(final String GAKUSHU_KYOTEN_CODE,final String course_mst) {

        if (GAKUSHU_KYOTEN_CODE == null) {
            return null;
        }
        
        int gakushuKyotenCd = Integer.valueOf(GAKUSHU_KYOTEN_CODE).intValue();       
        String studentDiv = (String) _studentDiv.get(course_mst);

        if ("05".equals(studentDiv) || "06".equals(studentDiv) || "99".equals(studentDiv)) {
            return studentDiv;
        }

        if ( gakushuKyotenCd == 1 ) {
            if ("01".equals(studentDiv) || "07".equals(studentDiv)) {
                return "01";
            }
            return null;
        }
        if (2 <= gakushuKyotenCd && gakushuKyotenCd <= 40) {
            if ("01".equals(studentDiv)) {
                return "03"; // �O�R�l��
            } else if ("08".equals(studentDiv) && gakushuKyotenCd == 38) {
                return "07"; // (�O�W)�b�o�R�[�X���͊w�K���_�R�[�h=38�łȂ���΂Ȃ�Ȃ��B
            } else if ("02".equals(studentDiv)) {
                return studentDiv;
            }
            return null;
        }
        if (gakushuKyotenCd == 59 || 151 <= gakushuKyotenCd && gakushuKyotenCd <= 169) {
            return "04"; // �O�S��g�搶
        }
        
        // �w�K���_�R�[�h�Ɋ֘A���Ȃ����̑�
        return studentDiv;
    }

    /**
     * �R�[�X�R�[�h�𓾂�B
     * @param GAKUSHU_KYOTEN_CODE ICASS�w�K���_�R�[�h
     * @param course_mst ICASS�R�[�X�}�X�^
     * @return �R�[�X�R�[�h
     */
    public String getCourseCode(final String GAKUSHU_KYOTEN_CODE, final String course_mst) {
        String courseCode = (String) _courseCode.get(course_mst);
        //        �����̒��ɂ�0001�Ƃ��������Ă���B
        if (null != GAKUSHU_KYOTEN_CODE && GAKUSHU_KYOTEN_CODE.equals(_BELONG01) && null != courseCode) {
            return courseCode;
        } else {
            return _COURSE0001;
        }
    }

    /**
     * �������琫�ʂ𓾂�B
     * @param zokugara ICASS����
     * @return ���ґ���
     */
    public String getZokugaraSex(final String zokugara) {
        final String zoku = getZokugara(zokugara);
        String retSex = "2";
        if (zoku.equals("01") ||
            zoku.equals("03") ||
            zoku.equals("05") ||
            zoku.equals("07") ||
            zoku.equals("09") ||
            zoku.equals("11")
        ) {
            retSex = "1";
        }
        return retSex;
    }
} // Param

// eof
