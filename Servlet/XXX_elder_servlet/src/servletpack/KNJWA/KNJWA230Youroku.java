// kanji=漢字
/*
 * $Id: fdd1beb1055ea8c3cf8c4892d5d07a804b594da2 $
 *
 * 作成日: 2008/06/19 16:47:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJWA.KNJWA230.CertifSchool;
import servletpack.KNJWA.KNJWA230.HrClass;
import servletpack.KNJWA.KNJWA230.Param;
import servletpack.KNJWA.KNJWA230.Student;
import servletpack.KNJWA.KNJWA230.Transfer;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 生徒指導要録
 * @author m-yama
 * @version $Id: fdd1beb1055ea8c3cf8c4892d5d07a804b594da2 $
 */
public class KNJWA230Youroku {

    private static final Log log = LogFactory.getLog(KNJWA230Youroku.class);
    private static final int MAX_HR_CNT = 6;

    Param _param;
    Vrw32alp _svf;
    DB2UDB _db2;

    public boolean printData(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student) throws Exception {
        boolean hasData = false;
        _db2 = db2;
        _param = param;
        _svf = svf;
        _svf.VrSetForm("KNJWA230_1.frm", 1);

        int hrCnt = 1;
        int hrSortChange = 0;
        final boolean isNavi = isNavi(db2, param._year);
        for (final Iterator itRegd = student._regdHist.iterator(); itRegd.hasNext();) {
            if (hrCnt > MAX_HR_CNT) {
                setBasePrint(student);
                hrCnt = 1;
                hrSortChange++;
            }
            final HrClass hrClass = (HrClass) itRegd.next();

            final String nendo = _param.changePrintYear(hrClass._year, false);
            final int sort = Integer.parseInt(hrClass._sort) - (hrSortChange * 6);
            _svf.VrsOut("NENDO" + sort + "_1", nendo.substring(0, 2));
            _svf.VrsOut("NENDO" + sort + "_2", nendo.substring(2));
            _svf.VrsOut("HR_CLASS" + sort, hrClass._hrNameAbbv);
            _svf.VrsOut("SCHREGNO" + sort, student._schregno);

            _svf.VrsOut("YEAR" + sort, _param.changePrintYear(hrClass._year, false));

            // 担任名出力
            final boolean notUseStaffClassHist = isNavi && Integer.parseInt(hrClass._year) < 2011; // ナビは2011年度より過去はSTAFF_CLASS_HIST_DATを使用しない。
            if (notUseStaffClassHist) {
                // 年度内に所属、クラスが変わっていた場合
                String baseHistStaff = null;
                if (student._baseHist.containsKey(hrClass._year)) {
                    final HrClass baseHrClass = (HrClass) student._baseHist.get(hrClass._year);
                    baseHistStaff = baseHrClass._staffName;
                    baseHistStaff = baseHistStaff == null ? baseHrClass._staffName2 : baseHistStaff;
                    baseHistStaff = baseHistStaff == null ? baseHrClass._staffName3 : baseHistStaff;
                }
                String staffMin = hrClass._staffName != null ? hrClass._staffName : hrClass._staffName2 != null ? hrClass._staffName2 : hrClass._staffName3;
                staffMin = baseHistStaff != null ? baseHistStaff : hrClass._staffName;
                final String staffMax = hrClass._staffName3 != null ? hrClass._staffName3 : hrClass._staffName2 != null ? hrClass._staffName2 : hrClass._staffName;

                if (null != student._entInfo._div &&
                    student._entInfo._div.equals("4") &&
                    hrClass._year.equals(student._entInfo._nendo)
                ) {
                    _svf.VrsOut("STAFFNAME" + sort, staffMax);
                } else if (null != staffMax && staffMin.equals(staffMax)) {
                    _svf.VrsOut("STAFFNAME" + sort, staffMin);
                } else {
                    _svf.VrsOut("STAFFNAME" + sort + "_2", staffMin);
                    _svf.VrsOut("STAFFNAME" + sort + "_3", staffMax);
                }
            } else {
                final Map staffMap = getPrintStaff(db2, hrClass, student, sort);
                int staffField = 2;
                for (final Iterator iter = staffMap.keySet().iterator(); iter.hasNext();) {
                    final String key = (String) iter.next();
                    final String staffName = (String) staffMap.get(key);
                    if (staffMap.size() == 1) {
                        _svf.VrsOut("STAFFNAME" + sort, staffName);
                    } else {
                        _svf.VrsOut("STAFFNAME" + sort + "_" + staffField, staffName);
                    }
                    staffField++;
                }
            }

            if (_param._certifMap.containsKey(hrClass._year)) {
                final CertifSchool certifSchool = (CertifSchool) _param._certifMap.get(hrClass._year);
                _svf.VrsOut("PRINCIPAL" + sort, certifSchool._principalName);
            }
            hrCnt++;
        }
        if (hrCnt > 1) {
            setBasePrint(student);
        }
        hasData = true;

        return hasData;
    }

