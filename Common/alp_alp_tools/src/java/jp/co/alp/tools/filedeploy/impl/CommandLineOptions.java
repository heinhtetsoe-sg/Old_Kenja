// kanji=����
/*
 * $Id: CommandLineOptions.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * �쐬��: 2006/06/30 11:18:16 - JST
 * �쐬��: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools.filedeploy.impl;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.tools.filedeploy.Options;

/**
 * �R�}���h���C���Łu�I�v�V������͂ƕێ��v�B
 * @author tamura
 * @version $Id: CommandLineOptions.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public class CommandLineOptions extends Options {
    /*pkg*/static final Log log = LogFactory.getLog(CommandLineOptions.class);

    private List _args;

    public CommandLineOptions() {
        super();
    }

    public void setArgs(final String[] args) {
        _args = argsToList(args);
        parse();
    }

    private static void printUsage(final PrintStream ps) {
        ps.println();
        ps.println("usage: kfu [�I�v�V����...] [�f�B���N�g��...]");
        ps.println("  �I�v�V����: �ȉ��̂��Âꂩ��܂��͑g�ݍ��킹���w��ł��܂��B");
        ps.println();
        ps.println("    -deploy");
        ps.println("    -copy");
        ps.println("      ���ۂɃR�s�[�����s���܂��B");
        ps.println();
        ps.println("    -showInfo");
        ps.println("    -info");
        ps.println("      ����\�����܂��B");
        ps.println();
        ps.println("    -compTS");
        ps.println("      �t�@�C���̃^�C���X�^���v(TS)���r���܂��B");
        ps.println();
        ps.println("    -diff");
        ps.println("      �t�@�C���̓��e���r���A1�o�C�g�ł��قȂ�΁u�O����r�c�[���v���N�����܂��B");
        ps.println();
        ps.println("    -help");
        ps.println("      �ȒP�ȃw���v(����)��\�����܂�(���̃I�v�V������f�B���N�g���w��͖����ɂȂ�)�B");
        ps.println();
        ps.println("  �f�B���N�g��: ��܂��͕����̃f�B���N�g�����w��ł��܂��B");
        ps.println("              : �f�B���N�g��������w�肵�Ȃ������ꍇ�́A");
        ps.println("              : ���݂̃f�B���N�g�����w�肳�ꂽ���ƂɂȂ�܂��B");
    }

    private void parse() {
        boolean option = true;

        for (final Iterator it = _args.iterator(); it.hasNext();) {
            final String arg = (String) it.next();
            if (StringUtils.isEmpty(arg)) {
                continue;
            }
            if ("-help".equalsIgnoreCase(arg)) {
                printUsage(System.out);
                return;
            }
        }

        for (final Iterator it = _args.iterator(); it.hasNext();) {
            final String arg = (String) it.next();
            if (StringUtils.isEmpty(arg)) {
                continue;
            }

            if (option) {
                if ('-' == arg.charAt(0)) {
                    if ("-deploy".equalsIgnoreCase(arg) || "-copy".equalsIgnoreCase(arg)) {
                        _deploy = true;
                        continue;
                    }
                    if ("-showInfo".equalsIgnoreCase(arg) || "-info".equalsIgnoreCase(arg)) {
                        _showInfo = true;
                        continue;
                    }
                    if ("-compTS".equalsIgnoreCase(arg)) {
                        _compTS = true;
                        continue;
                    }
                    if ("-diff".equalsIgnoreCase(arg)) {
                        _diff = true;
                        continue;
                    }

                    log.warn("�s���ȃI�v�V����:" + arg);
                } else {
                    option = false;
                }
            }

            if (!option){
                addDir(arg);
            }
        }

        if (getDirs().isEmpty()) {
            addDir(".");
        }
    }

    private static List argsToList(final String args[]) {
        final List rtn = new LinkedList();
        if (null != args && 0 != args.length) {
            for (int i = 0; i < args.length; i++) {
                rtn.add(args[i]);
            }
        }
        return rtn;
    }
} // CommandLineOptions

// eof
