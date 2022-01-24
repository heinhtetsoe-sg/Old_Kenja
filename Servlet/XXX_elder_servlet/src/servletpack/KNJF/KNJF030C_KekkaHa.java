// kanji=漢字
/*
 * $Id: 4d726378e40ff84ed05351eb7c00a2ade8ceafcd $
 *
 * 作成日: 2009/10/07 15:55:24 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.util.Iterator;
import java.util.List;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJF.KNJF030C.Param;
import servletpack.KNJF.KNJF030C.Student;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 4d726378e40ff84ed05351eb7c00a2ade8ceafcd $
 */
public class KNJF030C_KekkaHa extends KNJF030CAbstract {

    public KNJF030C_KekkaHa(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJF030C_KekkaHa.class");

    /**
     * {@inheritDoc}
     */
    protected boolean printMain(final List printStudents) throws SQLException {
        boolean hasData = false;
        _svf.VrSetForm(_param._printKekkaHaCard ? F_KEKKA_HA_A : F_KEKKA_HA, 1);
        for (final Iterator itStudent = printStudents.iterator(); itStudent.hasNext();) {
            printTitle(_param._printKekkaHaCard ? KEKKA_HA_A_DOC : KEKKA_HA_DOC);
            final Student student = (Student) itStudent.next();
            _svf.VrsOut("DATE", _param.changePrintDate(_param._ctrlDate));
            _svf.VrsOut("SCHOOLNAME1", _param.schoolInfoVal(student._schoolKind, Param.SCHOOL_NAME1));
            _svf.VrsOut("PRINCIPAL_NAME", _param.schoolInfoVal(student._schoolKind, Param.PRINCIPAL_NAME));
            _svf.VrsOut("PRINCIPAL_JOBNAME", _param.schoolInfoVal(student._schoolKind, Param.PRINCIPAL_JOBNAME));
            _svf.VrsOut("HR_NAME", student._hrName);
            _svf.VrsOut("ATTENDNO", String.valueOf(Integer.parseInt(student._attendno)) + "番");
            final String setMajor = student._majorname;
            final int majorField = setMajor.length() > 10 ? 2 : 1;
            _svf.VrsOut("MAJOR" + majorField, setMajor);
            final String fieldNo = (17 < student._name.length()) ? "_3" : (10 < student._name.length()) ? "_2" : "";
            _svf.VrsOut("NAME" + fieldNo, student._name);
            if (null != student._medexamToothDat) {
                final int reBaby = null != student._medexamToothDat._remainbabytooth ? Integer.parseInt(student._medexamToothDat._remainbabytooth) : 0;
                final int reAdult = null != student._medexamToothDat._remainadulttooth ? Integer.parseInt(student._medexamToothDat._remainadulttooth) : 0;
                final boolean reAdultBabyNull = null == student._medexamToothDat._remainadulttooth && null == student._medexamToothDat._remainbabytooth ? true : false;
                _svf.VrsOut("REMAIN_TOOTH", reAdultBabyNull ? "" : reBaby + reAdult > 0 ? "あり" : "なし");
                final int brackB = null != student._medexamToothDat._brackBabytooth ? Integer.parseInt(student._medexamToothDat._brackBabytooth) : 0;
                final boolean brackBNull = null == student._medexamToothDat._brackBabytooth ? true : false;
                _svf.VrsOut("BRACK_BABYTOOTH", brackBNull ? "" : brackB > 0 ? "あり" : "なし");
                final int brackA = null != student._medexamToothDat._brackAdulttooth ? Integer.parseInt(student._medexamToothDat._brackAdulttooth) : 0;
                final boolean brackANull = null == student._medexamToothDat._brackAdulttooth ? true : false;
                _svf.VrsOut("BRACK_ADULTTOOTH", brackANull ? "" : brackA > 0 ? "あり" : "なし");
                final int lostA = null != student._medexamToothDat._lostadulttooth ? Integer.parseInt(student._medexamToothDat._lostadulttooth) : 0;
                final boolean lostANull = null == student._medexamToothDat._lostadulttooth ? true : false;
                _svf.VrsOut("LOSTADULTTOOTH", lostANull ? "" : lostA > 0 ? "あり" : "なし");

                setName("JAWS_JOINT", "F510", student._medexamToothDat._jawsJointcd, "NAME1", 0, "", "");
                setName("JAWS_JOINT2", "F511", student._medexamToothDat._jawsJointcd2, "NAME1", 0, "", "");
                setName("PLAQUE", "F520", student._medexamToothDat._plaquecd, "NAME1", 0, "", "");
                setName("GUM", "F513", student._medexamToothDat._gumcd, "NAME1", 0, "", "");
            }
            _svf.VrEndPage();
            hasData = true;
        }
        return hasData;
    }
}

// eof
