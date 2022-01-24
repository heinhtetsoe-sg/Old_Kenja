// kanji=����
/*
 * $Id: Mk.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/04/04
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.batch.opencsv.CSVWriter;

/**
 * NBI�̃O���[�v�E�F�A�֘A��CSV�����p�B
 * @author takaesu
 * @version $Id: Mk.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public abstract class Mk {
    /*pkg*/static final Log log = LogFactory.getLog(Mk.class);

    protected final Param _param;
    protected final DB2UDB _db;

    public Mk(final DB2UDB db, final Param param, final String title) {
        _param = param;
        _db = db;

        log.info("��" + title);
    }

    protected void toCsv(final String subject, final String csvFile, final List data) {
        try {
            final String file = _param.getFullPath(csvFile);
            final OutputStream outputStream = new FileOutputStream(file);
            final Writer fileWriter = new PrintWriter(new OutputStreamWriter(outputStream, Param.encode));
            final CSVWriter writer = new CSVWriter(fileWriter, ',', '\0');
            writer.writeAll(data);
            writer.close();
            log.fatal("CSV�t�@�C������:" + file);
        } catch (final UnsupportedEncodingException e) {    // TODO: Exception �̎g�������K�v?
            log.fatal(subject + "�̏��擾�ŃG���[" + e);
        } catch (final FileNotFoundException e) {
            log.fatal(subject + "�̏��擾�ŃG���[" + e);
        } catch (final IOException e) {
            log.fatal(subject + "�̏��擾�ŃG���[" + e);
        }
    }

    /**
     * ������̓�1������������B<br>
     * Ex) "12345678" �� "2345678"
     * @param staffCd �E���R�[�h
     * @return ��1�������������E���R�[�h
     */
    public static String convStaffCd(final String staffCd) {
        if (null == staffCd) {
            return null;
        }
        return staffCd.substring(1);
    }

    // �ȉ��͕s�v�̃n�Y
//    /**
//     * yyyy-mm-dd �ȕ�����̋�؂蕶������������B
//     * @param dateStr ���t�ȕ�����
//     * @return 5�o�C�g�ځA8�o�C�g�ڂ���������������B�ϊ��ł��Ȃ��Ƃ��͂��̂܂܁B
//     */
//    public static String cutDateDelimit(final String dateStr) {
//        if (null == dateStr) {
//            return null;
//        }
//        if (dateStr.length() != 10) {
//            return dateStr;
//        }
//        return dateStr.substring(0, 4) + dateStr.substring(5, 7) + dateStr.substring(8);
//    }
//
//    public static String cutDateDelimit2(final String dateStr) {
//        final String rtn = dateStr.substring(0, 4) + dateStr.substring(5, 7) + dateStr.substring(8, 10);
//        return rtn;
//    }
//    /**
//     * �ȖڃR�[�h�̓�2�o�C�g���J�b�g����B
//     * @param subclassCd �ȖڃR�[�h
//     * @return �Z���Ȃ����ȖڃR�[�h
//     */
//    public static String cutSubclassCd(final String subclassCd) {
//        if (null == subclassCd || subclassCd.length() < 2) {
//            return subclassCd;
//        }
//        final String rtn = subclassCd.substring(2);
//        return rtn;
//    }
} // Mk

// eof