    private Map getPrintStaff(
            final DB2UDB db2,
            final HrClass hrClass,
            final Student student,
            final int sort
    ) throws SQLException {

        final Map retMap = new TreeMap();

        final String staffSql = getStaffSql(hrClass, student);
        db2.query(staffSql);
        ResultSet rs = null;
        try {
            rs = db2.getResultSet();
            String befTrDiv = "";
            while (rs.next()) {
                final String trDiv = rs.getString("TR_DIV");
                final String staffName = rs.getString("STAFFNAME");
                final String fromM = rs.getString("FROM_MONTH");
                final String fromD = rs.getString("FROM_DAY");
                final String toM = rs.getString("TO_MONTH");
                final String toD = rs.getString("TO_DAY");
                final boolean isNotMonthPrint = "4".equals(fromM) && "1".equals(fromD) && "3".equals(toM) && "31".equals(toD) ? true : false;
                final String printMonth = isNotMonthPrint ? "" : "(" + fromM + "月\uFF5E" + toM + "月)";
                if ("".equals(befTrDiv)) {
                    retMap.put("1", staffName + printMonth);
                } else {
                    if (!befTrDiv.equals(trDiv) && retMap.size() > 1) {
                        break;
                    }
                    retMap.put("2", staffName + printMonth);
                }
                befTrDiv = trDiv;
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

        return retMap;
    }

    private String getStaffSql(final HrClass hrClass, final Student student) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH HIST_MIN AS ( ");
        stb.append(" SELECT ");
        stb.append("     MIN(S_APPDATE) AS S_APPDATE, ");
        stb.append("     SUM(CASE WHEN GRADE <> '" + hrClass._grade + "' OR HR_CLASS <> '" + hrClass._hrClass + "' ");
        stb.append("              THEN 1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("     ) AS DIF_CNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_HIST_DAT ");
        stb.append(" WHERE ");
        stb.append("     SCHREGNO = '" + student._schregno + "' ");
        stb.append("     AND YEAR = '" + hrClass._year + "' ");
        stb.append("     AND SEMESTER = '1' ");
        stb.append(" ), HIST_MAIN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_HIST_DAT T1, ");
        stb.append("     HIST_MIN T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + student._schregno + "' ");
        stb.append("     AND T1.YEAR = '" + hrClass._year + "' ");
        stb.append("     AND T1.SEMESTER = '1' ");
        stb.append("     AND T1.S_APPDATE = T2.S_APPDATE ");
        stb.append("     AND T2.DIF_CNT > 0 ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     '0' AS SORT, ");
        stb.append("     T1.TR_DIV, ");
        stb.append("     T1.FROM_DATE, ");
        stb.append("     T1.TO_DATE, ");
        stb.append("     MONTH(T1.FROM_DATE) AS FROM_MONTH, ");
        stb.append("     DAY(T1.FROM_DATE) AS FROM_DAY, ");
        stb.append("     MONTH(T1.TO_DATE) AS TO_MONTH, ");
        stb.append("     DAY(T1.TO_DATE) AS TO_DAY, ");
        stb.append("     L1.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     STAFF_CLASS_HIST_DAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON T1.STAFFCD = L1.STAFFCD, ");
        stb.append("     HIST_MAIN T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + hrClass._year + "' ");
        stb.append("     AND T1.SEMESTER = '1' ");
        stb.append("     AND T1.GRADE = T2.GRADE ");
        stb.append("     AND T1.HR_CLASS = T2.HR_CLASS ");
        stb.append("     AND T1.TR_DIV IN ('1', '2', '3') ");
        stb.append("     AND T1.TO_DATE >= '" + student._entInfo._date + "' ");

        stb.append(" UNION ");

        stb.append(" SELECT ");
        stb.append("     '1' AS SORT, ");
        stb.append("     T1.TR_DIV, ");
        stb.append("     T1.FROM_DATE, ");
        stb.append("     T1.TO_DATE, ");
        stb.append("     MONTH(T1.FROM_DATE) AS FROM_MONTH, ");
        stb.append("     DAY(T1.FROM_DATE) AS FROM_DAY, ");
        stb.append("     MONTH(T1.TO_DATE) AS TO_MONTH, ");
        stb.append("     DAY(T1.TO_DATE) AS TO_DAY, ");
        stb.append("     L1.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     STAFF_CLASS_HIST_DAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON T1.STAFFCD = L1.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + hrClass._year + "' ");
        stb.append("     AND T1.SEMESTER = '1' ");
        stb.append("     AND T1.GRADE = '" + hrClass._grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + hrClass._hrClass + "' ");
        stb.append("     AND T1.TR_DIV IN ('1', '2', '3') ");
        stb.append("     AND T1.TO_DATE >= '" + student._entInfo._date + "' ");
        stb.append(" ORDER BY ");
        stb.append("     TR_DIV, ");
        stb.append("     SORT, ");
        stb.append("     FROM_DATE     ");

        return stb.toString();
    }

    private void setBasePrint(final Student student) {
        if (_param._simei) {
            final String nameField = student._name.length() > 12 ? "2" : "1";
            _svf.VrsOut("NAME" + nameField, student._name);
            _svf.VrsOut("KANA", student._kana);
            final String gNameField = student._gName.length() > 12 ? "2" : "1";
            _svf.VrsOut("GUARD_NAME" + gNameField, student._gName);
            _svf.VrsOut("GUARD_KANA", student._gKana);
        }
        _svf.VrsOut("SEX", student._sex);
        _svf.VrsOut("BIRTHDAY", _param.changePrintDate(student._birthDay) + "生");
        _svf.VrsOut("ADDRESS1", student._prefName + student._addr1);
        _svf.VrsOut("ADDRESS2", student._addr2);
        _svf.VrsOut("ADDRESS3", student._addr3);

        if (isAddresDf(student)) {
            _svf.VrsOut("GUARDIANADD1", "生徒の欄に同じ");
        } else {
            _svf.VrsOut("GUARDIANADD1", student._gPrefName + student._gAddr1);
            _svf.VrsOut("GUARDIANADD2", student._gAddr2);
            _svf.VrsOut("GUARDIANADD3", student._gAddr3);
        }

        final String finishYear = KNJ_EditDate.h_format_JP_N(student._finishDate);
        _svf.VrsOut("J_GRAD_YEAR", finishYear);
        final String schoolName = student._finSchool + "　卒業";
        final String schoolField = schoolName.length() > 20 ? "2" : "1";
        _svf.VrsOut("FINSCHOOL" + schoolField, schoolName);

        _svf.VrsOut("SCHOOLNAME1", _param._schoolName);
        _svf.VrsOut("SCHOOL_ADDR1", _param._schoolAddr1);
        _svf.VrsOut("SCHOOL_ADDR2", _param._schoolAddr2);
        _svf.VrsOut("COURSENAME", student._courseName);
        _svf.VrsOut("MAJORNAME", student._majorName);

        if (null != student._entInfo._div && student._entInfo._div.equals("4")) {
            _svf.VrsOut("TRANSFER_DATE1", _param.changePrintDate(student._entInfo._date));
            _svf.VrsOut("TRANSFER_DIV1", student._entInfo._name);
            _svf.VrsOut("TRANSFERREASON1_1", student._entInfo._school);
            _svf.VrsOut("TRANSFERREASON1_2", student._entInfo._addr);
            _svf.VrsOut("TRANSFERREASON1_3", student._entInfo._reason);
        } else {
            _svf.VrsOut("ENTER_DATE", _param.changePrintDate(student._entInfo._date));
            _svf.VrsOut("ENTER_DIV", student._entInfo._name);
            _svf.VrsOut("ENTERRESONS1", student._entInfo._school);
            _svf.VrsOut("ENTERRESONS2", student._entInfo._addr);
            _svf.VrsOut("ENTERRESONS3", student._entInfo._reason);
        }

        if (null != student._aftGrdInfo) {
            _svf.VrsOut("OTHER_TRANSFERREASON2_1", student._aftGrdInfo._statName);
            _svf.VrsOut("OTHER_TRANSFERREASON2_2", student._aftGrdInfo._areaName);
            _svf.VrsOut("OTHER_TRANSFERREASON2_3", student._aftGrdInfo._thinkExam);
        }

        if (null != student._grdInfo._div && student._grdInfo._div.equals("1")) {
            _svf.VrsOut("TRANSFER_DATE4", _param.changePrintDate(student._grdInfo._date));
        } else {
            _svf.VrsOut("TRANSFER_DATE2", _param.changePrintDate(student._grdInfo._date));
            _svf.VrsOut("TRANSFER_DIV2", student._grdInfo._name);
            _svf.VrsOut("TRANSFERREASON2_1", student._grdInfo._school);
            _svf.VrsOut("TRANSFERREASON2_2", student._grdInfo._addr);
            _svf.VrsOut("TRANSFERREASON2_3", student._grdInfo._reason);
        }

        final Transfer transfer = student.getMaxTransfer();
        if (null != transfer) {
            _svf.VrsOut("TRANSFER_DATE3", _param.changePrintDate(transfer._sdate) + "\uFF5E" + _param.changePrintDate(transfer._edate));
            _svf.VrsOut("TRANSFERREASON3_1", transfer._reason);
            _svf.VrsOut("TRANSFERREASON3_2", transfer._place);
        }
        _svf.VrEndPage();
    }

    private boolean isAddresDf(final Student student) {
        final String studentAddr = student._prefName + student._addr1 + student._addr2 + student._addr3;
        final String guardAddr = student._gPrefName + student._gAddr1 + student._gAddr2 + student._gAddr3;
        return studentAddr.equals(guardAddr);
    }
    
    private boolean isNavi(
            final DB2UDB db2,
            final String year
    ) throws SQLException {
        // ナビの学校コードは00350800060。ウィザスの学校コードは35080006001。
        final String naviSchoolcd = "00350800060";
        boolean ret = false;
        final String schoolcdSql = "SELECT SCHOOLCD FROM SCHOOL_MST WHERE YEAR = '" + year + "' ";
        db2.query(schoolcdSql);
        ResultSet rs = null;
        try {
            rs = db2.getResultSet();
            while (rs.next()) {
                ret = naviSchoolcd.equals(rs.getString("SCHOOLCD"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return ret;
    }
}
 // KNJWA230Youroku

// eof
