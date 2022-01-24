// kanji=漢字
/*
 * $Id: de9679b1f95dbe2eb883fc1330cdacb4d4510453 $
 *
 * 作成日: 2009/10/19 18:14:45 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.util.Iterator;
import java.util.List;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.KNJE380.Param;
import servletpack.KNJE.KNJE380.Student;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: de9679b1f95dbe2eb883fc1330cdacb4d4510453 $
 */
public class KNJE380_2 extends KNJE380Abstract {

    /**
     * コンストラクタ。
     * @param param
     * @param db2
     * @param svf
     */
    public KNJE380_2(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJE380_2.class");

    protected boolean printMain(final List printStudents, final String majorCd) throws SQLException {
        boolean hasData = false;
        PrintData gradPlan = new PrintData();
        PrintData singaku = new PrintData();
        PrintData shushokuS = new PrintData();
        PrintData shushokuP = new PrintData();
        PrintData shushokuM = new PrintData();
        PrintData nouryokuKaihtu = new PrintData();
        PrintData senmon = new PrintData();
        PrintData sonota = new PrintData();
        for (final Iterator itPrint = printStudents.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            gradPlan = setCnt(gradPlan, student);
            if (student._isShushoku) {
                if (student._aftGradCourseDatShu._introductionDiv.equals(INTRODUCT_S)) {
                    shushokuS = setCnt(shushokuS, student);
                } else if (student._aftGradCourseDatShu._introductionDiv.equals(INTRODUCT_P)) {
                    shushokuP = setCnt(shushokuP, student);
                } else if (student._aftGradCourseDatShu._introductionDiv.equals(INTRODUCT_M)) {
                    shushokuM = setCnt(shushokuM, student);
                } else {
                    sonota = setCnt(sonota, student);
                }
            } else if (student._isSingaku) {
                if (student._aftGradCourseDatSin._schoolGroup.equals(KOKURITU_DAI) ||
                    student._aftGradCourseDatSin._schoolGroup.equals(KOURITU_DAI) ||
                    student._aftGradCourseDatSin._schoolGroup.equals(SIRITU_DAI) ||
                    student._aftGradCourseDatSin._schoolGroup.equals(KOURITU_TAN) ||
                    student._aftGradCourseDatSin._schoolGroup.equals(SIRITU_TAN)
                ) {
                    singaku = setCnt(singaku, student);
                } else if (student._aftGradCourseDatSin._schoolGroup.equals(SENMON) ||
                           student._aftGradCourseDatSin._schoolGroup.equals(KANGO)
                ) {
                    senmon = setCnt(senmon, student);
                } else if (student._aftGradCourseDatSin._schoolGroup.equals(DAIGAKU_KO) ||
                           student._aftGradCourseDatSin._schoolGroup.equals(NOURYOKU_KAIHATU)
                ) {
                    nouryokuKaihtu = setCnt(nouryokuKaihtu, student);
                } else {
                    sonota = setCnt(sonota, student);
                }
            } else {
                sonota = setCnt(sonota, student);
            }
            hasData = true;
        }
        _svf.VrSetForm("KNJE380_2.frm", 1);
        _svf.VrsOut("SCHOOL_NAME", _param._schoolMst._schoolName1);
        _svf.VrsOut("MAJORNAME", _param.getMajorName(majorCd));
        _svf.VrsOut("GRAD_YEAR", _param.changePrintYear(_param._ctrlYear) + "度　");
        int fieldCnt = 1;
        printOut(fieldCnt++, gradPlan);
        printOut(fieldCnt++, singaku);
        printOut(fieldCnt++, shushokuS);
        printOut(fieldCnt++, shushokuP);
        printOut(fieldCnt++, shushokuM);
        printOut(fieldCnt++, nouryokuKaihtu);
        printOut(fieldCnt++, senmon);
        printOut(fieldCnt++, sonota);
        _svf.VrEndPage();
        return hasData;
    }

    private void printOut(final int fieldCnt, final PrintData data) {
        _svf.VrsOutn("TOTAL", fieldCnt, String.valueOf(data._totalCnt));
        _svf.VrsOutn("MAN", fieldCnt, String.valueOf(data._manCnt));
        _svf.VrsOutn("WOMAN", fieldCnt, String.valueOf(data._woManCnt));
    }
}

// eof
