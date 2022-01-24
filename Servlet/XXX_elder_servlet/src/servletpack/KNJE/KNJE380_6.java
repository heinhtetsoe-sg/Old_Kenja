// kanji=漢字
/*
 * $Id: 462d65edb06391544f9b2287fcdaede3de97e38b $
 *
 * 作成日: 2009/10/19 18:14:45 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.math.BigDecimal;
import java.util.ArrayList;
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
 * @version $Id: 462d65edb06391544f9b2287fcdaede3de97e38b $
 */
public class KNJE380_6 extends KNJE380Abstract {

    /**
     * コンストラクタ。
     * @param param
     * @param db2
     * @param svf
     */
    public KNJE380_6(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJE380_6.class");

    protected boolean printMain(final List printStudents, final String majorCd) throws SQLException {
        boolean hasData = false;

        List gradeList = new ArrayList();
        String befGrade = "";
        for (final Iterator iter = printStudents.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            if (!befGrade.equals("") && !befGrade.equals(student._grade)) {
                printOut(gradeList, befGrade, majorCd);
                gradeList = new ArrayList();
            }
            gradeList.add(student);
            befGrade = student._grade;
            hasData = true;
        }
        if (!befGrade.equals("")) {
            printOut(gradeList, befGrade, majorCd);
        }
        return hasData;
    }

    private void printOut(final List printStudents, final String grade, final String majorCd) {
        PrintData daigakuHope = new PrintData();
        PrintData daigakuAft = new PrintData();
        PrintData tandaiHope = new PrintData();
        PrintData tandaiAft = new PrintData();
        PrintData senmonHope = new PrintData();
        PrintData senmonAft = new PrintData();
        PrintData singakuSonotaHope = new PrintData();
        PrintData singakuSonotaAft = new PrintData();
        PrintData singakuKeiHope = new PrintData();
        PrintData singakuKeiAft = new PrintData();

        PrintData kenNaiHope = new PrintData();
        PrintData kenNaiAft = new PrintData();
        PrintData kenNaiKoumuHope = new PrintData();
        PrintData kenNaiKoumuAft = new PrintData();
        PrintData kenGaiHope = new PrintData();
        PrintData kenGaiAft = new PrintData();
        PrintData kenGaiKoumuHope = new PrintData();
        PrintData kenGaiKoumuAft = new PrintData();
        PrintData shushokuSonotaHope = new PrintData();
        PrintData shushokuSonotaAft = new PrintData();
        PrintData shushokuKeiHope = new PrintData();
        PrintData shushokuKeiAft = new PrintData();

        PrintData sonotaHope = new PrintData();
        PrintData sonotaAft = new PrintData();
        PrintData souGouHope = new PrintData();
        PrintData souGouAft = new PrintData();
        for (final Iterator itPrint = printStudents.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            souGouHope = setCnt(souGouHope, student);
            // 進学希望
            if (student._isSingakuHope || (student._isSingaku && !student._isShushoku)) {
                singakuKeiHope = setCnt(singakuKeiHope, student);
                if (student._isSingaku) {
                    souGouAft = setCnt(souGouAft, student);
                    singakuKeiAft = setCnt(singakuKeiAft, student);
                    if (student._aftGradCourseDatSin._schoolGroup.equals(KOKURITU_DAI) ||
                        student._aftGradCourseDatSin._schoolGroup.equals(KOURITU_DAI) ||
                        student._aftGradCourseDatSin._schoolGroup.equals(SIRITU_DAI)
                    ) {
                        daigakuAft = setCnt(daigakuAft, student);
                    } else if (student._aftGradCourseDatSin._schoolGroup.equals(KOURITU_TAN) ||
                            student._aftGradCourseDatSin._schoolGroup.equals(SIRITU_TAN)
                        ) {
                        tandaiAft = setCnt(tandaiAft, student);
                    } else if (student._aftGradCourseDatSin._schoolGroup.equals(SENMON) ||
                            student._aftGradCourseDatSin._schoolGroup.equals(KANGO)
                        ) {
                            senmonAft = setCnt(senmonAft, student);
                    } else if (student._aftGradCourseDatSin._schoolGroup.equals(DAIGAKU_KO) ||
                            student._aftGradCourseDatSin._schoolGroup.equals(NOURYOKU_KAIHATU)
                        ) {
                        singakuSonotaAft = setCnt(singakuSonotaAft, student);
                    } else {
                        singakuSonotaAft = setCnt(singakuSonotaAft, student);
                    }
                }
                if (null == student._courseHopeDat) {
                    singakuSonotaHope = setCnt(singakuSonotaHope, student);
                } else if (student._courseHopeDat._schoolGroup1.equals(KOKURITU_DAI) ||
                        student._courseHopeDat._schoolGroup1.equals(KOURITU_DAI) ||
                        student._courseHopeDat._schoolGroup1.equals(SIRITU_DAI)
                ) {
                    daigakuHope = setCnt(daigakuHope, student);
                } else if (student._courseHopeDat._schoolGroup1.equals(KOURITU_TAN) ||
                        student._courseHopeDat._schoolGroup1.equals(SIRITU_TAN)
                    ) {
                    tandaiHope = setCnt(tandaiHope, student);
                } else if (student._courseHopeDat._schoolGroup1.equals(SENMON) ||
                        student._courseHopeDat._schoolGroup1.equals(KANGO)
                    ) {
                        senmonHope = setCnt(senmonHope, student);
                } else if (student._courseHopeDat._schoolGroup1.equals(DAIGAKU_KO) ||
                        student._courseHopeDat._schoolGroup1.equals(NOURYOKU_KAIHATU)
                    ) {
                    singakuSonotaHope = setCnt(singakuSonotaHope, student);
                } else {
                    singakuSonotaHope = setCnt(singakuSonotaHope, student);
                }

            } else {
                // 就職希望：無 or 未
                if (null == student._courseHopeDat || student._isMiteiHope) {
                    sonotaHope = setCnt(sonotaHope, student);
                } else {
                    shushokuKeiHope = setCnt(shushokuKeiHope, student);
                    // 就職希望：家事手伝い
                    if (student._isKajiHope) {
                        sonotaHope = setCnt(sonotaHope, student);
                    } else {
                        if (student._courseHopeDat._workArea1.equals(KEN_NAI)) {
                            kenNaiHope = setCnt(kenNaiHope, student);
                            // 公務員
                            if (student._courseHopeDat._introductionDiv1.equals(INTRODUCT_P)) {
                                kenNaiKoumuHope = setCnt(kenNaiKoumuHope, student);
                            }
                        } else {
                            kenGaiHope = setCnt(kenGaiHope, student);
                            // 公務員
                            if (student._courseHopeDat._introductionDiv1.equals(INTRODUCT_P)) {
                                kenGaiKoumuHope = setCnt(kenGaiKoumuHope, student);
                            }
                        }
                    }
                }
                // 就職内定(確定者)
                if (student._isShushoku) {
                    souGouAft = setCnt(souGouAft, student);
                    shushokuKeiAft = setCnt(shushokuKeiAft, student);
                    if (student._aftGradCourseDatShu._prefCd.equals(_param._schoolMst._prefCd)) {
                        kenNaiAft = setCnt(kenNaiAft, student);
                        // 公務員
                        if (student._aftGradCourseDatShu._introductionDiv.equals(INTRODUCT_P)) {
                            kenNaiKoumuAft = setCnt(kenNaiKoumuAft, student);
                        }
                    } else {
                        kenGaiAft = setCnt(kenGaiAft, student);
                        // 公務員
                        if (student._aftGradCourseDatShu._introductionDiv.equals(INTRODUCT_P)) {
                            kenGaiKoumuAft = setCnt(kenGaiKoumuAft, student);
                        }
                    }
                }
            }
        }
        _svf.VrSetForm("KNJE380_6.frm", 1);
        _svf.VrsOut("NENDO", _param.changePrintYear(_param._ctrlYear) + "度");
        _svf.VrsOut("REGD", "第" + Integer.parseInt(grade) + "学年");
        _svf.VrsOut("DATE", _param.changePrintDate(_param._ctrlDate));
        _svf.VrsOut("SCHOOL_NAME", _param._schoolMst._schoolName1);
        _svf.VrsOut("MAJORNAME", _param.getMajorName(majorCd));

        int fieldCnt = 1;
        int percent = 0;
        printOutHope(fieldCnt, daigakuHope);
        printOutPercent(fieldCnt, daigakuHope, souGouHope);
        printOutAft(fieldCnt++, daigakuAft);

        printOutHope(fieldCnt, tandaiHope);
        printOutPercent(fieldCnt, tandaiHope, souGouHope);
        printOutAft(fieldCnt++, tandaiAft);

        printOutHope(fieldCnt, senmonHope);
        printOutPercent(fieldCnt, senmonHope, souGouHope);
        printOutAft(fieldCnt++, senmonAft);

        printOutHope(fieldCnt, singakuSonotaHope);
        printOutPercent(fieldCnt, singakuSonotaHope, souGouHope);
        printOutAft(fieldCnt++, singakuSonotaAft);

        printOutHope(fieldCnt, singakuKeiHope);
        percent += printOutPercent(fieldCnt, singakuKeiHope, souGouHope);
        printOutAft(fieldCnt++, singakuKeiAft);

        printOutHope(fieldCnt, kenNaiHope);
        printOutPercent(fieldCnt, kenNaiHope, souGouHope);
        printOutAft(fieldCnt++, kenNaiAft);

        printOutHope(fieldCnt, kenNaiKoumuHope);
        printOutPercent(fieldCnt, kenNaiKoumuHope, souGouHope);
        printOutAft(fieldCnt++, kenNaiKoumuAft);

        printOutHope(fieldCnt, kenGaiHope);
        printOutPercent(fieldCnt, kenGaiHope, souGouHope);
        printOutAft(fieldCnt++, kenGaiAft);

        printOutHope(fieldCnt, kenGaiKoumuHope);
        printOutPercent(fieldCnt, kenGaiKoumuHope, souGouHope);
        printOutAft(fieldCnt++, kenGaiKoumuAft);

        printOutHope(fieldCnt, shushokuSonotaHope);
        printOutPercent(fieldCnt, shushokuSonotaHope, souGouHope);
        printOutAft(fieldCnt++, shushokuSonotaAft);

        printOutHope(fieldCnt, shushokuKeiHope);
        percent += printOutPercent(fieldCnt, shushokuKeiHope, souGouHope);
        printOutAft(fieldCnt++, shushokuKeiAft);

        printOutHope(fieldCnt, sonotaHope);
        _svf.VrsOutn("PASS_PERCENT", fieldCnt, String.valueOf(100 - percent));
        printOutAft(fieldCnt++, sonotaAft);

        printOutHope(fieldCnt, souGouHope);
        printOutPercent(fieldCnt, souGouHope, souGouHope);
        printOutAft(fieldCnt++, souGouAft);

        _svf.VrEndPage();
    }

    private void printOutHope(final int fieldCnt, final PrintData data) {
        _svf.VrsOutn("MALE", fieldCnt, String.valueOf(data._manCnt));
        _svf.VrsOutn("FEMALE", fieldCnt, String.valueOf(data._woManCnt));
        _svf.VrsOutn("TOTAL", fieldCnt, String.valueOf(data._totalCnt));
    }

    private int printOutPercent(int fieldCnt, final PrintData data, final PrintData souGouHope) {
        final BigDecimal bigVal = new BigDecimal(data._totalCnt * 100).divide(new BigDecimal(souGouHope._totalCnt), 0, BigDecimal.ROUND_HALF_UP);
        _svf.VrsOutn("PASS_PERCENT", fieldCnt, bigVal.toString());
        return bigVal.intValue();
    }

    private void printOutAft(final int fieldCnt, final PrintData data) {
        _svf.VrsOutn("PASS_MALE", fieldCnt, String.valueOf(data._manCnt));
        _svf.VrsOutn("PASS_FEMALE", fieldCnt, String.valueOf(data._woManCnt));
        _svf.VrsOutn("PASS_TOTAL", fieldCnt, String.valueOf(data._totalCnt));
    }
}

// eof
