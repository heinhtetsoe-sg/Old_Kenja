// kanji=����
/*
 * $Id: AbstractKnj.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/07/15 14:17:31 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;

import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * �f�[�^�ڍs�̋��ʃN���X�B
 * @author takaesu
 * @version $Id: AbstractKnj.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public abstract class AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(AbstractKnj.class);

    public static final DecimalFormat _subClassCdFormat = new DecimalFormat("0000");

    DB2UDB _db2;
    Param _param;

    final MapListHandler _handler = new MapListHandler();
    final QueryRunnerKnj _runner = new QueryRunnerKnj();

    public AbstractKnj() {
    }

    /**
     * �R���X�g���N�^�B
     * @param db2 DB
     * @param param �p�����[�^
     * @deprecated ���t���N�V�����@�\���g���̂Ŕp�~
     */
    public AbstractKnj(DB2UDB db2, final Param param) {
        init(db2, param);
    }

    public void init(final DB2UDB db2, final Param param) {
        _db2 = db2;
        _param = param;
        _runner.init(db2);
    }

    /**
     * �f�[�^���ڍs����B
     */
    abstract void migrate() throws SQLException;

    /**
     * �^�C�g���𓾂�B
     * @return �^�C�g��
     */
    abstract String getTitle();

    /**
     * �^�C�g�������O�o�͂�����A�f�[�^���ڍs����B
     */
    public void migrateData() throws SQLException {
        log.info("��" + getTitle());
        migrate();
    }

    protected static String getInsertVal(final String str) {
        if (null != str && 0 < str.length()) {
            return "'" + str + "'";
        } else {
            return "null";
        }
    }

    protected static String getInsertChangeVal(final String str) {
        if (null != str && 0 < str.length()) {
            final StringBuffer retStr = new StringBuffer();
            final char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '\'') {
                    retStr.append("''");
                } else {
                    retStr.append(chars[i]);
                }
            }
            return "'" + retStr.toString() + "'";
        } else {
            return "null";
        }
    }

    protected static String getInsertVal(final Integer str) {
        if (null != str) {
            return str.toString();
        } else {
            return "null";
        }
    }

    /**
     * ����(�S���p)�̏o���ӏ��ŕ�����𕪊�
     * @param str
     * @return
     */
    protected static String[] divideStr(final String str) {
        String[] retStr = new String[2];
        retStr[0] = str;
        retStr[1] = "";
        if (null == str) {
            return retStr;
        }

        char[] strArray = str.toCharArray();
        int divideCnt = 0;
        for (int i = 0; i < strArray.length; i++) {
            if (strArray[i] == '0' || strArray[i] == '1' ||
                strArray[i] == '2' || strArray[i] == '3' ||
                strArray[i] == '4' || strArray[i] == '5' ||
                strArray[i] == '6' || strArray[i] == '7' ||
                strArray[i] == '8' || strArray[i] == '9' ||
                strArray[i] == '�O' || strArray[i] == '�P' ||
                strArray[i] == '�Q' || strArray[i] == '�R' ||
                strArray[i] == '�S' || strArray[i] == '�T' ||
                strArray[i] == '�U' || strArray[i] == '�V' ||
                strArray[i] == '�W' || strArray[i] == '�X'
                
            ) {
                divideCnt = i;
                break;
            }
        }
    
        if (0 < divideCnt) {
            retStr[0] = str.substring(0, divideCnt);
            retStr[1] = str.substring(divideCnt);
        }
        return retStr;
    }

    /**
     * �����񂩂�A�w�蕶�����폜
     */
    protected static String deleteStr(final String str, final String delStr) {
        if (null == str) {
            return null;
        }
        final StringBuffer retStb = new StringBuffer(str);
        if (0 < ((String) retStb.toString()).indexOf(delStr)) {
            for (; 0 < ((String) retStb.toString()).indexOf(delStr);) {
                final int delIndex = ((String) retStb.toString()).indexOf(delStr);
                retStb.delete(delIndex, delIndex + 1);
            }
        }
        return retStb.toString();
    }

    /**
     * �w�肳�ꂽ�������ƁA�������ŕ�����𕪊�����B
     * �w��𒴂������́A�̂Ă�B
     * @param str�F������
     * @param dividlen�F����������
     * @param dividnum�F������
     * @return �������������z��
     */
    //TODO: StringUtils.split �������悤�ȓ���������
    public static String[] retDividString(final String str, final int dividlen, final int dividnum) {
        String[] retStr = new String[dividnum];
        if (str == null || 0 == str.length()) {
            return retStr;
        }
    
        char[] strArray = str.toCharArray();
        int divideCnt = 0;
        int dividnumCnt = 0;
        for (int i = 0; i < strArray.length; i++) {
            divideCnt++;
            if (null == retStr[dividnumCnt]) {
                retStr[dividnumCnt] = "";
            }
            retStr[dividnumCnt] += strArray[i];
            if (divideCnt == dividlen) {
                dividnumCnt++;
                divideCnt = 0;
            }
            if (dividnum <= dividnumCnt) {
                break;
            }
        }
        return retStr;
    }

    /**
     * ���Ԃ̌����𓾂�B
     * @param fromDate �J�n���t
     * @param toDate �I�����t
     * @return �����B��) 2004-03-xx & 2005-09-xx �� 19
     */
    public static Integer get����(final String fromDate, final String toDate) {
        final int fYear = Integer.parseInt(fromDate.substring(0, 4));
        final int fMonth = Integer.parseInt(fromDate.substring(5, 7));

        final int tYear = Integer.parseInt(toDate.substring(0, 4));
        final int tMonth = Integer.parseInt(toDate.substring(5, 7));

        int ans = (tYear - fYear) * 12 + tMonth - fMonth;
        ans += 1;

        return new Integer(ans);
    }
} // AbstractKnj

// eof
