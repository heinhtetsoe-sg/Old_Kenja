// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2008/05/12 15:19:55 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.KNJD177.Param;
import servletpack.KNJD.KNJD177.Student;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id$
 */
public abstract class KNJD177FormAbstract {
    private static final Log log = LogFactory.getLog(KNJD177FormAbstract.class);

    protected final Vrw32alp _svf;
    protected final Param _param;

    public KNJD177FormAbstract(final HttpServletResponse response, final Param param) throws Exception {
        _param = param;
        _svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        init(response);

        log.debug(_param._inState);
    }

    private void init(
            final HttpServletResponse response
    ) throws IOException {
        response.setContentType("application/pdf");

        _svf.VrInit();
        _svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    public void noneData() {
        _svf.VrSetForm("MES001.frm", 0);
        _svf.VrsOut("note", "note");
        _svf.VrEndPage();
    }

    protected void setHead(final String formName) {
        _svf.VrSetForm(formName, 4);
        final String nendo = _param.changePrintYear();
        _svf.VrsOut("NENDO", nendo);
        final String semeName = _param.isGakunenMatu() ? (String) _param._semesterMap.get(_param._ctrlSeme) : (String) _param._semesterMap.get(_param._semester);
        _svf.VrsOut("SEMESTER",  semeName);
        if (null != _param._certifSchool) {
            _svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);
            _svf.VrsOut("JOB_NAME", _param._certifSchool._jobName);
            _svf.VrsOut("PRINCIPAL_NAME", _param._certifSchool._principalName);
        }
    }

    abstract protected void printScore(final Map subclassMap, final List testList, final Map scoreMap, final DB2UDB db2);

    protected void printStudent(final Student student) {
        log.debug(student);
        if (null != _param._certifSchool && null != _param._certifSchool._remark2) {
            _svf.VrsOut("STAFFNAME", _param._certifSchool._remark2 + student._hrStaffName);
        }
        _svf.VrsOut("HR_NAME", student._hrName + String.valueOf(Integer.parseInt(student._attendNo)) + "号");
        _svf.VrsOut("COURSE", student._courseName + student._majorName);
        _svf.VrsOut("NAME", student._name);
        if (null != _param._addrPrint) {
            _svf.VrsOut("ZIPCD", student._gZip);
            _svf.VrsOut("ADDR1", student._gAddr1);
            _svf.VrsOut("ADDR2", student._gAddr2);
            if (!StringUtils.isBlank(student._gName)) {
                _svf.VrsOut("ADDRESSEE", student._gName + "  様");
            }
        }
    }

    protected void printAttend(
            final int line,
            final String semesterName,
            final String lesson,
            final String mourning,
            final String present,
            final String attend,
            final String absence,
            final String late,
            final String early
    ) {
        _svf.VrsOutn("SEMESTERNAME", line, semesterName);
        _svf.VrsOutn("LESSON", line, lesson);
        _svf.VrsOutn("MOURNING", line, mourning);
        _svf.VrsOutn("PRESENT", line, present);
        _svf.VrsOutn("ATTEND", line, attend);
        _svf.VrsOutn("ABSENCE", line, absence);
        _svf.VrsOutn("LATE", line, late);
        _svf.VrsOutn("EARLY", line, early);
    }

    protected void printHreport(
            final String fieldName,
            final String str,
            final int size,
            final int lineCnt
    ) {
        List<String> arrlist = KNJ_EditKinsoku.getTokenList(str, size, lineCnt);
        log.info(" " + fieldName + ", " + arrlist);
        for (int i = 0; i < arrlist.size(); i++) {
            _svf.VrsOutn(fieldName, i + 1,  arrlist.get(i));
        }
    }

    /**
     * @param db2
     * @param student
     * @param subject_d
     * @param subject_t
     */
    public void printGetCredit(
            final String field,
            final String credit
    ) throws SQLException {
        _svf.VrsOut(field, credit);
    }
}
 // KNJD177FormAbstract

// eof
