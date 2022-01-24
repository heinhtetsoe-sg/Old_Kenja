// kanji=漢字
/*
 * $Id: 39b341210398999a2b31eddab79ac90f37ba670d $
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
 * @version $Id: 39b341210398999a2b31eddab79ac90f37ba670d $
 */
public class KNJF030C_GankaKensin extends KNJF030CAbstract {

    public KNJF030C_GankaKensin(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJF030C_GankaKensin.class");

    /**
     * {@inheritDoc}
     */
    protected boolean printMain(final List printStudents) throws SQLException {
        boolean hasData = false;
        _svf.VrSetForm(F_GANKA_KENSIN, 1);
        for (final Iterator itStudent = printStudents.iterator(); itStudent.hasNext();) {
            final Student student = (Student) itStudent.next();
            if (!isPrint(student)) {
                continue;
            }
            printTitle(GANKA_KENSIN_DOC);
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
            if (null != student._medexamDetDat) {
                _svf.VrsOut("R_BAREVISION_MARK", student._medexamDetDat._rBarevisionMark);
                _svf.VrsOut("R_VISION_MARK", student._medexamDetDat._rVisionMark);
                _svf.VrsOut("L_BAREVISION_MARK", student._medexamDetDat._lBarevisionMark);
                _svf.VrsOut("L_VISION_MARK", student._medexamDetDat._lVisionMark);
                if ("2".equals(_param._printKenkouSindanIppan)) {
                    if ("99".equals(student._medexamDetDat._eyediseasecd) &&
                        (null != student._medexamDetDat._eyeTestResult || null != student._medexamDetDat._eyeTestResult2 || null != student._medexamDetDat._eyeTestResult3)
                    ) {
                        _svf.VrsOut("EYE_TEST_RESULT1"     ,  student._medexamDetDat._eyeTestResult);
                        _svf.VrsOut("EYE_TEST_RESULT2"     ,  student._medexamDetDat._eyeTestResult2);
                        _svf.VrsOut("EYE_TEST_RESULT3"     ,  student._medexamDetDat._eyeTestResult3);
                    } else if (null != student._medexamDetDat._eyediseasecd && !"01".equals(student._medexamDetDat._eyediseasecd)) {
                        // 目の疾病及び異常が null ではなく '01'以外か
                        final String eyeTestResult = null == student._medexamDetDat._eyeTestResult ? "" : "(" + student._medexamDetDat._eyeTestResult + ")";
                        setName("EYEDISEASE", "F050", student._medexamDetDat._eyediseasecd, "NAME1", 40, eyeTestResult, "2");
                    }
                } else if (null != student._medexamDetDat._eyediseasecd && !"01".equals(student._medexamDetDat._eyediseasecd)) {
                    // 目の疾病及び異常が null ではなく '01'以外か
                    final String eyeTestResult = null == student._medexamDetDat._eyeTestResult ? "" : "(" + student._medexamDetDat._eyeTestResult + ")";
                    setName("EYE_TEST_RESULT", "F050", student._medexamDetDat._eyediseasecd, "NAME1", 40, eyeTestResult, "2");
                }
                // 矯正視力がC、Dのどちらかか
                if ("C".equals(student._medexamDetDat._rVisionMark) || "D".equals(student._medexamDetDat._rVisionMark)
                 || "C".equals(student._medexamDetDat._lVisionMark) || "D".equals(student._medexamDetDat._lVisionMark)
                ) {
                    _svf.VrsOut("EYE_NOTICE", "※生活視力が0.7未満でした。");
                // 裸眼視力がC、Dのどちらかか
                } else if ((null == student._medexamDetDat._rVisionMark && ("C".equals(student._medexamDetDat._rBarevisionMark) || "D".equals(student._medexamDetDat._rBarevisionMark)))
                        || (null == student._medexamDetDat._lVisionMark && ("C".equals(student._medexamDetDat._lBarevisionMark) || "D".equals(student._medexamDetDat._lBarevisionMark)))
                ) {
                    _svf.VrsOut("EYE_NOTICE", "※生活視力が0.7未満でした。");
                }
            }
            _svf.VrEndPage();
            hasData = true;
        }
        return hasData;
    }
    
    /**
     * 表示対象の生徒か
     * @param student 生徒
     * @return 表示対象ならtrue
     */
    private boolean isPrint(final Student student) {
        if (null == student._medexamDetDat) {
            return false;
        } else {
            // 矯正視力がC、Dのどちらかか
            if ("C".equals(student._medexamDetDat._rVisionMark) || "D".equals(student._medexamDetDat._rVisionMark)
             || "C".equals(student._medexamDetDat._lVisionMark) || "D".equals(student._medexamDetDat._lVisionMark)) {
                return true;
            }
            // 矯正視力の入力が無く裸眼視力がC、Dのどちらかか
            if (null == student._medexamDetDat._rVisionMark && null == student._medexamDetDat._lVisionMark
             && ("C".equals(student._medexamDetDat._rBarevisionMark) || "D".equals(student._medexamDetDat._rBarevisionMark)
              || "C".equals(student._medexamDetDat._lBarevisionMark) || "D".equals(student._medexamDetDat._lBarevisionMark))) {
                return true;
            }
            // 目の疾病及び異常が null ではなく '01'以外か
            if (null != student._medexamDetDat._eyediseasecd && !"01".equals(student._medexamDetDat._eyediseasecd)) {
                return true;
            }
        }
        return false;
    }
}

// eof
