// kanji=漢字
/*
 * $Id: d281ed9fdbce9f54a82952ce0af5ef0c2b3ae8a2 $
 *
 * 作成日: 2008/06/29 11:08:29 - JST
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

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: d281ed9fdbce9f54a82952ce0af5ef0c2b3ae8a2 $
 */
public class KNJD234GoodStudent extends KNJD234DetailAbstract {

    static final Log log = LogFactory.getLog(KNJD234BadStudent.class);

    protected KNJD234GoodStudent(final DB2UDB db2, final Vrw32alp svf, final Param param) throws IOException {
        super(db2, svf, param);
    }

    protected boolean printOut() {
        _svf.VrSetForm("KNJD234_5.frm", 1);

        int lineCnt = 1;
        for (final Iterator itPage = _pageList.iterator(); itPage.hasNext();) {

            final List studentList = (List) itPage.next();

            for (final Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {

                if (lineCnt > _maxLine) {
                    printHead();
                    _svf.VrEndPage();
                    lineCnt = 1;
                }

                final PrintDataStudent printDataStudent = (PrintDataStudent) itStudent.next();

                _svf.VrsOutn("HR_NAME", lineCnt, printDataStudent._hrClass);
                _svf.VrsOutn("ATTENDNO", lineCnt, printDataStudent._attendNo);
                final String nameField = printDataStudent._name.length() > 10 ? "2" : "1";
                _svf.VrsOutn("NAME" + nameField, lineCnt, printDataStudent._name);

                _svf.VrsOutn("COMP_CREDIT", lineCnt, String.valueOf(printDataStudent._totalCompCredit));
                _svf.VrsOutn("GET_CREDIT", lineCnt, String.valueOf(printDataStudent._totalCredit));
                _svf.VrsOutn("AVERAGE", lineCnt, printDataStudent._creditAvg);

                final List recordsSort = new ArrayList(printDataStudent._records.values());
                Collections.sort(recordsSort);

                int studentCnt = 1;
                for (final Iterator itRecords = recordsSort.iterator(); itRecords.hasNext();) {

                    if (studentCnt > _maxRetu) {
                        lineCnt++;
                        studentCnt = 1;
                    }

                    if (lineCnt > _maxLine) {
                        printHead();
                        _svf.VrEndPage();
                        lineCnt = 1;
                        studentCnt = 1;
                    }

                    final Record rec = (Record) itRecords.next();

                    final SubClass subClass = rec.getSubclass();
                    final Map subClassInfo = subClass.getSubclassInfo();
                    final String subClassName = (String) subClassInfo.get("NAME");
                    final String subField = subClassName.length() > 5 ? "2" : "1";
                    _svf.VrsOutn("SUBCLASS" + studentCnt + "_" + subField, lineCnt, subClassName);

                    final String gradValue = String.valueOf(rec.getGradValue());
                    if (null != gradValue && !gradValue.equals("null")) {
                        _svf.VrsOutn("SCORE" + studentCnt, lineCnt, gradValue);
                    }
                    studentCnt++;
                }
                lineCnt++;
            }
        }
        final boolean hasData = lineCnt > 1;
        if (hasData) {
            printHead();
            _svf.VrEndPage();
        }
        return hasData;
    }

    private void printHead() {

        final int year = Integer.parseInt(_param.getYear());
        final String gengou = KenjaProperties.gengou(year);
        _svf.VrsOut("NENDO", gengou + "年度");
        _svf.VrsOut("SEMESTER", _param.getSemesterName());

        _svf.VrsOut("DATE", _param.getJapaneseDate());

        _svf.VrsOut("CONDITION", _param.getUpperLine().toString());
    }


}
 // KNJD234GoodStudent

// eof
