// kanji=����
/*
 * $Id: IKnj.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * �쐬��: 2008/09/05 18:23:15 - JST
 * �쐬��: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.util.Map;

/**
 * <<�N���X�̐���>>�B
 * @author takaesu
 * @version $Id: IKnj.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public interface IKnj {

    /**
     * Map�̃f�[�^��z��ɕϊ�����B
     * @param map org.apache.commons.dbutils.handlers.MapListHandler �� Map
     * @return �ϊ����ꂽ�f�[�^
     */
    Object[] mapToArray(Map map);

}
// eof
