// kanji=äøéö
/*
 * $Id: Schedule.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * çÏê¨ì˙: 2008/04/24 14:43:39 - JST
 * çÏê¨é“: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.nbi.groupware.domain;

import java.sql.Date;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * éûä‘äÑÅB
 * @author takaesu
 * @version $Id: Schedule.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class Schedule {
    private final Date _date;
    private final String _periodCd;
    private final String _chairCd;
    private final boolean _executed;

    private Chair _chair;

    public Schedule(final Date date, final String periodCd, final String chairCd, final boolean executed) {
        _date = date;
        _periodCd = periodCd;
        _chairCd = chairCd;
        _executed = executed;
    }

    public Date getDate() { return _date; }
    public String getPeriodCd() { return _periodCd; }
    public String getchairCd() { return _chairCd; }
    public boolean isExecuted() { return _executed; }
    public Chair getChair() { return _chair; }
    public void setChair(Chair chair) { _chair = chair; }

    public String toString() {
        return _date + ", " + _periodCd + ", chairCd=" + _chairCd + ", executed=" + _executed;
    }
} // Schedule

// eof
