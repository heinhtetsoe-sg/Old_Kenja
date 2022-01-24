// kanji=漢字
/*
 * $Id: 02ab66d9b485d718a14292d09bd0654d1997b219 $
 *
 * 作成日: 2009/10/19 18:00:33 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.util.List;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.KNJE380.Student;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 02ab66d9b485d718a14292d09bd0654d1997b219 $
 */
public abstract class KNJE380Abstract {

    private static final Log log = LogFactory.getLog("KNJE380Abstract.class");

    protected final KNJE380.Param _param;
    protected final DB2UDB _db2;
    protected final Vrw32alp _svf;

    protected final String MAN = "1";
    /** 紹介区分：学校 */
    protected final String INTRODUCT_S = "1";
    /** 紹介区分：自己・縁故 */
    protected final String INTRODUCT_M = "2";
    /** 紹介区分：公務員 */
    protected final String INTRODUCT_P = "3";

    /** 学校区分 */
    protected final String KOKURITU_DAI = "01";
    protected final String KOURITU_DAI = "02";
    protected final String SIRITU_DAI = "03";
    protected final String KOURITU_TAN = "04";
    protected final String SIRITU_TAN = "05";
    protected final String SENMON = "06";
    protected final String KANGO = "07";
    protected final String DAIGAKU_KO = "08";
    protected final String NOURYOKU_KAIHATU = "09";
    protected final String SONOTA = "99";

    protected final String KEN_NAI = "1";
    protected final String KEN_GAI = "2";

    /**
     * コンストラクタ。
     */
    public KNJE380Abstract(final KNJE380.Param param, final DB2UDB db2, final Vrw32alp svf) {
        _param = param;
        _db2 = db2;
        _svf = svf;
    }

    abstract protected boolean printMain(final List printStudents, final String majorCd) throws SQLException;

    protected PrintData setCnt(final PrintData gradPlan, final Student student) {
        PrintData retData = gradPlan;
        retData._totalCnt++;
        if (student._sexCd.equals(MAN)) {
            retData._manCnt++;
        } else {
            retData._woManCnt++;
        }
        return retData;
    }

    protected class PrintData {
        int _manCnt;
        int _woManCnt;
        int _totalCnt;

        public PrintData() {
            _manCnt = 0;
            _woManCnt = 0;
            _totalCnt = 0;
        }
    }

}

// eof
