// kanji=漢字
/*
 * $Id: 4d762312239a3fac2df4304e6b3a8cdef19dc37b $
 *
 * 作成日: 2008/06/19 17:23:59 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.util.Iterator;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJWA.KNJWA230.HrClass;
import servletpack.KNJWA.KNJWA230.Param;
import servletpack.KNJWA.KNJWA230.Student;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 4d762312239a3fac2df4304e6b3a8cdef19dc37b $
 */
public class KNJWA230Katsudo {

    private static final Log log = LogFactory.getLog(KNJWA230Katsudo.class);
    private static final int MAX_HR_CNT = 6;

    Param _param;
    Vrw32alp _svf;
    DB2UDB _db2;

    public boolean printData(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student) throws Exception {
        boolean hasData = false;
        _db2 = db2;
        _param = param;
        _svf = svf;
        if ("1".equals(student._oldCurriculum)) {
            _svf.VrSetForm("KNJWA230_4_2.frm", 1);
        } else {
            _svf.VrSetForm("KNJWA230_4.frm", 1);
        }
        _svf.VrsOut("NAME", student._name);
        printTokubetsu(student);

        hasData = true;

        return hasData;
    }

    private void printSougouGakusyu(final Student student) throws SQLException {
        final String sql = "SELECT "
                         + "    VALUE(TOTALSTUDYACT, '') AS TOTALSTUDYACT, "
                         + "    VALUE(TOTALSTUDYVAL, '') AS TOTALSTUDYVAL "
                         + "FROM "
                         + "    HTRAINREMARK_HDAT "
                         + "WHERE "
                         + "    SCHREGNO = '" + student._schregno + "'";
        ResultSet rs = null;
        try {
            _db2.query(sql);
            rs = _db2.getResultSet();
            while (rs.next()) {
                String[] strArray = null; // フィールドの文字列を配列へ
                strArray = KNJ_EditEdit.get_token(rs.getString("TOTALSTUDYACT"), 120, 4);
                if (strArray != null) {
                    for (int i = 0; i < strArray.length; i++) {
                        _svf.VrsOutn("TOTALSTUDYACT", (i + 1), strArray[i]);
                    }
                }
                strArray = null;
                strArray = KNJ_EditEdit.get_token(rs.getString("TOTALSTUDYVAL"), 120, 5);
                if (strArray != null) {
                    for (int i = 0; i < strArray.length; i++) {
                        _svf.VrsOutn("TOTALSTUDYVAL", (i + 1), strArray[i]);
                    }
                }
            }
            _svf.VrEndPage();
        } finally {
            DbUtils.closeQuietly(rs);
            _db2.commit();
        }
    }

    private void printTokubetsu(final Student student) throws SQLException {
        ResultSet rs = null;
        try {
            int hrCnt = 1;
            int hrSortChange = 0;
            for (final Iterator itRegd = student._regdHist.iterator(); itRegd.hasNext();) {
                if (hrCnt > MAX_HR_CNT) {
                    printSougouGakusyu(student);
                    hrCnt = 1;
                    hrSortChange++;
                }
                final HrClass hrClass = (HrClass) itRegd.next();
                final int sort = Integer.parseInt(hrClass._sort) - (hrSortChange * 6);
                _svf.VrsOut("NENDO1_" + sort, _param.changePrintYear(hrClass._year, false));
                _svf.VrsOut("NENDO2_" + sort, _param.changePrintYear(hrClass._year, false));
                _svf.VrsOut("NENDO3_" + sort, _param.changePrintYear(hrClass._year, false));

                printCertifSchool(hrClass, sort);
                printHtrainRemark(student, hrClass, sort);
                hrCnt++;
            }
            if (hrCnt > 1) {
                printSougouGakusyu(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            _db2.commit();
        }
    }

    private void printCertifSchool(final HrClass hrClass, final int sort) throws SQLException {
        ResultSet rs = null;
        final String sql = ""
            + "SELECT "
            + "    * "
            + "FROM "
            + "    CERTIF_SCHOOL_DAT "
            + "WHERE "
            + "    YEAR = '" + hrClass._year + "' "
            + "    AND CERTIF_KINDCD = '107'";
        try {
            _db2.query(sql);
            rs = _db2.getResultSet();
            while (rs.next()) {
                _svf.VrsOut("ATTENDREC_REMARK" + sort, rs.getString("REMARK7"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            _db2.commit();
        }
    }

    private void printHtrainRemark(final Student student, final HrClass hrClass, final int sort) throws SQLException {
        ResultSet rs = null;
        final String sql = "SELECT "
            + "    VALUE(SPECIALACTREMARK, '') AS SPECIALACTREMARK, "
            + "    VALUE(TOTALREMARK, '') AS TOTALREMARK, "
            + "    VALUE(ATTENDREC_REMARK, '') AS ATTENDREC_REMARK "
            + "FROM "
            + "    HTRAINREMARK_DAT "
            + "WHERE "
            + "    YEAR = '" + hrClass._year + "' "
            + "    AND SCHREGNO = '" + student._schregno + "'";
        try {
            _db2.query(sql);
            rs = _db2.getResultSet();
            while (rs.next()) {
                String[] strArray = null; // フィールドの文字列を配列へ
                strArray = KNJ_EditEdit.get_token(rs.getString("SPECIALACTREMARK"), 20, 6);
                if (strArray != null) {
                    for (int i = 0; i < strArray.length; i++) {
                        _svf.VrsOutn("SPECIALACTREMARK" + sort, (i + 1), strArray[i]);
                    }
                }
                strArray = null;
                strArray = KNJ_EditEdit.get_token(rs.getString("TOTALREMARK"), 112, 6);
                if (strArray != null) {
                    for (int i = 0; i < strArray.length; i++) {
                        _svf.VrsOutn("TOTALREMARK" + sort, (i + 1), strArray[i]);
                    }
                }
                if (null != rs.getString("ATTENDREC_REMARK") && rs.getString("ATTENDREC_REMARK").length() > 0) {
                    _svf.VrsOut("ATTENDREC_REMARK" + sort, rs.getString("ATTENDREC_REMARK"));
                }
            }
        } finally {
            DbUtils.closeQuietly(rs);
            _db2.commit();
        }
    }

}
 // KNJWA230Katsudo

// eof
