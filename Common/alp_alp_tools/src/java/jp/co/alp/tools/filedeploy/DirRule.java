// kanji=����
/*
 * $Id: DirRule.java 56576 2017-10-22 11:25:31Z maeshiro $
 *
 * �쐬��: 2006/06/28 21:58:56 - JST
 * �쐬��: tamura
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.tools.filedeploy;

import java.io.File;

/**
 * �f�B���N�g�����̃��[���B
 * @author tamura
 * @version $Id: DirRule.java 56576 2017-10-22 11:25:31Z maeshiro $
 */
public interface DirRule {
    String getSrcDir(final File dir);
} // DirRule

// eof
