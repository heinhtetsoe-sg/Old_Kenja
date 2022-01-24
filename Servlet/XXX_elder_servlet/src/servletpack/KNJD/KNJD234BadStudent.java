// kanji=漢字
/*
 * $Id: ce621a09d6d719ae637ce2ac18b3323691ef21a1 $
 *
 * 作成日: 2008/06/25 17:21:30 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.KNJD234.Param;
import servletpack.KNJD.KNJD234.Record;
import servletpack.KNJD.KNJD234.SubClass;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: ce621a09d6d719ae637ce2ac18b3323691ef21a1 $
 */
public class KNJD234BadStudent extends KNJD234DetailAbstract {

    static final Log log = LogFactory.getLog(KNJD234BadStudent.class);

    protected KNJD234BadStudent(final DB2UDB db2, final Vrw32alp svf, final Param param) throws IOException {
        super(db2, svf, param);
    }

    protected boolean printOut() {
        _svf.VrSetForm("KNJD234_4.frm", 1);
        boolean hasData = false;
        final Integer gradCompCredits = _param.getGradCompCredits();
        final Integer gradCredits = _param.getGradCredits();

        //ページ単位の指定人数ずつ
        for (final Iterator itPage = _pageList.iterator(); itPage.hasNext();) {
            boolean thisHasData = false;
            final List subClassSort = new ArrayList(getSortSubclass());
            Collections.sort(subClassSort);

            int subCnt = 1;
            final List studentList = (List) itPage.next();

            //対象科目
            for (final Iterator itSubclass = subClassSort.iterator(); itSubclass.hasNext();) {
                int cnt = 1;
                if (subCnt > _maxRetu) {
                    printHead();
                    _svf.VrEndPage();
                    subCnt = 1;
                }
                final SubClass subClass = (SubClass) itSubclass.next();
                final Map subClassInfo = subClass.getSubclassInfo();
                final String subClassCd = (String) subClassInfo.get("CODE");
                final String subClassName = (String) subClassInfo.get("NAME");
                final String credit = _param.getPrintCredit(subClass);
                String absenceHigh = _param.getPrintAbsenceHigh(subClass).equals("null") ? "" : _param.getPrintAbsenceHigh(subClass);
                final String subField = subClassName.length() > 7 ? "2" : "1";

                _svf.VrsOut("SUBCLASS" + subCnt + "_" + subField, subClassName);
                _svf.VrsOut("MAX_CREDIT" + subCnt, absenceHigh);
                _svf.VrsOut("CREDIT" + subCnt, credit);
                for (final Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
                    final PrintDataStudent printDataStudent = (PrintDataStudent) itStudent.next();

                    printCredit("COMP_CREDIT", printDataStudent._totalCompCredit, gradCompCredits.intValue(), cnt);
                    printCredit("GET_CREDIT", printDataStudent._totalCredit, gradCredits.intValue(), cnt);

                    _svf.VrsOutn("HR_CLASS", cnt, printDataStudent._room);
                    _svf.VrsOutn("ATTENDNO", cnt, printDataStudent._attendNo);
                    final String nameField = printDataStudent._name.length() > 10 ? "2" : "1";
                    _svf.VrsOutn("NAME" + nameField, cnt, printDataStudent._name);
                    _svf.VrsOutn("AVERAGE", cnt, printDataStudent._creditAvg);
                    _svf.VrsOutn("REMARK", cnt, printDataStudent._entName);
                    _svf.VrsOutn("SEQNO", cnt, String.valueOf(printDataStudent._renBan));

                    if (printDataStudent._records.containsKey(subClassCd)) {
                        final Record rec = (Record) printDataStudent._records.get(subClassCd);
                        printScore(subCnt, cnt, rec);
                    }

                    if (printDataStudent._attendInfo.containsKey(subClassCd)) {
                        final int absence = ((Integer) printDataStudent._attendInfo.get(subClassCd)).intValue();
                        absenceHigh = _param.getPrintAbsenceHigh(subClass, printDataStudent._schregno, absenceHigh);
                        printAbsence(subCnt, cnt, absenceHigh, String.valueOf(absence));
                    }
                    cnt++;
                    hasData = true;
                    thisHasData = true;
                }
                subCnt++;
            }
            if (thisHasData) {
                printHead();
                _svf.VrEndPage();
            }
        }
        return hasData;
    }

    private void printCredit(
            final String field,
            final int credit,
            final int maxCredit,
            final int cnt
    ) {
        if (_param.isLastAnnual() && _param.getIsGakunenMatu() && credit < maxCredit) {
            _svf.VrAttributen(field, cnt, "Paint=(1,70,1),Bold=1");
        }
        _svf.VrsOutn(field, cnt, String.valueOf(credit));
    }

    private void printScore(final int subCnt, final int cnt, final Record rec) {
        final String gradValue = String.valueOf(rec.getGradValue());
        if (null != gradValue && !gradValue.equals("null")) {
            _svf.VrsOutn("SCORE" + subCnt, cnt, gradValue);
        } else {
            _svf.VrAttributen("SCORE" + subCnt, cnt, "Paint=(1,70,1),Bold=1");
            _svf.VrsOutn("SCORE" + subCnt, cnt, "0");
        }
    }

    private void printAbsence(final int subCnt, final int cnt, final String absenceHigh, final String absence) {
        final int high = absenceHigh.equals("") ? 0 : new java.math.BigDecimal(absenceHigh).intValue();
        final int abse = Integer.parseInt(absence);
        if (abse > high) {
            _svf.VrAttributen("ABSENCE" + subCnt, cnt, "Paint=(1,70,1),Bold=1");
        }
        _svf.VrsOutn("ABSENCE" + subCnt, cnt, absence);
    }

    private void printHead() {

        final int year = Integer.parseInt(_param.getYear());
        final String gengou = KenjaProperties.gengou(year);
        _svf.VrsOut("NENDO", gengou + "年度");
        _svf.VrsOut("SEMESTER", _param.getSemesterName());

        final int grade = Integer.parseInt(_param.getGrade());
        _svf.VrsOut("GRADE", String.valueOf(grade));
        _svf.VrsOut("DATE", _param.getJapaneseDate());

        final String lower = (null == _param.getLowerLine()) ? "??" : _param.getLowerLine().toString();

        final String cond0 = "評定平均 " + lower + "以下";
        final String cond1 = "又は 評定" + _param.getLowerValue() + "が" + _param.getLowerCount() + "科目以上";
        final String badCondition = " 又は 未履修科目が" + _param.getLowerUnStudyCount() + "科目以上";
        _svf.VrsOut("CONDITION1", cond0);
        _svf.VrsOut("CONDITION2", cond1 + badCondition);

        if (_param.isLastAnnual()) {
            _svf.VrsOut("TOTAL_MAX_CREDIT", _param.getGradCompCredits().toString());
            _svf.VrsOut("TOTAL_CREDIT", _param.getGradCredits().toString());
        }
    }

}
 // KNJD234BadStudent

// eof
