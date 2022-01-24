// kanji=����
/*
 * $Id: KenjaDirRule.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * �쐬��: 2006/06/28 22:00:21 - JST
 * �쐬��: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools.filedeploy.impl.kenja;

import java.io.File;

import org.apache.oro.text.perl.Perl5Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.tools.filedeploy.DirRule;

/**
 * �u���ҁv�̃f�B���N�g�����̃��[���B
 * @author tamura
 * @version $Id: KenjaDirRule.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public class KenjaDirRule implements DirRule {
    /*pkg*/static final Log log = LogFactory.getLog(KenjaDirRule.class);

    private static final String RE = "m/^KNJ([A-Z]).*$/";
    private static final Perl5Util P5UTIL = new Perl5Util();

    /**
     * �����Ȃ��R���X�g���N�^�B
     * ���t���N�V�����Ő��������̂ŁA�u�����Ȃ��R���X�g���N�^�v�͕K�{�B
     * @see jp.co.alp.tools.filedeploy.FileDeployInfo#createDirRule(String)
     */
    public KenjaDirRule() {
        super();
    }

    /**
     * ���f�B���N�g������A�u���҂̃f�B���N�g�����̃��[���v�ɂ���āA��f�B���N�g������Ԃ��B
     * ��:
     * "KNJX123"    -> "X/KNJX123"
     * "KNJX123_1"  -> "X/KNJX123_1"
     * @param dir ���f�B���N�g��
     * @return ��f�B���N�g����
     * @throws IllegalArgumentException ���f�B���N�g�������[���Ƀ}�b�`���Ȃ��ꍇ
     */
    public String getSrcDir(final File dir) {
        final String name = dir.getName();
        if (!P5UTIL.match(RE, name)) {
            throw new IllegalArgumentException(dir + ":�}�b�`���܂���");
        }
        final String subsysdir = P5UTIL.group(1);
        return subsysdir + "/" + name;
    }
} // KenjaDirRule

// eof
