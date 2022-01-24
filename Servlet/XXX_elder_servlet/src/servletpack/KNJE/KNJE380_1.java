// kanji=漢字
/*
 * $Id: dff8ff49f2585fac8579cdea0c42e42a53539d9d $
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
 * @version $Id: dff8ff49f2585fac8579cdea0c42e42a53539d9d $
 */
public class KNJE380_1 extends KNJE380Abstract {

    /**
     * コンストラクタ。
     * @param param
     * @param db2
     * @param svf
     */
    public KNJE380_1(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJE380_1.class");

    protected boolean printMain(final List printStudents, final String majorCd) throws SQLException {
        boolean hasData = false;
        PrintData gradPlan = new PrintData();
        PrintData eduPlan = new PrintData();
        PrintData hopeSt = new PrintData();
        PrintData hopeSi = new PrintData();
        PrintData hopeSo = new PrintData();
        PrintData hopeMt = new PrintData();
        PrintData hopeMi = new PrintData();
        PrintData hopeMo = new PrintData();
        PrintData hopePt = new PrintData();
        PrintData hopePi = new PrintData();
        PrintData hopePo = new PrintData();
        PrintData hopeTt = new PrintData();
        PrintData hopeTi = new PrintData();
        PrintData hopeTo = new PrintData();
        PrintData otherPlanY = new PrintData();
        PrintData otherPlanN = new PrintData();
        PrintData aftSt = new PrintData();
        PrintData aftSi = new PrintData();
        PrintData aftSo = new PrintData();
        PrintData aftMt = new PrintData();
        PrintData aftMi = new PrintData();
        PrintData aftMo = new PrintData();
        PrintData aftPt = new PrintData();
        PrintData aftPi = new PrintData();
        PrintData aftPo = new PrintData();
        PrintData aftTt = new PrintData();
        PrintData aftTi = new PrintData();
        PrintData aftTo = new PrintData();
        PrintData aftPlan = new PrintData();
        PrintData noAftTt = new PrintData();
        PrintData noAftTi = new PrintData();
        PrintData noAftTo = new PrintData();
        PrintData noHope = new PrintData();
        for (final Iterator itprint = printStudents.iterator(); itprint.hasNext();) {
            final Student student = (Student) itprint.next();
            gradPlan = setCnt(gradPlan, student);
            // 進学希望
            if (student._isSingakuHope || (student._isSingaku && !student._isShushoku)) {
                eduPlan = setCnt(eduPlan, student);
            } else {
                // 就職希望：無 or 未
                if (null == student._courseHopeDat || student._isMiteiHope) {
                    otherPlanN = setCnt(otherPlanN, student);
                    if (!student._isShushoku) {
                        noHope = setCnt(noHope, student);
                    }
                } else {
                    // 就職希望：家事手伝い
                    if (student._isKajiHope) {
                        otherPlanY = setCnt(otherPlanY, student);
                        if (!student._isShushoku) {
                            noHope = setCnt(noHope, student);
                        }
                    } else {
                        // 就職希望合計
                        hopeTt = setCnt(hopeTt, student);
                        if (student._courseHopeDat._workArea1.equals(KEN_NAI)) {
                            hopeTi = setCnt(hopeTi, student);
                        } else {
                            hopeTo = setCnt(hopeTo, student);
                        }
                        // 就職希望紹介：学校
                        if (student._courseHopeDat._introductionDiv1.equals(INTRODUCT_S)) {
                            hopeSt = setCnt(hopeSt, student);
                            if (student._courseHopeDat._workArea1.equals(KEN_NAI)) {
                                hopeSi = setCnt(hopeSi, student);
                            } else {
                                hopeSo = setCnt(hopeSo, student);
                            }
                        }
                        // 就職希望紹介：自己・縁故
                        if (student._courseHopeDat._introductionDiv1.equals(INTRODUCT_M)) {
                            hopeMt = setCnt(hopeMt, student);
                            if (student._courseHopeDat._workArea1.equals(KEN_NAI)) {
                                hopeMi = setCnt(hopeMi, student);
                            } else {
                                hopeMo = setCnt(hopeMo, student);
                            }
                        }
                        // 就職希望紹介：公務員
                        if (student._courseHopeDat._introductionDiv1.equals(INTRODUCT_P)) {
                            hopePt = setCnt(hopePt, student);
                            if (student._courseHopeDat._workArea1.equals(KEN_NAI)) {
                                hopePi = setCnt(hopePi, student);
                            } else {
                                hopePo = setCnt(hopePo, student);
                            }
                        }
                    }
                }
                // 就職内定(確定者)
                if (student._isShushoku) {
                    if (student._isSingaku) {
                        aftPlan = setCnt(aftPlan, student);
                    } else {
                        // 就職内定合計
                        aftTt = setCnt(aftTt, student);
                        if (student._aftGradCourseDatShu._prefCd.equals(_param._schoolMst._prefCd)) {
                            aftTi = setCnt(aftTi, student);
                        } else {
                            aftTo = setCnt(aftTo, student);
                        }
                        // 就職内定紹介：学校
                        if (student._aftGradCourseDatShu._introductionDiv.equals(INTRODUCT_S)) {
                            aftSt = setCnt(aftSt, student);
                            if (student._aftGradCourseDatShu._prefCd.equals(_param._schoolMst._prefCd)) {
                                aftSi = setCnt(aftSi, student);
                            } else {
                                aftSo = setCnt(aftSo, student);
                            }
                        }
                        // 就職内定紹介：自己・縁故
                        if (student._aftGradCourseDatShu._introductionDiv.equals(INTRODUCT_M)) {
                            aftMt = setCnt(aftMt, student);
                            if (student._aftGradCourseDatShu._prefCd.equals(_param._schoolMst._prefCd)) {
                                aftMi = setCnt(aftMi, student);
                            } else {
                                aftMo = setCnt(aftMo, student);
                            }
                        }
                        // 就職内定紹介：公務員
                        if (student._aftGradCourseDatShu._introductionDiv.equals(INTRODUCT_P)) {
                            aftPt = setCnt(aftPt, student);
                            if (student._aftGradCourseDatShu._prefCd.equals(_param._schoolMst._prefCd)) {
                                aftPi = setCnt(aftPi, student);
                            } else {
                                aftPo = setCnt(aftPo, student);
                            }
                        }
                    }
                } else {
                    if (null != student._courseHopeDat && !student._isMiteiHope && !student._isKajiHope) {
                        noHope = setCnt(noHope, student);
                    }
                    noAftTt = setCnt(noAftTt, student);
                    if (null != student._courseHopeDat && student._courseHopeDat._workArea1.equals(KEN_NAI)) {
                        noAftTi = setCnt(noAftTi, student);
                    } else {
                        noAftTo = setCnt(noAftTo, student);
                    }
                }
            }
            hasData = true;
        }
        _svf.VrSetForm("KNJE380_1.frm", 1);
        _svf.VrsOut("DATE", _param.changePrintDate(_param._ctrlDate));
        _svf.VrsOut("SCHOOL_NAME", _param._schoolMst._schoolName1);
        _svf.VrsOut("MAJORNAME", _param.getMajorName(majorCd));
        printOut("GRAD_PLAN", gradPlan);
        printOut("EDU_PLAN", eduPlan);
        printOut("HOPE_S_T", hopeSt);
        printOut("HOPE_S_I", hopeSi);
        printOut("HOPE_S_O", hopeSo);
        printOut("HOPE_M_T", hopeMt);
        printOut("HOPE_M_I", hopeMi);
        printOut("HOPE_M_O", hopeMo);
        printOut("HOPE_P_T", hopePt);
        printOut("HOPE_P_I", hopePi);
        printOut("HOPE_P_O", hopePo);
        printOut("HOPE_T_T", hopeTt);
        printOut("HOPE_T_I", hopeTi);
        printOut("HOPE_T_O", hopeTo);
        printOut("OTHER_PLAN_Y", otherPlanY);
        printOut("OTHER_PLAN_N", otherPlanN);
        printOut("AFT_S_T", aftSt);
        printOut("AFT_S_I", aftSi);
        printOut("AFT_S_O", aftSo);
        printOut("AFT_M_T", aftMt);
        printOut("AFT_M_I", aftMi);
        printOut("AFT_M_O", aftMo);
        printOut("AFT_P_T", aftPt);
        printOut("AFT_P_I", aftPi);
        printOut("AFT_P_O", aftPo);
        printOut("AFT_T_T", aftTt);
        printOut("AFT_T_I", aftTi);
        printOut("AFT_T_O", aftTo);
        printOut("AFT_PLAN", aftPlan);
        printOut("NO_AFT_T", noAftTt);
        printOut("NO_AFT_I", noAftTi);
        printOut("NO_AFT_O", noAftTo);
        printOut("NO_HOPE", noHope);
        _svf.VrEndPage();
        return hasData;
    }

    private void printOut(final String fieldName, final PrintData data) {
        _svf.VrsOutn(fieldName, 1, String.valueOf(data._manCnt));
        _svf.VrsOutn(fieldName, 2, String.valueOf(data._woManCnt));
        _svf.VrsOutn(fieldName, 3, String.valueOf(data._totalCnt));
    }
}

// eof
