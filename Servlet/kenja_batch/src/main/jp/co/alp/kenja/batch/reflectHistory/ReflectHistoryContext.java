/*
 * $Id: ReflectHistoryContext.java 74552 2020-05-27 04:41:22Z maeshiro $
 *
 * 作成日: 2015/06/15
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.reflectHistory;

import jp.co.alp.kenja.batch.KenjaBatchContext;
import jp.co.alp.kenja.common.domain.ControlMaster;
import jp.co.alp.kenja.common.domain.KenjaDateImpl;
import jp.co.alp.kenja.common.util.KenjaParameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReflectHistoryContext implements KenjaBatchContext {

    private static Log log = LogFactory.getLog(ReflectHistoryContext.class);

    private final ReflectHistoryParameters _parameters;
    private ControlMaster _cm;

    public ReflectHistoryContext(final ReflectHistoryParameters parameters) {
        _parameters = parameters;
    }

    public KenjaDateImpl getDate() {
        return _parameters.getKenjaDate();
    }

    public KenjaParameters getKenjaParameters() {
        return _parameters;
    }

    public void setControlMaster(final ControlMaster cm) {
        _cm = cm;
    }

    public ControlMaster getControlMaster() {
        return _cm;
    }
}
