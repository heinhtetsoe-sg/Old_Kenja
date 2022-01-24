// kanji=漢字
/*
 * $Id: aa0d57fecbffc11391e010636979a27a519ca971 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 *
 * DBNAME=//puma:50000/KM090327 category_selected=06010101 SEKI=00999999 DATE=2009/03/27 OUTPUT=2 MIRISYU=1 GRADE_HR_CLASS=03001 KANJI=1 YEAR=2008 PRGID=KNJE070 OS=1 GAKKI=3 CTRL_YEAR=2008
 * DBNAME=//tokio:50000/R1214TE category_selected=20051021 SEKI=00999999 DATE=2006/06/17 OUTPUT=1 MIRISYU=1 GRADE_HR_CLASS=02001 KANJI=1 YEAR=2006 PRGID=KNJE070 OS=1 GAKKI=1 CTRL_YEAR=2006
 */

package servletpack.KNJE;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/*
 *  学校教育システム 賢者 [進路情報管理]  高校用調査書  就職用
 */

public class KNJE070_2 extends KNJE070_1 {

    private static final Log log = LogFactory.getLog(KNJE070_2.class);
    
    private static final String revision = "$Revision: 62901 $ $Date: 2018-10-19 11:32:01 +0900 (金, 19 10 2018) $";
    
    private KNJE070_1.KNJE070_2P _knje070_2p;
    
    public KNJE070_2(final DB2UDB db2, final Vrw32alpWrap svf, final KNJDefineSchool definecode, final String useSyojikou3) {
    	super(db2, svf, definecode, useSyojikou3);
        _knje070_2p = new KNJE070_1.KNJE070_2P(db2, svf, definecode, useSyojikou3);
        log.fatal(revision);
    }
    
    public KNJE070_2(final DB2UDB db2, final List csvOutputLines, final KNJDefineSchool definecode, final String useSyojikou3) {
    	super(db2, csvOutputLines, definecode, useSyojikou3);
        _knje070_2p = new KNJE070_1.KNJE070_2P(db2, csvOutputLines, definecode, useSyojikou3);
        log.fatal(revision);
    }
    
    /**
     *  PrepareStatement作成
     */
    public void pre_stat(final String hyotei, final Map paramap) {
        _knje070_2p.pre_stat(hyotei, paramap);
    }
    
    public void printSvf(
            final String schregno,
            final String year,
            final String semes,
            final String date,
            final String staffCd,
            final String kanji,
            final String comment,
            final String os,
            final String certifNumber,
            final Map paramap
    ) {
        _knje070_2p.printSvf(schregno, year, semes, date, staffCd, kanji, comment, os, certifNumber, paramap);
        if (_knje070_2p.nonedata) {
            nonedata = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    protected SqlStudyrec getPreStatementStudyrec(
            final String schregno,
            final String year,
            final Map paramap
    ) {
        return _knje070_2p.getPreStatementStudyrec(schregno, year, paramap);
    }
    
    /**
     *  PrepareStatement close
     */
    public void pre_stat_f() {
        _knje070_2p.pre_stat_f();
    }
}
