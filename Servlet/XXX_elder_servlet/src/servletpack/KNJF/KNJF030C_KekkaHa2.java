// kanji=漢字
/*
 * $Id: a8ad3e029f9c717941d8f2b889ed89261abb073a $
 *
 * 作成日: 2011/05/13 15:55:24 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.util.Iterator;
import java.util.List;

import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJF.KNJF030C.Param;
import servletpack.KNJF.KNJF030C.Student;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: a8ad3e029f9c717941d8f2b889ed89261abb073a $
 */
public class KNJF030C_KekkaHa2 extends KNJF030CAbstract {

    public KNJF030C_KekkaHa2(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJF030C_KekkaHa2.class");

    /**
     * {@inheritDoc}
     */
    protected boolean printMain(final List printStudents) throws SQLException {
        boolean hasData = false;
        _svf.VrSetForm(F_KEKKA_HA2, 1);
        for (final Iterator itStudent = printStudents.iterator(); itStudent.hasNext();) {
            final Student student = (Student) itStudent.next();
            _svf.VrsOut("DATE", _param.changePrintDate(_param._kekkaHa2Date));
            if (_param._isMusashinoHigashi) {
                _svf.VrAttribute("SCHOOLNAME1", "Edit=");
                _svf.VrsOut("SCHOOLNAME1", StringUtils.defaultString(_param.schoolInfoVal(student._schoolKind, Param.SCHOOL_NAME1)) + "  保健室");

            } else {
                _svf.VrsOut("SCHOOLNAME1", _param.schoolInfoVal(student._schoolKind, Param.SCHOOL_NAME1));
            }
            _svf.VrsOut("HR_NAME", student._hrName);
            _svf.VrsOut("ATTENDNO", String.valueOf(Integer.parseInt(student._attendno)) + "番");
            _svf.VrsOut("MAJOR", student._majorname);
            final String fieldNo = (17 < student._name.length()) ? "_3" : (10 < student._name.length()) ? "_2" : "";
            _svf.VrsOut("NAME" + fieldNo, student._name);
            if (null != student._medexamToothDat) {
                // フィールド
                final int brackA = null != student._medexamToothDat._brackAdulttooth ? Integer.parseInt(student._medexamToothDat._brackAdulttooth) : 0;
                final int checkA = null != student._medexamToothDat._checkAdulttooth ? Integer.parseInt(student._medexamToothDat._checkAdulttooth) : 0;
                final int reBaby = null != student._medexamToothDat._remainbabytooth ? Integer.parseInt(student._medexamToothDat._remainbabytooth) : 0;
                final int reAdult = null != student._medexamToothDat._remainadulttooth ? Integer.parseInt(student._medexamToothDat._remainadulttooth) : 0;
                final int brackB = null != student._medexamToothDat._brackBabytooth ? Integer.parseInt(student._medexamToothDat._brackBabytooth) : 0;
                final String gumcd = student._medexamToothDat._gumcd;
                final String plaquecd = student._medexamToothDat._plaquecd;
                final String jawsJointcd = student._medexamToothDat._jawsJointcd;
                final String jawsJointcd2 = student._medexamToothDat._jawsJointcd2;
                final String calculuscd = student._medexamToothDat._calculuscd;
//                final String dentisttreatcd = student._medexamToothDat._dentisttreatcd;
                final String otherdiseasecd = student._medexamToothDat._otherdiseasecd;
                final String otherdisease = student._medexamToothDat._otherdisease;
                // ○表示するか？
                boolean maru2  = brackA > 0 ? true : false;
                boolean maru3  = "02".equals(gumcd) ? true : false;
                boolean maru4  = "02".equals(plaquecd) ? true : false;
                boolean maru5  = "02".equals(jawsJointcd) ? true : false;
                boolean maru6  = "02".equals(jawsJointcd2) ? true : false;
                boolean maru7  = checkA > 0 ? true : false;
                boolean maru8  = reBaby + reAdult > 0 ? true : false;
                boolean maru9  = "03".equals(gumcd) ? true : false;
                boolean maru10 = "01".equals(calculuscd) ? true : false;
                boolean maru11 = "03".equals(jawsJointcd) ? true : false;
                boolean maru12 = "03".equals(jawsJointcd2) ? true : false;
                boolean maru13 = brackB > 0 ? true : false;
                boolean maru14 = "03".equals(plaquecd) ? true : false;
                boolean maru15 = false;
                if ("01".equals(otherdiseasecd) || "02".equals(otherdiseasecd) || "03".equals(otherdiseasecd) ||
                    "04".equals(otherdiseasecd) || "05".equals(otherdiseasecd) || "99".equals(otherdiseasecd)) maru15 = true;
                boolean maru1  = true;
                if (maru2 || maru3 || maru4 || maru5 ||
                    maru6 || maru7 || maru8 || maru9 || maru10 || maru11 || maru12 || maru13 || maru14 || maru15) maru1 = false;

                /*** 異常なし ***/
                _svf.VrsOut("MARU1", maru1 ? "○" : "");
                /*** 経過観察 ***/
                _svf.VrsOut("MARU2", maru2 ? "○" : "");
                _svf.VrsOut("CHILD_LINE1", maru2 ? "＝＝" : "");
                _svf.VrsOut("MARU3", maru3 ? "○" : "");
                _svf.VrsOut("MARU4", maru4 ? "○" : "");
                _svf.VrsOut("MARU5", maru5 ? "○" : "");
                _svf.VrsOut("MARU6", maru6 ? "○" : "");
                /*** 受診 ***/
                _svf.VrsOut("MARU7", maru7 ? "○" : "");
                _svf.VrsOut("CHILD_LINE2", maru7 ? "＝＝" : "");
                _svf.VrsOut("MARU8", maru8 ? "○" : "");
                _svf.VrsOut("CHILD_LINE3", reAdult > 0 ? "＝＝" : "");
                _svf.VrsOut("ADULT_LINE3", reBaby > 0 ? "＝＝＝" : "");
                _svf.VrsOut("MARU9", maru9 ? "○" : "");
                _svf.VrsOut("MARU10", maru10 ? "○" : "");
                _svf.VrsOut("MARU11", maru11 ? "○" : "");
                _svf.VrsOut("MARU12", maru12 ? "○" : "");
                _svf.VrsOut("MARU13", maru13 ? "○" : "");
                _svf.VrsOut("MARU14", maru14 ? "○" : "");
                if ("99".equals(otherdiseasecd)) {
                    _svf.VrsOut("OTHER1", otherdisease);
                } else if (NumberUtils.isNumber(otherdiseasecd) && Integer.parseInt(otherdiseasecd) >= 2) {
                    setName("OTHER1", "F530", otherdiseasecd, "NAME1", 0, "", "");
                }
                if (maru15) {
                    _svf.VrsOut("MARU15", "○");
                } else {
                    _svf.VrsOut("MARU15", "");
                }
            }
            putGengou1(_db2, _svf, "ERA_NAME");
            _svf.VrEndPage();
            hasData = true;
        }
        return hasData;
    }

    private void putGengou1(final DB2UDB db2, final Vrw32alp svf, final String field) {
        //元号(記入項目用)
        String[] dwk;
        if (_param._kekkaHa2Date.indexOf('/') >= 0) {
            dwk = StringUtils.split(_param._kekkaHa2Date, '/');
        } else if (_param._kekkaHa2Date.indexOf('-') >= 0) {
            dwk = StringUtils.split(_param._kekkaHa2Date, '-');
        } else {
            //ありえないので、固定値で設定。
            dwk = new String[1];
        }
        if (dwk.length >= 3) {
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(dwk[0]), Integer.parseInt(dwk[1]), Integer.parseInt(dwk[2]));
            svf.VrsOut(field, gengou);
        }
    }

}

// eof
