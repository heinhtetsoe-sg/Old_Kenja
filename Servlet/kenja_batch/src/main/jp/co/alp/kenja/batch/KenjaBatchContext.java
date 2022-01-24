/*
 * $Id: KenjaBatchContext.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2015/06/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch;

import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.util.KenjaParameters;

public interface KenjaBatchContext {

    KenjaParameters getKenjaParameters();
    
    KenjaDateImpl getDate();
    
    void setControlMaster(ControlMaster cm);

    ControlMaster getControlMaster();
}
